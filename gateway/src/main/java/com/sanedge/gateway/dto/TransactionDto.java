package com.sanedge.gateway.dto;

import java.util.List;
import java.util.stream.Collectors;

public class TransactionDto {

    public record CreateRequest(
            String apiKey,
            String cardNumber,
            int amount,
            String paymentMethod,
            int merchantId) {
    }

    public record UpdateRequest(
            int transactionId,
            String apiKey,
            String cardNumber,
            int amount,
            String paymentMethod,
            int merchantId) {
    }

    public record TransactionResponse(
            int id,
            String cardNumber,
            String transactionNo,
            int amount,
            String paymentMethod,
            int merchantId,
            String transactionTime,
            String createdAt,
            String updatedAt) {
        public static TransactionResponse from(pb.transaction.Transaction.TransactionResponse proto) {
            return new TransactionResponse(
                    proto.getId(),
                    proto.getCardNumber(),
                    proto.getTransactionNo(),
                    proto.getAmount(),
                    proto.getPaymentMethod(),
                    proto.getMerchantId(),
                    proto.getTransactionTime(),
                    proto.getCreatedAt(),
                    proto.getUpdatedAt());
        }
    }

    public record TransactionResponseDeleteAt(
            int id,
            String cardNumber,
            String transactionNo,
            int amount,
            String paymentMethod,
            int merchantId,
            String transactionTime,
            String createdAt,
            String updatedAt,
            String deletedAt) {
        public static TransactionResponseDeleteAt from(pb.transaction.Transaction.TransactionResponseDeleteAt proto) {
            return new TransactionResponseDeleteAt(
                    proto.getId(),
                    proto.getCardNumber(),
                    proto.getTransactionNo(),
                    proto.getAmount(),
                    proto.getPaymentMethod(),
                    proto.getMerchantId(),
                    proto.getTransactionTime(),
                    proto.getCreatedAt(),
                    proto.getUpdatedAt(),
                    proto.hasDeletedAt() ? proto.getDeletedAt().getValue() : null);
        }
    }

    public record ApiResponseTransaction(
            String status,
            String message,
            TransactionResponse data) {
        public static ApiResponseTransaction from(pb.transaction.Transaction.ApiResponseTransaction proto) {
            return new ApiResponseTransaction(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.hasData() ? TransactionResponse.from(proto.getData()) : null);
        }
    }

