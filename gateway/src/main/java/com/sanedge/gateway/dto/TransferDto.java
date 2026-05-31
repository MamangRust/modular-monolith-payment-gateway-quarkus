package com.sanedge.gateway.dto;

import java.util.List;
import java.util.stream.Collectors;

public class TransferDto {

    public record CreateRequest(
            String transferFrom,
            String transferTo,
            int transferAmount) {
    }

    public record UpdateRequest(
            int transferId,
            String transferFrom,
            String transferTo,
            int transferAmount) {
    }

    public record TransferResponse(
            int id,
            String transferNo,
            String transferFrom,
            String transferTo,
            int transferAmount,
            String transferTime,
            String createdAt,
            String updatedAt) {
        public static TransferResponse from(pb.transfer.Transfer.TransferResponse proto) {
            return new TransferResponse(
                    proto.getId(),
                    proto.getTransferNo(),
                    proto.getTransferFrom(),
                    proto.getTransferTo(),
                    proto.getTransferAmount(),
                    proto.getTransferTime(),
                    proto.getCreatedAt(),
                    proto.getUpdatedAt());
        }
    }

    public record TransferResponseDeleteAt(
            int id,
            String transferNo,
            String transferFrom,
            String transferTo,
            int transferAmount,
            String transferTime,
            String createdAt,
            String updatedAt,
            String deletedAt) {
        public static TransferResponseDeleteAt from(pb.transfer.Transfer.TransferResponseDeleteAt proto) {
            return new TransferResponseDeleteAt(
                    proto.getId(),
                    proto.getTransferNo(),
                    proto.getTransferFrom(),
                    proto.getTransferTo(),
                    proto.getTransferAmount(),
                    proto.getTransferTime(),
                    proto.getCreatedAt(),
                    proto.getUpdatedAt(),
                    proto.hasDeletedAt() ? proto.getDeletedAt().getValue() : null);
        }
    }

    public record ApiResponseTransfer(
            String status,
            String message,
            TransferResponse data) {
        public static ApiResponseTransfer from(pb.transfer.Transfer.ApiResponseTransfer proto) {
            return new ApiResponseTransfer(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.hasData() ? TransferResponse.from(proto.getData()) : null);
        }
    }

