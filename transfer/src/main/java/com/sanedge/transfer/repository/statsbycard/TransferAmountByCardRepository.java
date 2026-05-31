package com.sanedge.transfer.repository.statsbycard;

import java.util.List;

import com.sanedge.transfer.entity.Transfer;
import com.sanedge.transfer.entity.TransferMonthAmount;
import com.sanedge.transfer.entity.TransferYearAmount;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransferAmountByCardRepository implements PanacheRepository<Transfer> {

    public Uni<List<TransferMonthAmount>> findMonthlyTransferAmountsBySender(String transferFrom, Long year) {
        String sql = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            )
            SELECT
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar'
                    WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun'
                    WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep'
                    WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN (
                transfers t
                JOIN cards c ON t.transfer_from = c.card_number AND c.deleted_at IS NULL AND c.card_number = :transferFrom
            ) ON t.transfer_time >= MAKE_DATE(:year::int, m.m, 1)
              AND t.transfer_time < MAKE_DATE(:year::int, m.m, 1) + interval '1 month'
              AND t.deleted_at IS NULL
            GROUP BY m.m
            ORDER BY m.m
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("transferFrom", transferFrom)
                .setParameter("year", year)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new TransferMonthAmount(
                        (String) row[0],
                        row[1] == null ? 0L : ((Number) row[1]).longValue()
                    ))
                    .toList()
                )
        );
    }

    public Uni<List<TransferYearAmount>> findYearlyTransferAmountsBySender(String transferFrom, Long endYear) {
        String sql = """
            WITH yearly AS (
                SELECT :endYear AS reportYear UNION ALL
                SELECT :endYear - 1 UNION ALL
                SELECT :endYear - 2 UNION ALL
                SELECT :endYear - 3 UNION ALL
                SELECT :endYear - 4
            )
            SELECT
                CAST(y.reportYear AS CHAR(4)) AS reportYear,
                CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
            FROM yearly y
            LEFT JOIN (
                transfers t
                JOIN cards c ON t.transfer_from = c.card_number AND c.deleted_at IS NULL AND c.card_number = :transferFrom
            ) ON t.transfer_time >= MAKE_TIMESTAMP(y.reportYear::int, 1, 1, 0, 0, 0)
              AND t.transfer_time < MAKE_TIMESTAMP((y.reportYear + 1)::int, 1, 1, 0, 0, 0)
              AND t.deleted_at IS NULL
            GROUP BY y.reportYear
            ORDER BY y.reportYear
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("transferFrom", transferFrom)
                .setParameter("endYear", endYear)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new TransferYearAmount(
                        row[0] != null ? row[0].toString().trim() : null,
                        row[1] == null ? 0L : ((Number) row[1]).longValue()
                    ))
                    .toList()
                )
        );
    }

    public Uni<List<TransferMonthAmount>> findMonthlyTransferAmountsByReceiver(String transferTo, Long year) {
        String sql = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            )
            SELECT
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar'
                    WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun'
                    WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep'
                    WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN (
                transfers t
                JOIN cards c ON t.transfer_to = c.card_number AND c.deleted_at IS NULL AND c.card_number = :transferTo
            ) ON t.transfer_time >= MAKE_DATE(:year::int, m.m, 1)
              AND t.transfer_time < MAKE_DATE(:year::int, m.m, 1) + interval '1 month'
              AND t.deleted_at IS NULL
            GROUP BY m.m
            ORDER BY m.m
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("transferTo", transferTo)
                .setParameter("year", year)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new TransferMonthAmount(
                        (String) row[0],
                        row[1] == null ? 0L : ((Number) row[1]).longValue()
                    ))
                    .toList()
                )
        );
    }

    public Uni<List<TransferYearAmount>> findYearlyTransferAmountsByReceiver(String transferTo, Long endYear) {
        String sql = """
            WITH yearly AS (
                SELECT :endYear AS reportYear UNION ALL
                SELECT :endYear - 1 UNION ALL
                SELECT :endYear - 2 UNION ALL
                SELECT :endYear - 3 UNION ALL
                SELECT :endYear - 4
            )
            SELECT
                CAST(y.reportYear AS CHAR(4)) AS reportYear,
                CAST(COALESCE(SUM(t.transfer_amount), 0) AS BIGINT) AS totalAmount
            FROM yearly y
            LEFT JOIN (
                transfers t
                JOIN cards c ON t.transfer_to = c.card_number AND c.deleted_at IS NULL AND c.card_number = :transferTo
            ) ON t.transfer_time >= MAKE_TIMESTAMP(y.reportYear::int, 1, 1, 0, 0, 0)
              AND t.transfer_time < MAKE_TIMESTAMP((y.reportYear + 1)::int, 1, 1, 0, 0, 0)
              AND t.deleted_at IS NULL
            GROUP BY y.reportYear
            ORDER BY y.reportYear
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("transferTo", transferTo)
                .setParameter("endYear", endYear)
                .getResultList()
                .map(list -> list.stream()
                    .map(row -> new TransferYearAmount(
                        row[0] != null ? row[0].toString().trim() : null,
                        row[1] == null ? 0L : ((Number) row[1]).longValue()
                    ))
                    .toList()
                )
        );
    }
}
