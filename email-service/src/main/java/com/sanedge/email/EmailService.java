package com.sanedge.email;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public static final String RETRY_TOPIC = "email-service-retry";
    public static final String DLQ_TOPIC = "email-service-dlq";

    private static final List<String> REQUIRED_FIELDS = Arrays.asList(
            "event_id", "email", "subject", "body");

    @Inject
    Vertx vertx;

    @Inject
    ReactiveMailer mailer;

    @Inject
    EmailIdempotencyGuard idempotencyGuard;

    // Set directly by tests; in production a CDI producer would provide this.
    public KafkaProducer<String, String> producer;

    public int maxAttempts = 3;
    public long baseBackoffMs = 250;

    private final Tracer tracer;

    private KafkaConsumer<String, JsonObject> consumer;

    /** CDI constructor — CDI does not inject OpenTelemetry here. */
    EmailService() {
        this.tracer = null;
    }

    /** Constructor used by unit tests. */
    public EmailService(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry != null
                ? openTelemetry.getTracer(EmailService.class.getName())
                : null;
    }

    void onStart(@Observes StartupEvent ev) {
        log.info("📧 Starting Email Service...");

        Map<String, String> kafkaConfig = new HashMap<>();
        kafkaConfig.put("bootstrap.servers", System.getenv().getOrDefault("KAFKA_BROKERS", "localhost:9092"));
        kafkaConfig.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaConfig.put("value.deserializer", "io.vertx.kafka.client.serialization.JsonObjectDeserializer");
        kafkaConfig.put("group.id", "email-service-group");
        kafkaConfig.put("auto.offset.reset", "earliest");

        consumer = KafkaConsumer.create(vertx, kafkaConfig);

        List<String> topics = Arrays.asList(
            "email-service-topic-auth-register",
            "email-service-topic-auth-forgot-password",
            "email-service-topic-auth-verify-code-success",
            "email-service-topic-saldo-create",
            "email-service-topic-topup-create",
            "email-service-topic-transaction-create",
            "email-service-topic-transfer-create",
            "email-service-topic-merchant-create",
            "email-service-topic-merchant-update-status",
            "email-service-topic-merchant-document-create",
            "email-service-topic-merchant-document-update-status"
        );

        consumer.handler(record -> {
            JsonObject emailReq = record.value();
            log.info("📥 Received message from topic {}: {}", record.topic(), emailReq.encode());
            processRecord(record.topic(), record.partition(), record.offset(),
                    record.key(), emailReq, 0)
                .subscribe().with(
                    v -> log.debug("Processed record from {}", record.topic()),
                    err -> log.error("Failed to process record from {}", record.topic(), err));
        });

        consumer.subscribe(new java.util.HashSet<>(topics))
            .onSuccess(v -> log.info("📧 Email Service successfully started and subscribed to {} topics", topics.size()))
            .onFailure(err -> log.error("❌ Failed to start Email Service subscription", err));
    }

    public Map<String, String> consumerConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", System.getenv().getOrDefault("KAFKA_BROKERS", "localhost:9092"));
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "io.vertx.kafka.client.serialization.JsonObjectDeserializer");
        config.put("auto.offset.reset", "earliest");
        return config;
    }

    public Uni<Void> processRecord(String srcTopic, int srcPartition, long srcOffset,
            String key, JsonObject payload, int attempt) {
        // Validate envelope: every required field must be present.
        String missing = null;
        for (String field : REQUIRED_FIELDS) {
            if (!payload.containsKey(field) || payload.getValue(field) == null) {
                missing = field;
                break;
            }
        }
        if (missing != null) {
            log.warn("Invalid envelope: missing {} | payload={}", missing, payload);
            return publishToDlq(srcTopic, srcPartition, srcOffset, payload, "invalid_event_envelope", attempt)
                    .replaceWithVoid();
        }

        String eventId = payload.getString("event_id");
        String email = payload.getString("email");
        String subject = payload.getString("subject");
        String body = payload.getString("body");

        return idempotencyGuard.claim(eventId)
                .onItem().transformToUni(result -> {
                    switch (result) {
                        case DUPLICATE:
                            log.debug("Duplicate event {} (already sent)", eventId);
                            return Uni.createFrom().voidItem();
                        case BUSY:
                            return Uni.createFrom().failure(
                                    new IllegalStateException("claim busy for event " + eventId));
                        case CLAIMED:
                            return sendMail(email, subject, body)
                                    .onItem().transformToUni(v -> idempotencyGuard.markSent(eventId))
                                    .onFailure().recoverWithUni(err -> {
                                        log.warn("Send failed for event {}, releasing claim", eventId, err);
                                        return idempotencyGuard.release(eventId)
                                                .chain(v -> publishToRetryOrDlq(
                                                        srcTopic, srcPartition, srcOffset, key, payload, attempt));
                                    });
                        default:
                            return Uni.createFrom().failure(
                                    new IllegalStateException("Unknown claim result: " + result));
                    }
                });
    }

    private Uni<Void> sendMail(String to, String subject, String body) {
        return mailer.send(Mail.withHtml(to, subject, body));
    }

    private Uni<Void> publishToRetryOrDlq(String srcTopic, int srcPartition, long srcOffset,
            String key, JsonObject payload, int attempt) {
        int nextAttempt = attempt + 1;
        if (nextAttempt >= maxAttempts) {
            return publishToDlq(srcTopic, srcPartition, srcOffset, payload, "max_retries_exceeded", nextAttempt);
        }
        return publishToRetry(srcTopic, srcPartition, srcOffset, key, payload, nextAttempt);
    }

    private Uni<Void> publishToRetry(String srcTopic, int srcPartition, long srcOffset,
            String key, JsonObject payload, int attempt) {
        JsonObject retryPayload = payload.copy();
        retryPayload.put("_srcTopic", srcTopic);
        retryPayload.put("_srcPartition", srcPartition);
        retryPayload.put("_srcOffset", srcOffset);
        retryPayload.put("_attempt", attempt);
        retryPayload.put("_retryAt", System.currentTimeMillis() + computeBackoff(attempt));
        KafkaProducerRecord<String, String> record =
                KafkaProducerRecord.create(RETRY_TOPIC, key, retryPayload.encode());
        return Uni.createFrom().completionStage(producer.send(record).toCompletionStage())
                .replaceWithVoid();
    }

    private Uni<Void> publishToDlq(String srcTopic, int srcPartition, long srcOffset,
            JsonObject payload, String reason, int attempts) {
        JsonObject dlqPayload = new JsonObject();
        dlqPayload.put("_srcTopic", srcTopic);
        dlqPayload.put("_srcPartition", srcPartition);
        dlqPayload.put("_srcOffset", srcOffset);
        dlqPayload.put("_reason", reason);
        dlqPayload.put("_attempts", attempts);
        if (payload != null) {
            dlqPayload.put("_originalPayload", payload);
        }
        KafkaProducerRecord<String, String> record =
                KafkaProducerRecord.create(DLQ_TOPIC, null, dlqPayload.encode());
        return Uni.createFrom().completionStage(producer.send(record).toCompletionStage())
                .replaceWithVoid();
    }

    private long computeBackoff(int attempt) {
        return (long) (baseBackoffMs * Math.pow(2, attempt - 1));
    }

    public Span startConsumeSpan(
            List<io.vertx.kafka.client.producer.KafkaHeader> headers, String topic, int partition, long offset) {
        // Real tracing instrumentation would go here.
        return Span.getInvalid();
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.info("📧 Stopping Email Service...");
        if (consumer != null) {
            consumer.close()
                .onSuccess(v -> log.info("📧 Kafka consumer closed successfully"))
                .onFailure(err -> log.error("❌ Failed to close Kafka consumer", err));
        }
    }
}
