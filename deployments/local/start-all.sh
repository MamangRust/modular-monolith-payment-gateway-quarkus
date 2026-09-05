#!/usr/bin/env bash
# Start all services using setsid for proper daemonization
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
LOG_DIR="/tmp/e2e-host-logs"
mkdir -p "$LOG_DIR"
rm -f "$LOG_DIR"/*.pid 2>/dev/null

ENV_EXPORTS='
export DB_USER=DRAGON
export DB_PASSWORD=DRAGON
export DB_USERNAME=DRAGON
export DB_PASS=DRAGON
export KAFKA_BROKERS=localhost:9092
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export REDIS_HOSTS="redis://:dragon_knight@localhost:6379,redis://:dragon_knight@localhost:6380,redis://:dragon_knight@localhost:6381,redis://:dragon_knight@localhost:6382,redis://:dragon_knight@localhost:6383,redis://:dragon_knight@localhost:6384"
export REDIS_PASSWORD=dragon_knight
export REDIS_CLUSTER_ENABLED=true
export OTEL_ENDPOINT=localhost:4317
export APP_ENV=local
export QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=drop-and-create
'

start_svc() {
    local name="$1" jar="$2"
    shift 2
    echo "Starting $name..."
    setsid bash -c "$ENV_EXPORTS
$*
exec java -jar '$ROOT/$jar'
" >"$LOG_DIR/$name.log" 2>&1 &
    sleep 0.3
    # Find the java PID
    local java_pid
    for j in $(seq 1 20); do
        java_pid=$(pgrep -f "quarkus-app/$jar" 2>/dev/null | head -1 || true)
        if [ -n "$java_pid" ]; then break; fi
        sleep 0.2
    done
    echo "${java_pid:-unknown}" >"$LOG_DIR/$name.pid"
}

start_svc role role/target/quarkus-app/quarkus-run.jar \
    DB_HOST=localhost DB_PORT=5435 DB_NAME=role_db
sleep 5

start_svc user user/target/quarkus-app/quarkus-run.jar \
    DB_HOST=localhost DB_PORT=5434 DB_NAME=user_db ROLE_GRPC_HOST=localhost

start_svc auth auth/target/quarkus-app/quarkus-run.jar \
    DB_HOST=localhost DB_PORT=5433 DB_NAME=auth_db \
    USER_GRPC_HOST=localhost ROLE_GRPC_HOST=localhost GRPC_PORT=9012

start_svc card card/target/quarkus-app/quarkus-run.jar \
    DB_HOST=localhost DB_PORT=5436 DB_NAME=card_db

start_svc merchant merchant/target/quarkus-app/quarkus-run.jar \
    DB_HOST=localhost DB_PORT=5437 DB_NAME=merchant_db

start_svc saldo saldo/target/quarkus-app/quarkus-run.jar \
    DB_HOST=localhost DB_PORT=5438 DB_NAME=saldo_db

start_svc topup topup/target/quarkus-app/quarkus-run.jar \
    DB_HOST=localhost DB_PORT=5439 DB_NAME=topup_db

start_svc transaction transaction/target/quarkus-app/quarkus-run.jar \
    DB_HOST=localhost DB_PORT=5440 DB_NAME=transaction_db

start_svc transfer transfer/target/quarkus-app/quarkus-run.jar \
    DB_HOST=localhost DB_PORT=5441 DB_NAME=transfer_db

start_svc withdraw withdraw/target/quarkus-app/quarkus-run.jar \
    DB_HOST=localhost DB_PORT=5442 DB_NAME=withdraw_db

start_svc gateway gateway/target/quarkus-app/quarkus-run.jar \
    HTTP_PORT=8080 \
    DB_HOST=localhost DB_PORT=5432 DB_NAME=PAYMENT_GATEWAY \
    AUTH_HOST=localhost USER_HOST=localhost CARD_HOST=localhost \
    MERCHANT_HOST=localhost ROLE_HOST=localhost SALDO_HOST=localhost \
    TOPUP_HOST=localhost TRANSACTION_HOST=localhost TRANSFER_HOST=localhost \
    WITHDRAW_HOST=localhost \
    JAVA_OPTS="-Dapp.jwt.enabled=false -Dquarkus.http.auth.enabled=false"

echo ""
echo "All 11 services started. Waiting for gateway..."
for i in $(seq 1 90); do
    if curl -sf http://localhost:8080/q/health > /dev/null 2>&1; then
        echo "✅ Gateway ready after ${i}s on http://localhost:8080"
        break
    fi
    if [ $i -eq 90 ]; then
        echo "❌ Gateway not ready after 180s"
        tail -5 "$LOG_DIR/gateway.log" 2>/dev/null
    fi
    sleep 2
done

echo ""
echo "=== Service Status ==="
for pidfile in "$LOG_DIR"/*.pid; do
    name=$(basename "$pidfile" .pid)
    pid=$(cat "$pidfile")
    alive="❌"
    kill -0 "$pid" 2>/dev/null && alive="✅"
    printf "%s %-15s PID %s\n" "$alive" "$name" "$pid"
done
