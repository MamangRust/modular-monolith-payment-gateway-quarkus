package com.sanedge.gateway.dto;

import java.util.List;
import java.util.stream.Collectors;

public class SaldoDto {

    public record CreateRequest(
            String cardNumber,
            int totalBalance) {
    }

    public record UpdateRequest(
            int id,
            String cardNumber,
            int totalBalance) {
    }

    public record SaldoResponse(
            int saldoId,
            String cardNumber,
            int totalBalance,
            String withdrawTime,
            int withdrawAmount,
            String createdAt,
            String updatedAt) {
        public static SaldoResponse from(pb.saldo.Saldo.SaldoResponse proto) {
            return new SaldoResponse(
                    proto.getSaldoId(),
                    proto.getCardNumber(),
                    proto.getTotalBalance(),
                    proto.getWithdrawTime(),
                    proto.getWithdrawAmount(),
                    proto.getCreatedAt(),
                    proto.getUpdatedAt());
        }
    }

    public record SaldoResponseDeleteAt(
            int saldoId,
            String cardNumber,
            int totalBalance,
            String withdrawTime,
            int withdrawAmount,
            String createdAt,
            String updatedAt,
            String deletedAt) {
        public static SaldoResponseDeleteAt from(pb.saldo.Saldo.SaldoResponseDeleteAt proto) {
            return new SaldoResponseDeleteAt(
                    proto.getSaldoId(),
                    proto.getCardNumber(),
                    proto.getTotalBalance(),
                    proto.getWithdrawTime(),
                    proto.getWithdrawAmount(),
                    proto.getCreatedAt(),
                    proto.getUpdatedAt(),
                    proto.hasDeletedAt() ? proto.getDeletedAt().getValue() : null);
        }
    }

    public record ApiResponseSaldo(
            String status,
            String message,
            SaldoResponse data) {
        public static ApiResponseSaldo from(pb.saldo.Saldo.ApiResponseSaldo proto) {
            return new ApiResponseSaldo(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.hasData() ? SaldoResponse.from(proto.getData()) : null);
        }
    }

    public record ApiResponseSaldoDeleteAt(
            String status,
            String message,
            SaldoResponseDeleteAt data) {
        public static ApiResponseSaldoDeleteAt from(pb.saldo.Saldo.ApiResponseSaldoDeleteAt proto) {
            return new ApiResponseSaldoDeleteAt(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.hasData() ? SaldoResponseDeleteAt.from(proto.getData()) : null);
        }
    }

    public record SaldoMonthBalanceResponse(
            String month,
            int totalBalance) {
        public static SaldoMonthBalanceResponse from(pb.saldo.stats.SaldoStatsBalance.SaldoMonthBalanceResponse proto) {
            return new SaldoMonthBalanceResponse(proto.getMonth(), proto.getTotalBalance());
        }
    }

    public record SaldoYearBalanceResponse(
            String year,
            int totalBalance) {
        public static SaldoYearBalanceResponse from(pb.saldo.stats.SaldoStatsBalance.SaldoYearBalanceResponse proto) {
            return new SaldoYearBalanceResponse(proto.getYear(), proto.getTotalBalance());
        }
    }

    public record ApiResponseMonthSaldoBalances(
            String status,
            String message,
            List<SaldoMonthBalanceResponse> data) {
        public static ApiResponseMonthSaldoBalances from(pb.saldo.stats.SaldoStatsBalance.ApiResponseMonthSaldoBalances proto) {
            return new ApiResponseMonthSaldoBalances(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(SaldoMonthBalanceResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseYearSaldoBalances(
            String status,
            String message,
            List<SaldoYearBalanceResponse> data) {
        public static ApiResponseYearSaldoBalances from(pb.saldo.stats.SaldoStatsBalance.ApiResponseYearSaldoBalances proto) {
            return new ApiResponseYearSaldoBalances(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(SaldoYearBalanceResponse::from).collect(Collectors.toList()));
        }
    }

    public record SaldoMonthTotalBalanceResponse(
            String month,
            String year,
            int totalBalance) {
        public static SaldoMonthTotalBalanceResponse from(pb.saldo.stats.SaldoStatsTotal.SaldoMonthTotalBalanceResponse proto) {
            return new SaldoMonthTotalBalanceResponse(proto.getMonth(), proto.getYear(), proto.getTotalBalance());
        }
    }

    public record SaldoYearTotalBalanceResponse(
            String year,
            int totalBalance) {
        public static SaldoYearTotalBalanceResponse from(pb.saldo.stats.SaldoStatsTotal.SaldoYearTotalBalanceResponse proto) {
            return new SaldoYearTotalBalanceResponse(proto.getYear(), proto.getTotalBalance());
        }
    }

    public record ApiResponseMonthTotalSaldo(
            String status,
            String message,
            List<SaldoMonthTotalBalanceResponse> data) {
        public static ApiResponseMonthTotalSaldo from(pb.saldo.stats.SaldoStatsTotal.ApiResponseMonthTotalSaldo proto) {
            return new ApiResponseMonthTotalSaldo(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(SaldoMonthTotalBalanceResponse::from).collect(Collectors.toList()));
        }
    }

    public record ApiResponseYearTotalSaldo(
            String status,
            String message,
            List<SaldoYearTotalBalanceResponse> data) {
        public static ApiResponseYearTotalSaldo from(pb.saldo.stats.SaldoStatsTotal.ApiResponseYearTotalSaldo proto) {
            return new ApiResponseYearTotalSaldo(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(SaldoYearTotalBalanceResponse::from).collect(Collectors.toList()));
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

    public record ApiResponsePaginationSaldo(
            String status,
            String message,
            List<SaldoResponse> data,
            PaginationMeta paginationMeta) {
        public static ApiResponsePaginationSaldo from(pb.saldo.SaldoQuery.ApiResponsePaginationSaldo proto) {
            return new ApiResponsePaginationSaldo(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(SaldoResponse::from).collect(Collectors.toList()),
                    proto.hasPaginationMeta() ? PaginationMeta.from(proto.getPaginationMeta()) : null);
        }
    }

    public record ApiResponsePaginationSaldoDeleteAt(
            String status,
            String message,
            List<SaldoResponseDeleteAt> data,
            PaginationMeta paginationMeta) {
        public static ApiResponsePaginationSaldoDeleteAt from(pb.saldo.SaldoQuery.ApiResponsePaginationSaldoDeleteAt proto) {
            return new ApiResponsePaginationSaldoDeleteAt(
                    proto.getStatus(),
                    proto.getMessage(),
                    proto.getDataList().stream().map(SaldoResponseDeleteAt::from).collect(Collectors.toList()),
                    proto.hasPaginationMeta() ? PaginationMeta.from(proto.getPaginationMeta()) : null);
        }
    }

    public record SimpleResponse(
            String status,
            String message) {
        public static SimpleResponse from(pb.saldo.SaldoCommand.ApiResponseSaldoDelete proto) {
            return new SimpleResponse(proto.getStatus(), proto.getMessage());
        }
        public static SimpleResponse from(pb.saldo.SaldoCommand.ApiResponseSaldoAll proto) {
            return new SimpleResponse(proto.getStatus(), proto.getMessage());
        }
    }
}