    public record ApiResponseTransfers(
            String status,
            String message,
            List<TransferResponse> data) {
        public static ApiResponseTransfers from(pb.transfer.TransferQuery.ApiResponseTransfers proto) {
            return new ApiResponseTransfers(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransferResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseTransferDeleteAt(
            String status,
            String message,
            TransferResponseDeleteAt data) {
        public static ApiResponseTransferDeleteAt from(pb.transfer.Transfer.ApIResponseTransferDeleteAt proto) {
            return new ApiResponseTransferDeleteAt(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.hasData() ? TransferResponseDeleteAt.from(proto.getData()) : null);
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

    public record ApiResponsePaginationTransfer(
            String status,
            String message,
            List<TransferResponse> data,
            PaginationMeta paginationMeta) {
        public static ApiResponsePaginationTransfer from(pb.transfer.TransferQuery.ApiResponsePaginationTransfer proto) {
            return new ApiResponsePaginationTransfer(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransferResponse::from).collect(Collectors.toList()),
                    proto.hasPaginationMeta() ? PaginationMeta.from(proto.getPaginationMeta()) : null);
        }
    }

    public record ApiResponsePaginationTransferDeleteAt(
            String status,
            String message,
            List<TransferResponseDeleteAt> data,
            PaginationMeta paginationMeta) {
        public static ApiResponsePaginationTransferDeleteAt from(pb.transfer.TransferQuery.ApiResponsePaginationTransferDeleteAt proto) {
            return new ApiResponsePaginationTransferDeleteAt(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransferResponseDeleteAt::from).collect(Collectors.toList()),
                    proto.hasPaginationMeta() ? PaginationMeta.from(proto.getPaginationMeta()) : null);
        }
    }

    public record SimpleResponse(
            String status,
            String message) {
        public static SimpleResponse from(pb.transfer.TransferCommand.ApiResponseTransferDelete proto) {
            return new SimpleResponse(proto.getStatus(), proto.getMessage());
        }
        public static SimpleResponse from(pb.transfer.TransferCommand.ApiResponseTransferAll proto) {
            return new SimpleResponse(proto.getStatus(), proto.getMessage());
        }
    }

    public record TransferMonthAmountResponse(
            String month,
            int totalAmount) {
        public static TransferMonthAmountResponse from(pb.transfer.stats.TransferStatsAmount.TransferMonthAmountResponse proto) {
            return new TransferMonthAmountResponse(proto.getMonth(), proto.getTotalAmount());
        }
    }

    public record TransferYearAmountResponse(
            String year,
            int totalAmount) {
        public static TransferYearAmountResponse from(pb.transfer.stats.TransferStatsAmount.TransferYearAmountResponse proto) {
            return new TransferYearAmountResponse(proto.getYear(), proto.getTotalAmount());
        }
    }

    public record ApiResponseTransferMonthAmount(
            String status,
            String message,
            List<TransferMonthAmountResponse> data) {
        public static ApiResponseTransferMonthAmount from(pb.transfer.stats.TransferStatsAmount.ApiResponseTransferMonthAmount proto) {
            return new ApiResponseTransferMonthAmount(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransferMonthAmountResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseTransferYearAmount(
            String status,
            String message,
            List<TransferYearAmountResponse> data) {
        public static ApiResponseTransferYearAmount from(pb.transfer.stats.TransferStatsAmount.ApiResponseTransferYearAmount proto) {
            return new ApiResponseTransferYearAmount(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransferYearAmountResponse::from).collect(Collectors.toList()));
        }
    }

    public record TransferMonthStatusSuccessResponse(
            String year,
            String month,
            int totalSuccess,
            int totalAmount) {
        public static TransferMonthStatusSuccessResponse from(pb.transfer.stats.TransferStatsStatus.TransferMonthStatusSuccessResponse proto) {
            return new TransferMonthStatusSuccessResponse(proto.getYear(), proto.getMonth(), proto.getTotalSuccess(), proto.getTotalAmount());
        }
    }

    public record TransferYearStatusSuccessResponse(
            String year,
            int totalSuccess,
            int totalAmount) {
        public static TransferYearStatusSuccessResponse from(pb.transfer.stats.TransferStatsStatus.TransferYearStatusSuccessResponse proto) {
            return new TransferYearStatusSuccessResponse(proto.getYear(), proto.getTotalSuccess(), proto.getTotalAmount());
        }
    }

    public record TransferMonthStatusFailedResponse(
            String year,
            String month,
            int totalFailed,
            int totalAmount) {
        public static TransferMonthStatusFailedResponse from(pb.transfer.stats.TransferStatsStatus.TransferMonthStatusFailedResponse proto) {
            return new TransferMonthStatusFailedResponse(proto.getYear(), proto.getMonth(), proto.getTotalFailed(), proto.getTotalAmount());
        }
    }

    public record TransferYearStatusFailedResponse(
            String year,
            int totalFailed,
            int totalAmount) {
        public static TransferYearStatusFailedResponse from(pb.transfer.stats.TransferStatsStatus.TransferYearStatusFailedResponse proto) {
            return new TransferYearStatusFailedResponse(proto.getYear(), proto.getTotalFailed(), proto.getTotalAmount());
        }
    }

    public record ApiResponseTransferMonthStatusSuccess(
            String status,
            String message,
            List<TransferMonthStatusSuccessResponse> data) {
        public static ApiResponseTransferMonthStatusSuccess from(pb.transfer.stats.TransferStatsStatus.ApiResponseTransferMonthStatusSuccess proto) {
            return new ApiResponseTransferMonthStatusSuccess(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransferMonthStatusSuccessResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseTransferYearStatusSuccess(
            String status,
            String message,
            List<TransferYearStatusSuccessResponse> data) {
        public static ApiResponseTransferYearStatusSuccess from(pb.transfer.stats.TransferStatsStatus.ApiResponseTransferYearStatusSuccess proto) {
            return new ApiResponseTransferYearStatusSuccess(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransferYearStatusSuccessResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseTransferMonthStatusFailed(
            String status,
            String message,
            List<TransferMonthStatusFailedResponse> data) {
        public static ApiResponseTransferMonthStatusFailed from(pb.transfer.stats.TransferStatsStatus.ApiResponseTransferMonthStatusFailed proto) {
            return new ApiResponseTransferMonthStatusFailed(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransferMonthStatusFailedResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseTransferYearStatusFailed(
            String status,
            String message,
            List<TransferYearStatusFailedResponse> data) {
        public static ApiResponseTransferYearStatusFailed from(pb.transfer.stats.TransferStatsStatus.ApiResponseTransferYearStatusFailed proto) {
            return new ApiResponseTransferYearStatusFailed(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(TransferYearStatusFailedResponse::from).collect(Collectors.toList()));
        }
    }
}