    public record ApiResponseTransactions(
            String status,
            String message,
            List<TransactionResponse> data) {
        public static ApiResponseTransactions from(pb.transaction.Transaction.ApiResponseTransactions proto) {
            return new ApiResponseTransactions(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransactionResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseTransactionDeleteAt(
            String status,
            String message,
            TransactionResponseDeleteAt data) {
        public static ApiResponseTransactionDeleteAt from(pb.transaction.Transaction.ApiResponseTransactionDeleteAt proto) {
            return new ApiResponseTransactionDeleteAt(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.hasData() ? TransactionResponseDeleteAt.from(proto.getData()) : null);
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

    public record ApiResponsePaginationTransaction(
            String status,
            String message,
            List<TransactionResponse> data,
            PaginationMeta paginationMeta) {
        public static ApiResponsePaginationTransaction from(pb.transaction.TransactionQuery.ApiResponsePaginationTransaction proto) {
            return new ApiResponsePaginationTransaction(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransactionResponse::from).collect(Collectors.toList()),
                    proto.hasPaginationMeta() ? PaginationMeta.from(proto.getPaginationMeta()) : null);
        }
    }

    public record ApiResponsePaginationTransactionDeleteAt(
            String status,
            String message,
            List<TransactionResponseDeleteAt> data,
            PaginationMeta paginationMeta) {
        public static ApiResponsePaginationTransactionDeleteAt from(pb.transaction.TransactionQuery.ApiResponsePaginationTransactionDeleteAt proto) {
            return new ApiResponsePaginationTransactionDeleteAt(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransactionResponseDeleteAt::from).collect(Collectors.toList()),
                    proto.hasPaginationMeta() ? PaginationMeta.from(proto.getPaginationMeta()) : null);
        }
    }

    public record SimpleResponse(
            String status,
            String message) {
        public static SimpleResponse from(pb.transaction.TransactionCommand.ApiResponseTransactionDelete proto) {
            return new SimpleResponse(proto.getStatus(), proto.getMessage());
        }
        public static SimpleResponse from(pb.transaction.TransactionCommand.ApiResponseTransactionAll proto) {
            return new SimpleResponse(proto.getStatus(), proto.getMessage());
        }
    }

    public record TransactionMonthAmountResponse(
            String month,
            int totalAmount) {
        public static TransactionMonthAmountResponse from(pb.transaction.stats.TransactionStatsAmount.TransactionMonthAmountResponse proto) {
            return new TransactionMonthAmountResponse(proto.getMonth(), proto.getTotalAmount());
        }
    }

    public record TransactionYearlyAmountResponse(
            String year,
            int totalAmount) {
        public static TransactionYearlyAmountResponse from(pb.transaction.stats.TransactionStatsAmount.TransactionYearlyAmountResponse proto) {
            return new TransactionYearlyAmountResponse(proto.getYear(), proto.getTotalAmount());
        }
    }

    public record ApiResponseTransactionMonthAmount(
            String status,
            String message,
            List<TransactionMonthAmountResponse> data) {
        public static ApiResponseTransactionMonthAmount from(pb.transaction.stats.TransactionStatsAmount.ApiResponseTransactionMonthAmount proto) {
            return new ApiResponseTransactionMonthAmount(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransactionMonthAmountResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseTransactionYearAmount(
            String status,
            String message,
            List<TransactionYearlyAmountResponse> data) {
        public static ApiResponseTransactionYearAmount from(pb.transaction.stats.TransactionStatsAmount.ApiResponseTransactionYearAmount proto) {
            return new ApiResponseTransactionYearAmount(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransactionYearlyAmountResponse::from).collect(Collectors.toList()));
        }
    }

    public record TransactionMonthMethodResponse(
            String month,
            String paymentMethod,
            int totalTransactions,
            int totalAmount) {
        public static TransactionMonthMethodResponse from(pb.transaction.stats.TransactionStatsMethod.TransactionMonthMethodResponse proto) {
            return new TransactionMonthMethodResponse(proto.getMonth(), proto.getPaymentMethod(), proto.getTotalTransactions(), proto.getTotalAmount());
        }
    }

    public record TransactionYearMethodResponse(
            String year,
            String paymentMethod,
            int totalTransactions,
            int totalAmount) {
        public static TransactionYearMethodResponse from(pb.transaction.stats.TransactionStatsMethod.TransactionYearMethodResponse proto) {
            return new TransactionYearMethodResponse(proto.getYear(), proto.getPaymentMethod(), proto.getTotalTransactions(), proto.getTotalAmount());
        }
    }

    public record ApiResponseTransactionMonthMethod(
            String status,
            String message,
            List<TransactionMonthMethodResponse> data) {
        public static ApiResponseTransactionMonthMethod from(pb.transaction.stats.TransactionStatsMethod.ApiResponseTransactionMonthMethod proto) {
            return new ApiResponseTransactionMonthMethod(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransactionMonthMethodResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseTransactionYearMethod(
            String status,
            String message,
            List<TransactionYearMethodResponse> data) {
        public static ApiResponseTransactionYearMethod from(pb.transaction.stats.TransactionStatsMethod.ApiResponseTransactionYearMethod proto) {
            return new ApiResponseTransactionYearMethod(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransactionYearMethodResponse::from).collect(Collectors.toList()));
        }
    }

    public record TransactionMonthStatusSuccessResponse(
            String year,
            String month,
            int totalSuccess,
            int totalAmount) {
        public static TransactionMonthStatusSuccessResponse from(pb.transaction.stats.TransactionStatsStatus.TransactionMonthStatusSuccessResponse proto) {
            return new TransactionMonthStatusSuccessResponse(proto.getYear(), proto.getMonth(), proto.getTotalSuccess(), proto.getTotalAmount());
        }
    }

    public record TransactionYearStatusSuccessResponse(
            String year,
            int totalSuccess,
            int totalAmount) {
        public static TransactionYearStatusSuccessResponse from(pb.transaction.stats.TransactionStatsStatus.TransactionYearStatusSuccessResponse proto) {
            return new TransactionYearStatusSuccessResponse(proto.getYear(), proto.getTotalSuccess(), proto.getTotalAmount());
        }
    }

    public record TransactionMonthStatusFailedResponse(
            String year,
            String month,
            int totalFailed,
            int totalAmount) {
        public static TransactionMonthStatusFailedResponse from(pb.transaction.stats.TransactionStatsStatus.TransactionMonthStatusFailedResponse proto) {
            return new TransactionMonthStatusFailedResponse(proto.getYear(), proto.getMonth(), proto.getTotalFailed(), proto.getTotalAmount());
        }
    }

    public record TransactionYearStatusFailedResponse(
            String year,
            int totalFailed,
            int totalAmount) {
        public static TransactionYearStatusFailedResponse from(pb.transaction.stats.TransactionStatsStatus.TransactionYearStatusFailedResponse proto) {
            return new TransactionYearStatusFailedResponse(proto.getYear(), proto.getTotalFailed(), proto.getTotalAmount());
        }
    }

    public record ApiResponseTransactionMonthStatusSuccess(
            String status,
            String message,
            List<TransactionMonthStatusSuccessResponse> data) {
        public static ApiResponseTransactionMonthStatusSuccess from(pb.transaction.stats.TransactionStatsStatus.ApiResponseTransactionMonthStatusSuccess proto) {
            return new ApiResponseTransactionMonthStatusSuccess(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransactionMonthStatusSuccessResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseTransactionYearStatusSuccess(
            String status,
            String message,
            List<TransactionYearStatusSuccessResponse> data) {
        public static ApiResponseTransactionYearStatusSuccess from(pb.transaction.stats.TransactionStatsStatus.ApiResponseTransactionYearStatusSuccess proto) {
            return new ApiResponseTransactionYearStatusSuccess(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransactionYearStatusSuccessResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseTransactionMonthStatusFailed(
            String status,
            String message,
            List<TransactionMonthStatusFailedResponse> data) {
        public static ApiResponseTransactionMonthStatusFailed from(pb.transaction.stats.TransactionStatsStatus.ApiResponseTransactionMonthStatusFailed proto) {
            return new ApiResponseTransactionMonthStatusFailed(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransactionMonthStatusFailedResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseTransactionYearStatusFailed(
            String status,
            String message,
            List<TransactionYearStatusFailedResponse> data) {
        public static ApiResponseTransactionYearStatusFailed from(pb.transaction.stats.TransactionStatsStatus.ApiResponseTransactionYearStatusFailed proto) {
            return new ApiResponseTransactionYearStatusFailed(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransactionYearStatusFailedResponse::from).collect(Collectors.toList()));
        }
    }
}
