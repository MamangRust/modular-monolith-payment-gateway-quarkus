package com.sanedge.gateway.dto;

import java.util.List;
import java.util.stream.Collectors;

public class MerchantDto {

    public record CreateRequest(
            String name,
            int userId) {
    }

    public record UpdateRequest(
            int id,
            String name,
            int userId,
            String status) {
    }

    public record MerchantResponse(
            int id,
            String name,
            String apiKey,
            String status,
            int userId,
            String createdAt,
            String updatedAt) {
        public static MerchantResponse from(pb.merchant.Merchant.MerchantResponse proto) {
            return new MerchantResponse(
                    proto.getId(),
                    proto.getName(),
                    proto.getApiKey(),
                    proto.getStatus(),
                    proto.getUserId(),
                    proto.getCreatedAt(),
                    proto.getUpdatedAt());
        }
    }

    public record MerchantResponseDeleteAt(
            int id,
            String name,
            String apiKey,
            String status,
            int userId,
            String createdAt,
            String updatedAt,
            String deletedAt) {
        public static MerchantResponseDeleteAt from(pb.merchant.Merchant.MerchantResponseDeleteAt proto) {
            return new MerchantResponseDeleteAt(
                    proto.getId(),
                    proto.getName(),
                    proto.getApiKey(),
                    proto.getStatus(),
                    proto.getUserId(),
                    proto.getCreatedAt(),
                    proto.getUpdatedAt(),
                    proto.hasDeletedAt() ? proto.getDeletedAt().getValue() : null);
        }
    }

    public record ApiResponseMerchant(
            String status,
            String message,
            MerchantResponse data) {
        public static ApiResponseMerchant from(pb.merchant.Merchant.ApiResponseMerchant proto) {
            return new ApiResponseMerchant(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.hasData() ? MerchantResponse.from(proto.getData()) : null);
        }
    }

    public record ApiResponseMerchantDeleteAt(
            String status,
            String message,
            MerchantResponseDeleteAt data) {
        public static ApiResponseMerchantDeleteAt from(pb.merchant.Merchant.ApiResponseMerchantDeleteAt proto) {
            return new ApiResponseMerchantDeleteAt(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.hasData() ? MerchantResponseDeleteAt.from(proto.getData()) : null);
        }
    }

    public record ApiResponsesMerchant(
            String status,
            String message,
            List<MerchantResponse> data) {
        public static ApiResponsesMerchant from(pb.merchant.Merchant.ApiResponsesMerchant proto) {
            return new ApiResponsesMerchant(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(MerchantResponse::from).collect(Collectors.toList()));
        }
    }

    public record MerchantTransactionResponse(
            int id,
            String cardNumber,
            int amount,
            String paymentMethod,
            int merchantId,
            String merchantName,
            String transactionTime,
            String createdAt,
            String updatedAt,
            String deletedAt) {
        public static MerchantTransactionResponse from(
                pb.merchant.MerchantTransaction.MerchantTransactionResponse proto) {
            return new MerchantTransactionResponse(
                    proto.getId(),
                    proto.getCardNumber(),
                    proto.getAmount(),
                    proto.getPaymentMethod(),
                    proto.getMerchantId(),
                    proto.getMerchantName(),
                    proto.getTransactionTime(),
                    proto.getCreatedAt(),
                    proto.getUpdatedAt(),
                    proto.hasDeletedAt() ? proto.getDeletedAt().getValue() : null);
        }
    }

    public record ApiResponsePaginationMerchantTransaction(
            String status,
            String message,
            List<MerchantTransactionResponse> data,
            PaginationMeta paginationMeta) {
        public static ApiResponsePaginationMerchantTransaction from(
                pb.merchant.MerchantTransaction.ApiResponsePaginationMerchantTransaction proto) {
            return new ApiResponsePaginationMerchantTransaction(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(MerchantTransactionResponse::from).collect(Collectors.toList()),
                    proto.hasPaginationMeta() ? PaginationMeta.from(proto.getPaginationMeta()) : null);
        }
    }

    public record MerchantResponseMonthlyAmount(
            String month,
            long totalAmount) {
        public static MerchantResponseMonthlyAmount from(
                pb.merchant.stats.MerchantStatsAmount.MerchantResponseMonthlyAmount proto) {
            return new MerchantResponseMonthlyAmount(proto.getMonth(), proto.getTotalAmount());
        }
    }

    public record MerchantResponseYearlyAmount(
            String year,
            long totalAmount) {
        public static MerchantResponseYearlyAmount from(
                pb.merchant.stats.MerchantStatsAmount.MerchantResponseYearlyAmount proto) {
            return new MerchantResponseYearlyAmount(proto.getYear(), proto.getTotalAmount());
        }
    }

