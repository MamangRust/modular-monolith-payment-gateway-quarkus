package com.sanedge.transfer.repository.stats;

import java.util.List;

import com.sanedge.transfer.entity.Transfer;
import com.sanedge.transfer.entity.TransferMonthStatusFailed;
import com.sanedge.transfer.entity.TransferMonthStatusSuccess;
import com.sanedge.transfer.entity.TransferYearStatusFailed;
import com.sanedge.transfer.entity.TransferYearStatusSuccess;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransferStatusRepository implements PanacheRepository<Transfer> {

    public Uni<List<TransferMonthStatusSuccess>> findMonthTransferStatusSuccess(
            Long startYear, Integer startMonth, Long endYear, Integer endMonth) {
        String sql = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            )
            SELECT
                CAST(:startYear AS VARCHAR) AS reportYear,
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar' WHEN 4 THEN 'Apr'
                    WHEN 5 THEN 'May' WHEN 6 THEN 'Jun' WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug'
                    WHEN 9 THEN 'Sep' WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'SUCCESS' THEN 1 ELSE 0 END), 0) AS BIGINT) AS totalSuccess,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'SUCCESS' THEN t.transfer_amount ELSE 0 END), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN (
                transfers t
                JOIN cards c ON t.transfer_from = c.card_number AND c.deleted_at IS NULL
            ) ON t.transfer_time >= MAKE_DATE(:startYear::int, m.m, 1)
              AND t.transfer_time < MAKE_DATE(:startYear::int, m.m, 1) + interval '1 month'
              AND t.deleted_at IS NULL
            WHERE m.m >= :startMonth AND m.m <= :endMonth
            GROUP BY m.m
            ORDER BY m.m
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("startYear", startYear)
                .setParameter("startMonth", startMonth)
                .setParameter("endMonth", endMonth)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new TransferMonthStatusSuccess(
                        row[0] != null ? row[0].toString().trim() : null,
                        (String) row[1],
                        row[2] == null ? 0 : ((Number) row[2]).intValue(),
                        row[3] == null ? 0L : ((Number) row[3]).longValue()
                    ))
                    .toList()
                )
        );
    }

    public Uni<List<TransferMonthStatusFailed>> findMonthTransferStatusFailed(
            Long startYear, Integer startMonth, Long endYear, Integer endMonth) {
        String sql = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            )
            SELECT
                CAST(:startYear AS VARCHAR) AS reportYear,
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar' WHEN 4 THEN 'Apr'
                    WHEN 5 THEN 'May' WHEN 6 THEN 'Jun' WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug'
                    WHEN 9 THEN 'Sep' WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'FAILED' THEN 1 ELSE 0 END), 0) AS BIGINT) AS totalFailed,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'FAILED' THEN t.transfer_amount ELSE 0 END), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN (
                transfers t
                JOIN cards c ON t.transfer_from = c.card_number AND c.deleted_at IS NULL
            ) ON t.transfer_time >= MAKE_DATE(:startYear::int, m.m, 1)
              AND t.transfer_time < MAKE_DATE(:startYear::int, m.m, 1) + interval '1 month'
              AND t.deleted_at IS NULL
            WHERE m.m >= :startMonth AND m.m <= :endMonth
            GROUP BY m.m
            ORDER BY m.m
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("startYear", startYear)
                .setParameter("startMonth", startMonth)
                .setParameter("endMonth", endMonth)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new TransferMonthStatusFailed(
                        row[0] != null ? row[0].toString().trim() : null,
                        (String) row[1],
                        row[2] == null ? 0 : ((Number) row[2]).intValue(),
                        row[3] == null ? 0L : ((Number) row[3]).longValue()
                    ))
                    .toList()
                )
        );
    }

    public Uni<List<TransferYearStatusSuccess>> findYearlyTransferStatusSuccess(Long year) {
        String sql = """
            WITH years AS (
                SELECT CAST(:year AS INTEGER) AS y UNION ALL SELECT CAST(:year AS INTEGER) - 1
            )
            SELECT
                CAST(y.y AS VARCHAR) AS reportYear,
                CAST(COALESCE(COUNT(t.transfer_id), 0) AS BIGINT) AS totalSuccess,
                CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
            FROM years y
            LEFT JOIN (
                transfers t
                JOIN cards c ON t.transfer_from = c.card_number AND c.deleted_at IS NULL
            ) ON t.transfer_time >= MAKE_TIMESTAMP(y.y::int, 1, 1, 0, 0, 0)
              AND t.transfer_time < MAKE_TIMESTAMP((y.y + 1)::int, 1, 1, 0, 0, 0)
              AND t.status = 'SUCCESS'
              AND t.deleted_at IS NULL
            GROUP BY y.y
            ORDER BY y.y DESC
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("year", year)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new TransferYearStatusSuccess(
                        row[0] != null ? row[0].toString().trim() : null,
                        row[1] == null ? 0 : ((Number) row[1]).intValue(),
                        row[2] == null ? 0L : ((Number) row[2]).longValue()
                    ))
                    .toList()
                )
        );
    }

    public Uni<List<TransferYearStatusFailed>> findYearlyTransferStatusFailed(Long year) {
        String sql = """
            WITH years AS (
                SELECT CAST(:year AS INTEGER) AS y UNION ALL SELECT CAST(:year AS INTEGER) - 1
            )
            SELECT
                CAST(y.y AS VARCHAR) AS reportYear,
                CAST(COALESCE(COUNT(t.transfer_id), 0) AS BIGINT) AS totalFailed,
                CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
            FROM years y
            LEFT JOIN (
                transfers t
                JOIN cards c ON t.transfer_from = c.card_number AND c.deleted_at IS NULL
            ) ON t.transfer_time >= MAKE_TIMESTAMP(y.y::int, 1, 1, 0, 0, 0)
              AND t.transfer_time < MAKE_TIMESTAMP((y.y + 1)::int, 1, 1, 0, 0, 0)
              AND t.status = 'FAILED'
              AND t.deleted_at IS NULL
            GROUP BY y.y
            ORDER BY y.y DESC
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("year", year)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new TransferYearStatusFailed(
                        row[0] != null ? row[0].toString().trim() : null,
                        row[1] == null ? 0 : ((Number) row[1]).intValue(),
                        row[2] == null ? 0L : ((Number) row[2]).longValue()
                    ))
                    .toList()
                )
        );
    }
}
