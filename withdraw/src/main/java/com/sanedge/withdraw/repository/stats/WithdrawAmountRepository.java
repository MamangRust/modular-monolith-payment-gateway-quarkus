package com.sanedge.withdraw.repository.stats;

import java.util.List;

import com.sanedge.withdraw.entity.Withdraw;
import com.sanedge.withdraw.entity.WithdrawMonthlyAmount;
import com.sanedge.withdraw.entity.WithdrawYearlyAmount;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WithdrawAmountRepository implements PanacheRepository<Withdraw> {

    public Uni<List<WithdrawMonthlyAmount>> findMonthlyWithdraws(Long year) {
        String sql = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            )
            SELECT
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar' WHEN 4 THEN 'Apr'
                    WHEN 5 THEN 'May' WHEN 6 THEN 'Jun' WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug'
                    WHEN 9 THEN 'Sep' WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(w.withdraw_amount), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN (
                withdraws w
                JOIN cards c ON w.card_number = c.card_number AND c.deleted_at IS NULL
            ) ON w.withdraw_time >= MAKE_DATE(:year::int, m.m, 1)
             AND w.withdraw_time < MAKE_DATE(:year::int, m.m, 1) + interval '1 month'
             AND w.deleted_at IS NULL
            GROUP BY m.m
            ORDER BY m.m
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("year", year)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new WithdrawMonthlyAmount(
                        (String) row[0],
                        row[1] == null ? 0L : ((Number) row[1]).longValue()
                    ))
                    .toList()
                )
        );
    }

    public Uni<List<WithdrawYearlyAmount>> findYearlyWithdraws(Long year) {
        String sql = """
            WITH years AS (
                SELECT :year - 4 AS y UNION ALL
                SELECT :year - 3 AS y UNION ALL
                SELECT :year - 2 AS y UNION ALL
                SELECT :year - 1 AS y UNION ALL
                SELECT :year AS y
            )
            SELECT
                CAST(y.y AS VARCHAR) AS reportYear,
                CAST(COALESCE(SUM(w.withdraw_amount), 0) AS BIGINT) AS totalAmount
            FROM years y
            LEFT JOIN (
                withdraws w
                JOIN cards c ON w.card_number = c.card_number AND c.deleted_at IS NULL
            ) ON w.withdraw_time >= MAKE_TIMESTAMP(y.y::int, 1, 1, 0, 0, 0)
             AND w.withdraw_time < MAKE_TIMESTAMP((y.y + 1)::int, 1, 1, 0, 0, 0)
             AND w.deleted_at IS NULL
            GROUP BY y.y
            ORDER BY y.y
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("year", year)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new WithdrawYearlyAmount(
                        row[0] != null ? row[0].toString().trim() : null,
                        row[1] == null ? 0L : ((Number) row[1]).longValue()
                    ))
                    .toList()
                )
        );
    }
}
