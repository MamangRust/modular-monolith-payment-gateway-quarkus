package com.sanedge.transfer.repository.statsbycard;

import java.util.List;

import com.sanedge.transfer.domain.requests.statsbycard.MonthStatusTransferCardNumber;
import com.sanedge.transfer.entity.Transfer;
import com.sanedge.transfer.entity.TransferMonthStatusFailed;
import com.sanedge.transfer.entity.TransferMonthStatusSuccess;
import com.sanedge.transfer.entity.TransferYearStatusFailed;
import com.sanedge.transfer.entity.TransferYearStatusSuccess;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransferStatusByCardRepository implements PanacheRepository<Transfer> {

    public Uni<List<TransferMonthStatusSuccess>> findMonthTransferStatusSuccessByCard(
            MonthStatusTransferCardNumber req) {
        String cardNumber = req.getCardNumber();
        Long startYear = (long) req.getYear();
        Integer startMonth = req.getMonth();
        Long endYear = (long) req.getYear();
        Integer endMonth = req.getMonth();

        String sql = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            )
            SELECT
                CAST(:startYear AS CHAR(4)) AS reportYear,
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar'
                    WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun'
                    WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep'
                    WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'SUCCESS' THEN 1 ELSE 0 END), 0) AS BIGINT) AS totalSuccess,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'SUCCESS' THEN t.transfer_amount ELSE 0 END), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN (
                transfers t
                JOIN cards c ON (t.transfer_from = c.card_number OR t.transfer_to = c.card_number) AND c.deleted_at IS NULL AND c.card_number = :cardNumber
            ) ON t.transfer_time >= MAKE_DATE(:startYear::int, m.m, 1)
              AND t.transfer_time < MAKE_DATE(:startYear::int, m.m, 1) + interval '1 month'
              AND t.deleted_at IS NULL
            GROUP BY m.m
            ORDER BY m.m
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("cardNumber", cardNumber)
                .setParameter("startYear", startYear)
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

    public Uni<List<TransferMonthStatusFailed>> findMonthTransferStatusFailedByCard(
            MonthStatusTransferCardNumber req) {
        String cardNumber = req.getCardNumber();
        Long startYear = (long) req.getYear();
        Integer startMonth = req.getMonth();
        Long endYear = (long) req.getYear();
        Integer endMonth = req.getMonth();

        String sql = """
            WITH months AS (
                SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
                SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            )
            SELECT
                CAST(:startYear AS CHAR(4)) AS reportYear,
                CASE m.m
                    WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar'
                    WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun'
                    WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep'
                    WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
                END AS monthName,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'FAILED' THEN 1 ELSE 0 END), 0) AS BIGINT) AS totalFailed,
                CAST(COALESCE(SUM(CASE WHEN t.status = 'FAILED' THEN t.transfer_amount ELSE 0 END), 0) AS BIGINT) AS totalAmount
            FROM months m
            LEFT JOIN (
                transfers t
                JOIN cards c ON (t.transfer_from = c.card_number OR t.transfer_to = c.card_number) AND c.deleted_at IS NULL AND c.card_number = :cardNumber
            ) ON t.transfer_time >= MAKE_DATE(:startYear::int, m.m, 1)
              AND t.transfer_time < MAKE_DATE(:startYear::int, m.m, 1) + interval '1 month'
              AND t.deleted_at IS NULL
            GROUP BY m.m
            ORDER BY m.m
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("cardNumber", cardNumber)
                .setParameter("startYear", startYear)
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

    public Uni<List<TransferYearStatusSuccess>> findYearlyTransferStatusSuccessByCard(
            String cardNumber, Long year) {
        String sql = """
            WITH yearly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM t.transfer_time) AS CHAR(4)) AS reportYear,
                    COUNT(*) AS total_success,
                    COALESCE(SUM(t.transfer_amount), 0) AS totalAmount
                FROM transfers t
                JOIN cards c ON (t.transfer_from = c.card_number OR t.transfer_to = c.card_number) AND c.deleted_at IS NULL AND c.card_number = :cardNumber
                WHERE t.deleted_at IS NULL
                  AND t.status = 'SUCCESS'
                  AND (
                      (t.transfer_time >= MAKE_TIMESTAMP(:year::int, 1, 1, 0, 0, 0) AND t.transfer_time < MAKE_TIMESTAMP((:year + 1)::int, 1, 1, 0, 0, 0))
                      OR
                      (t.transfer_time >= MAKE_TIMESTAMP((:year - 1)::int, 1, 1, 0, 0, 0) AND t.transfer_time < MAKE_TIMESTAMP(:year::int, 1, 1, 0, 0, 0))
                  )
                GROUP BY EXTRACT(YEAR FROM t.transfer_time)
            ), formatted_data AS (
                SELECT reportYear, CAST(total_success AS BIGINT) AS totalSuccess, CAST(totalAmount AS BIGINT) AS totalAmount FROM yearly_data
                UNION ALL
                SELECT CAST(:year AS CHAR(4)), 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE reportYear = CAST(:year AS CHAR(4)))
                UNION ALL
                SELECT CAST(:year - 1 AS CHAR(4)), 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE reportYear = CAST(:year - 1 AS CHAR(4)))
            )
            SELECT * FROM formatted_data
            ORDER BY reportYear DESC
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("cardNumber", cardNumber)
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

    public Uni<List<TransferYearStatusFailed>> findYearlyTransferStatusFailedByCard(
            String cardNumber, Long year) {
        String sql = """
            WITH yearly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM t.transfer_time) AS CHAR(4)) AS reportYear,
                    COUNT(*) AS total_failed,
                    COALESCE(SUM(t.transfer_amount), 0) AS totalAmount
                FROM transfers t
                JOIN cards c ON (t.transfer_from = c.card_number OR t.transfer_to = c.card_number) AND c.deleted_at IS NULL AND c.card_number = :cardNumber
                WHERE t.deleted_at IS NULL
                  AND t.status = 'FAILED'
                  AND (
                      (t.transfer_time >= MAKE_TIMESTAMP(:year::int, 1, 1, 0, 0, 0) AND t.transfer_time < MAKE_TIMESTAMP((:year + 1)::int, 1, 1, 0, 0, 0))
                      OR
                      (t.transfer_time >= MAKE_TIMESTAMP((:year - 1)::int, 1, 1, 0, 0, 0) AND t.transfer_time < MAKE_TIMESTAMP(:year::int, 1, 1, 0, 0, 0))
                  )
                GROUP BY EXTRACT(YEAR FROM t.transfer_time)
            ), formatted_data AS (
                SELECT reportYear, CAST(total_failed AS BIGINT) AS totalFailed, CAST(totalAmount AS BIGINT) AS totalAmount FROM yearly_data
                UNION ALL
                SELECT CAST(:year AS CHAR(4)), 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE reportYear = CAST(:year AS CHAR(4)))
                UNION ALL
                SELECT CAST(:year - 1 AS CHAR(4)), 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE reportYear = CAST(:year - 1 AS CHAR(4)))
            )
            SELECT * FROM formatted_data
            ORDER BY reportYear DESC
            """;

        return getSession().chain(session ->
            session.createNativeQuery(sql, Object[].class)
                .setParameter("cardNumber", cardNumber)
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
