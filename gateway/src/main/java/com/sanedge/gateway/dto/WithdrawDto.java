package com.sanedge.gateway.dto;

import java.util.List;
import java.util.stream.Collectors;

public class WithdrawDto {

    public record CreateRequest(
            String cardNumber,
            int withdrawAmount) {
    }

    public record UpdateRequest(
            int withdrawId,
            String cardNumber,
            int withdrawAmount) {
    }

    public record WithdrawResponse(
            int withdrawId,
            String withdrawNo,
            String cardNumber,
            int withdrawAmount,
            String withdrawTime,
            String createdAt,
            String updatedAt) {
        public static WithdrawResponse from(pb.withdraw.Withdraw.WithdrawResponse proto) {
            return new WithdrawResponse(
                    proto.getWithdrawId(),
                    proto.getWithdrawNo(),
                    proto.getCardNumber(),
                    proto.getWithdrawAmount(),
                    proto.getWithdrawTime(),
                    proto.getCreatedAt(),
                    proto.getUpdatedAt());
        }
    }

    public record WithdrawResponseDeleteAt(
            int withdrawId,
            String withdrawNo,
            String cardNumber,
            int withdrawAmount,
            String withdrawTime,
            String createdAt,
            String updatedAt,
            String deletedAt) {
        public static WithdrawResponseDeleteAt from(pb.withdraw.Withdraw.WithdrawResponseDeleteAt proto) {
            return new WithdrawResponseDeleteAt(
                    proto.getWithdrawId(),
                    proto.getWithdrawNo(),
                    proto.getCardNumber(),
                    proto.getWithdrawAmount(),
                    proto.getWithdrawTime(),
                    proto.getCreatedAt(),
                    proto.getUpdatedAt(),
                    proto.hasDeletedAt() ? proto.getDeletedAt().getValue() : null);
        }
    }

    public record ApiResponseWithdraw(
            String status,
            String message,
            WithdrawResponse data) {
        public static ApiResponseWithdraw from(pb.withdraw.Withdraw.ApiResponseWithdraw proto) {
            return new ApiResponseWithdraw(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.hasData() ? WithdrawResponse.from(proto.getData()) : null);
        }
    }

