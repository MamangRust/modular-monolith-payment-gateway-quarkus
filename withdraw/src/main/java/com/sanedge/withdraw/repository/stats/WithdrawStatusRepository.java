package com.sanedge.withdraw.repository.stats;

import java.util.List;

import com.sanedge.withdraw.entity.Withdraw;
import com.sanedge.withdraw.entity.WithdrawMonthStatusFailed;
import com.sanedge.withdraw.entity.WithdrawMonthStatusSuccess;
import com.sanedge.withdraw.entity.WithdrawYearStatusFailed;
import com.sanedge.withdraw.entity.WithdrawYearStatusSuccess;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WithdrawStatusRepository implements PanacheRepository<Withdraw> {

    public Uni<List<WithdrawMonthStatusSuccess>> findMonthWithdrawStatusSuccess(
            Long startYear, Integer startMonth, Long endYear, Integer endMonth) {
        String sql = """
            SELECT
                CAST(:startYear AS text) AS reportYear,
                m.monthName AS monthName,
                CAST(COALESCE(SUM(CASE WHEN w.status = 'SUCCESS' THEN 1 ELSE 0 END), 0) AS bigint) AS totalSuccess,
                CAST(COALESCE(SUM(CASE WHEN w.status = 'SUCCESS' THEN w.withdraw_amount ELSE 0 END), 0) AS bigint) AS totalAmount
            FROM (
                SELECT 'Jan' AS monthName, 1 AS monthNum UNION ALL
                SELECT 'Feb', 2 UNION ALL
                SELECT 'Mar', 3 UNION ALL
                SELECT 'Apr', 4 UNION ALL
                SELECT 'May', 5 UNION ALL
                SELECT 'Jun', 6 UNION ALL
                SELECT 'Jul', 7 UNION ALL
                SELECT 'Aug', 8 UNION ALL
                SELECT 'Sep', 9 UNION ALL
                SELECT 'Oct', 10 UNION ALL
                SELECT 'Nov', 11 UNION ALL
                SELECT 'Dec', 12
            ) m
            LEFT JOIN (
                withdraws w
                JOIN cards c ON w.card_number = c.card_number AND c.deleted_at IS NULL
            ) ON w.withdraw_time >= MAKE_DATE(:startYear::int, m.monthNum, 1)
                AND w.withdraw_time < MAKE_DATE(:startYear::int, m.monthNum, 1) + interval '1 month'
                AND w.deleted_at IS NULL
            WHERE m.monthNum >= :startMonth AND m.monthNum <= :endMonth
            GROUP BY m.monthName, m.monthNum
            ORDER BY m.monthNum
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("startYear", startYear)
                .setParameter("startMonth", startMonth)
                .setParameter("endMonth", endMonth)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new WithdrawMonthStatusSuccess(
                        row[0] != null ? row[0].toString().trim() : null,
                        (String) row[1],
                        row[2] == null ? 0 : ((Number) row[2]).intValue(),
                        row[3] == null ? 0L : ((Number) row[3]).longValue()
                    ))
                    .toList()
                )
        );
    }

    public Uni<List<WithdrawMonthStatusFailed>> findMonthWithdrawStatusFailed(
            Long startYear, Integer startMonth, Long endYear, Integer endMonth) {
        String sql = """
            SELECT
                CAST(:startYear AS text) AS reportYear,
                m.monthName AS monthName,
                CAST(COALESCE(SUM(CASE WHEN w.status = 'FAILED' THEN 1 ELSE 0 END), 0) AS bigint) AS totalFailed,
                CAST(COALESCE(SUM(CASE WHEN w.status = 'FAILED' THEN w.withdraw_amount ELSE 0 END), 0) AS bigint) AS totalAmount
            FROM (
                SELECT 'Jan' AS monthName, 1 AS monthNum UNION ALL
                SELECT 'Feb', 2 UNION ALL
                SELECT 'Mar', 3 UNION ALL
                SELECT 'Apr', 4 UNION ALL
                SELECT 'May', 5 UNION ALL
                SELECT 'Jun', 6 UNION ALL
                SELECT 'Jul', 7 UNION ALL
                SELECT 'Aug', 8 UNION ALL
                SELECT 'Sep', 9 UNION ALL
                SELECT 'Oct', 10 UNION ALL
                SELECT 'Nov', 11 UNION ALL
                SELECT 'Dec', 12
            ) m
            LEFT JOIN (
                withdraws w
                JOIN cards c ON w.card_number = c.card_number AND c.deleted_at IS NULL
            ) ON w.withdraw_time >= MAKE_DATE(:startYear::int, m.monthNum, 1)
                AND w.withdraw_time < MAKE_DATE(:startYear::int, m.monthNum, 1) + interval '1 month'
                AND w.deleted_at IS NULL
            WHERE m.monthNum >= :startMonth AND m.monthNum <= :endMonth
            GROUP BY m.monthName, m.monthNum
            ORDER BY m.monthNum
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("startYear", startYear)
                .setParameter("startMonth", startMonth)
                .setParameter("endMonth", endMonth)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new WithdrawMonthStatusFailed(
                        row[0] != null ? row[0].toString().trim() : null,
                        (String) row[1],
                        row[2] == null ? 0 : ((Number) row[2]).intValue(),
                        row[3] == null ? 0L : ((Number) row[3]).longValue()
                    ))
                    .toList()
                )
        );
    }

    public Uni<List<WithdrawYearStatusSuccess>> findYearlyWithdrawStatusSuccess(Long year) {
        String sql = """
            WITH years AS (
                SELECT :year AS y UNION ALL SELECT :year - 1 AS y
            )
            SELECT
                CAST(y.y AS VARCHAR) AS reportYear,
                CAST(COALESCE(COUNT(w.withdraw_id), 0) AS BIGINT) AS totalSuccess,
                CAST(COALESCE(SUM(w.withdraw_amount), 0) AS BIGINT) AS totalAmount
            FROM years y
            LEFT JOIN (
                withdraws w
                JOIN cards c ON w.card_number = c.card_number AND c.deleted_at IS NULL
            ) ON w.withdraw_time >= MAKE_TIMESTAMP(y.y::int, 1, 1, 0, 0, 0)
                AND w.withdraw_time < MAKE_TIMESTAMP((y.y + 1)::int, 1, 1, 0, 0, 0)
                AND w.status = 'SUCCESS'
                AND w.deleted_at IS NULL
            GROUP BY y.y
            ORDER BY y.y DESC
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("year", year)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new WithdrawYearStatusSuccess(
                        row[0] != null ? row[0].toString().trim() : null,
                        row[1] == null ? 0 : ((Number) row[1]).intValue(),
                        row[2] == null ? 0L : ((Number) row[2]).longValue()
                    ))
                    .toList()
                )
        );
    }

    public Uni<List<WithdrawYearStatusFailed>> findYearlyWithdrawStatusFailed(Long year) {
        String sql = """
            WITH years AS (
                SELECT :year AS y UNION ALL SELECT :year - 1 AS y
            )
            SELECT
                CAST(y.y AS VARCHAR) AS reportYear,
                CAST(COALESCE(COUNT(w.withdraw_id), 0) AS BIGINT) AS totalFailed,
                CAST(COALESCE(SUM(w.withdraw_amount), 0) AS BIGINT) AS totalAmount
            FROM years y
            LEFT JOIN (
                withdraws w
                JOIN cards c ON w.card_number = c.card_number AND c.deleted_at IS NULL
            ) ON w.withdraw_time >= MAKE_TIMESTAMP(y.y::int, 1, 1, 0, 0, 0)
                AND w.withdraw_time < MAKE_TIMESTAMP((y.y + 1)::int, 1, 1, 0, 0, 0)
                AND w.status = 'FAILED'
                AND w.deleted_at IS NULL
            GROUP BY y.y
            ORDER BY y.y DESC
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("year", year)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new WithdrawYearStatusFailed(
                        row[0] != null ? row[0].toString().trim() : null,
                        row[1] == null ? 0 : ((Number) row[1]).intValue(),
                        row[2] == null ? 0L : ((Number) row[2]).longValue()
                    ))
                    .toList()
                )
        );
    }
}