    public record ApiResponseMerchantMonthlyAmount(
            String status,
            String message,
            List<MerchantResponseMonthlyAmount> data) {
        public static ApiResponseMerchantMonthlyAmount from(
                pb.merchant.stats.MerchantStatsAmount.ApiResponseMerchantMonthlyAmount proto) {
            return new ApiResponseMerchantMonthlyAmount(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(MerchantResponseMonthlyAmount::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseMerchantYearlyAmount(
            String status,
            String message,
            List<MerchantResponseYearlyAmount> data) {
        public static ApiResponseMerchantYearlyAmount from(
                pb.merchant.stats.MerchantStatsAmount.ApiResponseMerchantYearlyAmount proto) {
            return new ApiResponseMerchantYearlyAmount(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(MerchantResponseYearlyAmount::from).collect(Collectors.toList()));
        }
    }

    public record MerchantResponseMonthlyPaymentMethod(
            String month,
            String paymentMethod,
            long totalAmount) {
        public static MerchantResponseMonthlyPaymentMethod from(
                pb.merchant.stats.MerchantStatsMethod.MerchantResponseMonthlyPaymentMethod proto) {
            return new MerchantResponseMonthlyPaymentMethod(proto.getMonth(), proto.getPaymentMethod(),
                    proto.getTotalAmount());
        }
    }

    public record MerchantResponseYearlyPaymentMethod(
            String year,
            String paymentMethod,
            long totalAmount) {
        public static MerchantResponseYearlyPaymentMethod from(
                pb.merchant.stats.MerchantStatsMethod.MerchantResponseYearlyPaymentMethod proto) {
            return new MerchantResponseYearlyPaymentMethod(proto.getYear(), proto.getPaymentMethod(),
                    proto.getTotalAmount());
        }
    }

    public record ApiResponseMerchantMonthlyPaymentMethod(
            String status,
            String message,
            List<MerchantResponseMonthlyPaymentMethod> data) {
        public static ApiResponseMerchantMonthlyPaymentMethod from(
                pb.merchant.stats.MerchantStatsMethod.ApiResponseMerchantMonthlyPaymentMethod proto) {
            return new ApiResponseMerchantMonthlyPaymentMethod(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(MerchantResponseMonthlyPaymentMethod::from)
                            .collect(Collectors.toList()));
        }
    }

    public record ApiResponseMerchantYearlyPaymentMethod(
            String status,
            String message,
            List<MerchantResponseYearlyPaymentMethod> data) {
        public static ApiResponseMerchantYearlyPaymentMethod from(
                pb.merchant.stats.MerchantStatsMethod.ApiResponseMerchantYearlyPaymentMethod proto) {
            return new ApiResponseMerchantYearlyPaymentMethod(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(MerchantResponseYearlyPaymentMethod::from)
                            .collect(Collectors.toList()));
        }
    }

    public record MerchantResponseMonthlyTotalAmount(
            String month,
            String year,
            long totalAmount) {
        public static MerchantResponseMonthlyTotalAmount from(
                pb.merchant.stats.MerchantStatsTotalamount.MerchantResponseMonthlyTotalAmount proto) {
            return new MerchantResponseMonthlyTotalAmount(proto.getMonth(), proto.getYear(), proto.getTotalAmount());
        }
    }

    public record MerchantResponseYearlyTotalAmount(
            String year,
            long totalAmount) {
        public static MerchantResponseYearlyTotalAmount from(
                pb.merchant.stats.MerchantStatsTotalamount.MerchantResponseYearlyTotalAmount proto) {
            return new MerchantResponseYearlyTotalAmount(proto.getYear(), proto.getTotalAmount());
        }
    }

    public record ApiResponseMerchantMonthlyTotalAmount(
            String status,
            String message,
            List<MerchantResponseMonthlyTotalAmount> data) {
        public static ApiResponseMerchantMonthlyTotalAmount from(
                pb.merchant.stats.MerchantStatsTotalamount.ApiResponseMerchantMonthlyTotalAmount proto) {
            return new ApiResponseMerchantMonthlyTotalAmount(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(MerchantResponseMonthlyTotalAmount::from)
                            .collect(Collectors.toList()));
        }
    }

    public record ApiResponseMerchantYearlyTotalAmount(
            String status,
            String message,
            List<MerchantResponseYearlyTotalAmount> data) {
        public static ApiResponseMerchantYearlyTotalAmount from(
                pb.merchant.stats.MerchantStatsTotalamount.ApiResponseMerchantYearlyTotalAmount proto) {
            return new ApiResponseMerchantYearlyTotalAmount(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(MerchantResponseYearlyTotalAmount::from)
                            .collect(Collectors.toList()));
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

    public record ApiResponsePaginationMerchant(
            String status,
            String message,
            List<MerchantResponse> data,
            PaginationMeta paginationMeta) {
        public static ApiResponsePaginationMerchant from(
                pb.merchant.MerchantQuery.ApiResponsePaginationMerchant proto) {
            return new ApiResponsePaginationMerchant(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(MerchantResponse::from).collect(Collectors.toList()),
                    proto.hasPaginationMeta() ? PaginationMeta.from(proto.getPaginationMeta()) : null);
        }
    }

    public record ApiResponsePaginationMerchantDeleteAt(
            String status,
            String message,
            List<MerchantResponseDeleteAt> data,
            PaginationMeta paginationMeta) {
        public static ApiResponsePaginationMerchantDeleteAt from(
                pb.merchant.MerchantQuery.ApiResponsePaginationMerchantDeleteAt proto) {
            return new ApiResponsePaginationMerchantDeleteAt(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(MerchantResponseDeleteAt::from).collect(Collectors.toList()),
                    proto.hasPaginationMeta() ? PaginationMeta.from(proto.getPaginationMeta()) : null);
        }
    }
}