    public record ApiResponsesWithdraw(
            String status,
            String message,
            List<WithdrawResponse> data) {
        public static ApiResponsesWithdraw from(pb.withdraw.Withdraw.ApiResponsesWithdraw proto) {
            return new ApiResponsesWithdraw(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(WithdrawResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseWithdrawDeleteAt(
            String status,
            String message,
            WithdrawResponseDeleteAt data) {
        public static ApiResponseWithdrawDeleteAt from(pb.withdraw.Withdraw.ApIResponseWithdrawDeleteAt proto) {
            return new ApiResponseWithdrawDeleteAt(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.hasData() ? WithdrawResponseDeleteAt.from(proto.getData()) : null);
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

    public record ApiResponsePaginationWithdraw(
            String status,
            String message,
            List<WithdrawResponse> data,
            PaginationMeta paginationMeta) {
        public static ApiResponsePaginationWithdraw from(pb.withdraw.WithdrawQuery.ApiResponsePaginationWithdraw proto) {
            return new ApiResponsePaginationWithdraw(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(WithdrawResponse::from).collect(Collectors.toList()),
                    proto.hasPaginationMeta() ? PaginationMeta.from(proto.getPaginationMeta()) : null);
        }
    }

    public record ApiResponsePaginationWithdrawDeleteAt(
            String status,
            String message,
            List<WithdrawResponseDeleteAt> data,
            PaginationMeta paginationMeta) {
        public static ApiResponsePaginationWithdrawDeleteAt from(pb.withdraw.WithdrawQuery.ApiResponsePaginationWithdrawDeleteAt proto) {
            return new ApiResponsePaginationWithdrawDeleteAt(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(WithdrawResponseDeleteAt::from).collect(Collectors.toList()),
                    proto.hasPaginationMeta() ? PaginationMeta.from(proto.getPaginationMeta()) : null);
        }
    }

    public record SimpleResponse(
            String status,
            String message) {
        public static SimpleResponse from(pb.withdraw.WithdrawCommand.ApiResponseWithdrawDelete proto) {
            return new SimpleResponse(proto.getStatus(), proto.getMessage());
        }
        public static SimpleResponse from(pb.withdraw.WithdrawCommand.ApiResponseWithdrawAll proto) {
            return new SimpleResponse(proto.getStatus(), proto.getMessage());
        }
    }

    public record WithdrawMonthlyAmountResponse(
            String month,
            int totalAmount) {
        public static WithdrawMonthlyAmountResponse from(pb.withdraw.stats.WithdrawStatsAmount.WithdrawMonthlyAmountResponse proto) {
            return new WithdrawMonthlyAmountResponse(proto.getMonth(), proto.getTotalAmount());
        }
    }

    public record WithdrawYearlyAmountResponse(
            String year,
            int totalAmount) {
        public static WithdrawYearlyAmountResponse from(pb.withdraw.stats.WithdrawStatsAmount.WithdrawYearlyAmountResponse proto) {
            return new WithdrawYearlyAmountResponse(proto.getYear(), proto.getTotalAmount());
        }
    }

    public record ApiResponseWithdrawMonthAmount(
            String status,
            String message,
            List<WithdrawMonthlyAmountResponse> data) {
        public static ApiResponseWithdrawMonthAmount from(pb.withdraw.stats.WithdrawStatsAmount.ApiResponseWithdrawMonthAmount proto) {
            return new ApiResponseWithdrawMonthAmount(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(WithdrawMonthlyAmountResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseWithdrawYearAmount(
            String status,
            String message,
            List<WithdrawYearlyAmountResponse> data) {
        public static ApiResponseWithdrawYearAmount from(pb.withdraw.stats.WithdrawStatsAmount.ApiResponseWithdrawYearAmount proto) {
            return new ApiResponseWithdrawYearAmount(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(WithdrawYearlyAmountResponse::from).collect(Collectors.toList()));
        }
    }

    public record WithdrawMonthStatusSuccessResponse(
            String year,
            String month,
            int totalSuccess,
            int totalAmount) {
        public static WithdrawMonthStatusSuccessResponse from(pb.withdraw.stats.WithdrawStatsStatus.WithdrawMonthStatusSuccessResponse proto) {
            return new WithdrawMonthStatusSuccessResponse(proto.getYear(), proto.getMonth(), proto.getTotalSuccess(), proto.getTotalAmount());
        }
    }

    public record WithdrawYearStatusSuccessResponse(
            String year,
            int totalSuccess,
            int totalAmount) {
        public static WithdrawYearStatusSuccessResponse from(pb.withdraw.stats.WithdrawStatsStatus.WithdrawYearStatusSuccessResponse proto) {
            return new WithdrawYearStatusSuccessResponse(proto.getYear(), proto.getTotalSuccess(), proto.getTotalAmount());
        }
    }

    public record WithdrawMonthStatusFailedResponse(
            String year,
            String month,
            int totalFailed,
            int totalAmount) {
        public static WithdrawMonthStatusFailedResponse from(pb.withdraw.stats.WithdrawStatsStatus.WithdrawMonthStatusFailedResponse proto) {
            return new WithdrawMonthStatusFailedResponse(proto.getYear(), proto.getMonth(), proto.getTotalFailed(), proto.getTotalAmount());
        }
    }

    public record WithdrawYearStatusFailedResponse(
            String year,
            int totalFailed,
            int totalAmount) {
        public static WithdrawYearStatusFailedResponse from(pb.withdraw.stats.WithdrawStatsStatus.WithdrawYearStatusFailedResponse proto) {
            return new WithdrawYearStatusFailedResponse(proto.getYear(), proto.getTotalFailed(), proto.getTotalAmount());
        }
    }

    public record ApiResponseWithdrawMonthStatusSuccess(
            String status,
            String message,
            List<WithdrawMonthStatusSuccessResponse> data) {
        public static ApiResponseWithdrawMonthStatusSuccess from(pb.withdraw.stats.WithdrawStatsStatus.ApiResponseWithdrawMonthStatusSuccess proto) {
            return new ApiResponseWithdrawMonthStatusSuccess(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(WithdrawMonthStatusSuccessResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseWithdrawYearStatusSuccess(
            String status,
            String message,
            List<WithdrawYearStatusSuccessResponse> data) {
        public static ApiResponseWithdrawYearStatusSuccess from(pb.withdraw.stats.WithdrawStatsStatus.ApiResponseWithdrawYearStatusSuccess proto) {
            return new ApiResponseWithdrawYearStatusSuccess(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(WithdrawYearStatusSuccessResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseWithdrawMonthStatusFailed(
            String status,
            String message,
            List<WithdrawMonthStatusFailedResponse> data) {
        public static ApiResponseWithdrawMonthStatusFailed from(pb.withdraw.stats.WithdrawStatsStatus.ApiResponseWithdrawMonthStatusFailed proto) {
            return new ApiResponseWithdrawMonthStatusFailed(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(WithdrawMonthStatusFailedResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseWithdrawYearStatusFailed(
            String status,
            String message,
            List<WithdrawYearStatusFailedResponse> data) {
        public static ApiResponseWithdrawYearStatusFailed from(pb.withdraw.stats.WithdrawStatsStatus.ApiResponseWithdrawYearStatusFailed proto) {
            return new ApiResponseWithdrawYearStatusFailed(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(WithdrawYearStatusFailedResponse::from).collect(Collectors.toList()));
        }
    }
}
