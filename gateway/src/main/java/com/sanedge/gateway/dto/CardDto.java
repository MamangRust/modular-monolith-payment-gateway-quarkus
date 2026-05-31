package com.sanedge.gateway.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

public class CardDto {

    public record CreateRequest(
            int userId,
            String cardType,
            String expireDate,
            String cvv,
            String cardProvider) {
    }

    public record UpdateRequest(
            int cardId,
            int userId,
            String cardType,
            String expireDate,
            String cvv,
            String cardProvider) {
    }

    public record CardResponse(
            int id,
            int userId,
            String cardNumber,
            String cardType,
            String expireDate,
            String cvv,
            String cardProvider,
            String createdAt,
            String updatedAt) {
        public static CardResponse from(pb.card.Card.CardResponse proto) {
            return new CardResponse(
                    proto.getId(),
                    proto.getUserId(),
                    proto.getCardNumber(),
                    proto.getCardType(),
                    proto.getExpireDate(),
                    proto.getCvv(),
                    proto.getCardProvider(),
                    proto.getCreatedAt(),
                    proto.getUpdatedAt());
        }
    }

    public record CardResponseDeleteAt(
            int id,
            int userId,
            String cardNumber,
            String cardType,
            String expireDate,
            String cvv,
            String cardProvider,
            String createdAt,
            String updatedAt,
            String deletedAt) {
        public static CardResponseDeleteAt from(pb.card.Card.CardResponseDeleteAt proto) {
            return new CardResponseDeleteAt(
                    proto.getId(),
                    proto.getUserId(),
                    proto.getCardNumber(),
                    proto.getCardType(),
                    proto.getExpireDate(),
                    proto.getCvv(),
                    proto.getCardProvider(),
                    proto.getCreatedAt(),
                    proto.getUpdatedAt(),
                    proto.hasDeletedAt() ? proto.getDeletedAt().getValue() : null);
        }
    }

    public record ApiResponseCard(
            String status,
            String message,
            CardResponse data) {
        public static ApiResponseCard from(pb.card.Card.ApiResponseCard proto) {
            return new ApiResponseCard(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.hasData() ? CardResponse.from(proto.getData()) : null);
        }
    }

    public record ApiResponseCardDeleteAt(
            String status,
            String message,
            CardResponseDeleteAt data) {
        public static ApiResponseCardDeleteAt from(pb.card.Card.ApiResponseCardDeleteAt proto) {
            return new ApiResponseCardDeleteAt(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.hasData() ? CardResponseDeleteAt.from(proto.getData()) : null);
        }
    }

    public record CardResponseMonthlyAmount(
            String month,
            long totalAmount) {
        public static CardResponseMonthlyAmount from(pb.card.Card.CardResponseMonthlyAmount proto) {
            return new CardResponseMonthlyAmount(proto.getMonth(), proto.getTotalAmount());
        }
    }

    public record CardResponseYearlyAmount(
            String year,
            long totalAmount) {
        public static CardResponseYearlyAmount from(pb.card.Card.CardResponseYearlyAmount proto) {
            return new CardResponseYearlyAmount(proto.getYear(), proto.getTotalAmount());
        }
    }

