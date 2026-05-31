package com.sanedge.card.repository.stats;

import java.time.LocalDate;
import java.util.List;

import com.sanedge.card.entity.Card;
import com.sanedge.card.entity.CardMonthBalance;
import com.sanedge.card.entity.CardYearBalance;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CardBalanceRepository implements PanacheRepository<Card> {

    public Uni<List<CardMonthBalance>> getMonthlyBalances(LocalDate yearDate) {
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
                CAST(COALESCE(SUM(s.total_balance), 0) AS BIGINT) AS totalBalance
            FROM
                months m
            LEFT JOIN (
                saldos s
                JOIN cards c ON s.card_number = c.card_number AND c.deleted_at IS NULL
            ) ON s.created_at >= MAKE_DATE(EXTRACT(YEAR FROM :yearDate)::int, m.m, 1)
              AND s.created_at < MAKE_DATE(EXTRACT(YEAR FROM :yearDate)::int, m.m, 1) + interval '1 month'
              AND s.deleted_at IS NULL
            GROUP BY
                m.m
            ORDER BY
                m.m
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("yearDate", yearDate)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new CardMonthBalance(
                        (String) row[0],
                        row[1] == null ? 0L : ((Number) row[1]).longValue()
                    ))
                    .toList()
                )
        );
    }

    public Uni<List<CardYearBalance>> getYearlyBalances(Long year) {
        String sql = """
            WITH yearly_data AS (
                SELECT
                    EXTRACT(YEAR FROM s.created_at) AS reportYear,
                    COALESCE(SUM(s.total_balance), 0) AS totalBalance
                FROM saldos s
                JOIN cards c ON s.card_number = c.card_number
                WHERE s.deleted_at IS NULL AND c.deleted_at IS NULL
                  AND s.created_at >= MAKE_TIMESTAMP((:year - 4)::int, 1, 1, 0, 0, 0)
                  AND s.created_at < MAKE_TIMESTAMP((:year + 1)::int, 1, 1, 0, 0, 0)
                GROUP BY EXTRACT(YEAR FROM s.created_at)
            ), yearly AS (
                SELECT :year AS reportYear UNION ALL
                SELECT :year - 1 UNION ALL
                SELECT :year - 2 UNION ALL
                SELECT :year - 3 UNION ALL
                SELECT :year - 4
            )
            SELECT
                CAST(y.reportYear AS CHAR(4)) AS reportYear,
                CAST(COALESCE(yd.totalBalance, 0) AS BIGINT) AS totalBalance
            FROM yearly y
            LEFT JOIN yearly_data yd ON y.reportYear = yd.reportYear
            ORDER BY y.reportYear DESC
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("year", year)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new CardYearBalance(
                        row[0] != null ? row[0].toString().trim() : null,
                        row[1] == null ? 0L : ((Number) row[1]).longValue()
                    ))
                    .toList()
                )
        );
    }
}
