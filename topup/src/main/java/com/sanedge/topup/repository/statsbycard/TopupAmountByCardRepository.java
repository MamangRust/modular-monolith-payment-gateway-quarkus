package com.sanedge.topup.repository.statsbycard;

import java.util.List;

import com.sanedge.topup.entity.Topup;
import com.sanedge.topup.entity.TopupMonthAmount;
import com.sanedge.topup.entity.TopupYearAmount;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TopupAmountByCardRepository implements PanacheRepository<Topup> {

    public Uni<List<TopupMonthAmount>> findMonthlyTopupAmountsByCard(String cardNumber, Long year) {
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
                CAST(COALESCE(SUM(t.topup_amount), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN (
                topups t
                JOIN cards c ON t.card_number = c.card_number AND c.deleted_at IS NULL AND c.card_number = :cardNumber
            ) ON t.topup_time >= MAKE_DATE(:year::int, m.m, 1)
              AND t.topup_time < MAKE_DATE(:year::int, m.m, 1) + interval '1 month'
              AND t.deleted_at IS NULL
            GROUP BY m.m
            ORDER BY m.m
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("cardNumber", cardNumber)
                .setParameter("year", year)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new TopupMonthAmount(
                        (String) row[0],
                        row[1] == null ? 0L : ((Number) row[1]).longValue()
                    ))
                    .toList()
                )
        );
    }

    public Uni<List<TopupYearAmount>> findYearlyTopupAmountsByCard(String cardNumber, Long endYear) {
        String sql = """
            WITH years AS (
                SELECT :endYear - 4 AS y UNION ALL
                SELECT :endYear - 3 AS y UNION ALL
                SELECT :endYear - 2 AS y UNION ALL
                SELECT :endYear - 1 AS y UNION ALL
                SELECT :endYear AS y
            )
            SELECT
                CAST(y.y AS VARCHAR) AS reportYear,
                CAST(COALESCE(SUM(t.topup_amount), 0) AS BIGINT) AS totalAmount
            FROM years y
            LEFT JOIN (
                topups t
                JOIN cards c ON t.card_number = c.card_number AND c.deleted_at IS NULL AND c.card_number = :cardNumber
            ) ON t.topup_time >= MAKE_TIMESTAMP(y.y::int, 1, 1, 0, 0, 0)
              AND t.topup_time < MAKE_TIMESTAMP((y.y + 1)::int, 1, 1, 0, 0, 0)
              AND t.deleted_at IS NULL
            GROUP BY y.y
            ORDER BY y.y
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("cardNumber", cardNumber)
                .setParameter("endYear", endYear)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new TopupYearAmount(
                        row[0] != null ? row[0].toString().trim() : null,
                        row[1] == null ? 0L : ((Number) row[1]).longValue()
                    ))
                    .toList()
                )
        );
    }
}