    public record ApiResponseMonthlyAmount(
            String status,
            String message,
            List<CardResponseMonthlyAmount> data) {
        public static ApiResponseMonthlyAmount from(pb.card.Card.ApiResponseMonthlyAmount proto) {
            return new ApiResponseMonthlyAmount(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(CardResponseMonthlyAmount::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseYearlyAmount(
            String status,
            String message,
            List<CardResponseYearlyAmount> data) {
        public static ApiResponseYearlyAmount from(pb.card.Card.ApiResponseYearlyAmount proto) {
            return new ApiResponseYearlyAmount(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(CardResponseYearlyAmount::from).collect(Collectors.toList()));
        }
    }

    public record CardResponseMonthlyBalance(
            String month,
            long totalBalance) {
        public static CardResponseMonthlyBalance from(pb.card.stats.CardStatsBalance.CardResponseMonthlyBalance proto) {
            return new CardResponseMonthlyBalance(proto.getMonth(), proto.getTotalBalance());
        }
    }

    public record CardResponseYearlyBalance(
            String year,
            long totalBalance) {
        public static CardResponseYearlyBalance from(pb.card.stats.CardStatsBalance.CardResponseYearlyBalance proto) {
            return new CardResponseYearlyBalance(proto.getYear(), proto.getTotalBalance());
        }
    }

    public record ApiResponseMonthlyBalance(
            String status,
            String message,
            List<CardResponseMonthlyBalance> data) {
        public static ApiResponseMonthlyBalance from(pb.card.stats.CardStatsBalance.ApiResponseMonthlyBalance proto) {
            return new ApiResponseMonthlyBalance(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(CardResponseMonthlyBalance::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseYearlyBalance(
            String status,
            String message,
            List<CardResponseYearlyBalance> data) {
        public static ApiResponseYearlyBalance from(pb.card.stats.CardStatsBalance.ApiResponseYearlyBalance proto) {
            return new ApiResponseYearlyBalance(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(CardResponseYearlyBalance::from).collect(Collectors.toList()));
        }
    }

    public record CardResponseDashboard(
            long totalBalance,
            long totalTopup,
            long totalWithdraw,
            long totalTransaction,
            long totalTransfer) {
        public static CardResponseDashboard from(pb.card.CardDashboard.CardResponseDashboard proto) {
            return new CardResponseDashboard(
                    proto.getTotalBalance(),
                    proto.getTotalTopup(),
                    proto.getTotalWithdraw(),
                    proto.getTotalTransaction(),
                    proto.getTotalTransfer());
        }
    }

    public record CardResponseDashboardCardNumber(
            long totalBalance,
            long totalTopup,
            long totalWithdraw,
            long totalTransaction,
            long totalTransferSend,
            long totalTransferReceiver) {
        public static CardResponseDashboardCardNumber from(pb.card.CardDashboard.CardResponseDashboardCardNumber proto) {
            return new CardResponseDashboardCardNumber(
                    proto.getTotalBalance(),
                    proto.getTotalTopup(),
                    proto.getTotalWithdraw(),
                    proto.getTotalTransaction(),
                    proto.getTotalTransferSend(),
                    proto.getTotalTransferReceiver());
        }
    }

    public record ApiResponseDashboardCard(
            String status,
            String message,
            CardResponseDashboard data) {
        public static ApiResponseDashboardCard from(pb.card.CardDashboard.ApiResponseDashboardCard proto) {
            return new ApiResponseDashboardCard(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.hasData() ? CardResponseDashboard.from(proto.getData()) : null);
        }
    }

    public record ApiResponseDashboardCardNumber(
            String status,
            String message,
            CardResponseDashboardCardNumber data) {
        public static ApiResponseDashboardCardNumber from(pb.card.CardDashboard.ApiResponseDashboardCardNumber proto) {
            return new ApiResponseDashboardCardNumber(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.hasData() ? CardResponseDashboardCardNumber.from(proto.getData()) : null);
        }
    }

    public record PaginationMeta(
            int currentPage,
            int pageSize,
            int totalPage,
            int totalRecords) {
        public static PaginationMeta from(pb.common.PaginationMeta proto) {
            return new PaginationMeta(
                    proto.getCurrentPage(),
                    proto.getPageSize(),
                    proto.getTotalPages(),
                    proto.getTotalRecords());
        }
    }

    public record ApiResponsePaginationCard(
            String status,
            String message,
            List<CardResponse> data,
            PaginationMeta paginationMeta) {
        public static ApiResponsePaginationCard from(pb.card.CardQuery.ApiResponsePaginationCard proto) {
            return new ApiResponsePaginationCard(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(CardResponse::from).collect(Collectors.toList()),
                    proto.hasPaginationMeta() ? PaginationMeta.from(proto.getPaginationMeta()) : null);
        }
    }

    public record ApiResponsePaginationCardDeleteAt(
            String status,
            String message,
            List<CardResponseDeleteAt> data,
            PaginationMeta paginationMeta) {
        public static ApiResponsePaginationCardDeleteAt from(pb.card.CardQuery.ApiResponsePaginationCardDeleteAt proto) {
            return new ApiResponsePaginationCardDeleteAt(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(CardResponseDeleteAt::from).collect(Collectors.toList()),
                    proto.hasPaginationMeta() ? PaginationMeta.from(proto.getPaginationMeta()) : null);
        }
    }

    public record SimpleResponse(
            String status,
            String message) {
        public static SimpleResponse from(pb.card.CardCommand.ApiResponseCardDelete proto) {
            return new SimpleResponse(proto.getStatus(), proto.getMessage());
        }
        public static SimpleResponse from(pb.card.CardCommand.ApiResponseCardAll proto) {
            return new SimpleResponse(proto.getStatus(), proto.getMessage());
        }
    }

    public static com.google.protobuf.Timestamp toProtoTimestamp(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return com.google.protobuf.Timestamp.getDefaultInstance();
        }
        try {
            Instant instant = Instant.parse(dateStr);
            return com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build();
        } catch (Exception e) {
            try {
                LocalDate localDate = LocalDate.parse(dateStr);
                Instant instant = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                return com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(instant.getEpochSecond())
                        .setNanos(instant.getNano())
                        .build();
            } catch (Exception ex) {
                return com.google.protobuf.Timestamp.getDefaultInstance();
            }
        }
    }
}
