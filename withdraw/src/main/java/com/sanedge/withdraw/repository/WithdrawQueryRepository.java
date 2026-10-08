package com.sanedge.withdraw.repository;

import java.util.List;

import com.sanedge.common.domain.response.PagedResult;
import com.sanedge.withdraw.domain.requests.FindAllWithdrawCardNumber;
import com.sanedge.withdraw.domain.requests.FindAllWithdraws;
import com.sanedge.withdraw.entity.Withdraw;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WithdrawQueryRepository implements PanacheRepository<Withdraw> {

    public Uni<PagedResult<Withdraw>> findAllWithdraws(FindAllWithdraws req) {
        String keyword = req.getSearch();
        int page = req.getPage();
        int size = req.getPageSize();
        var query = """
                    deletedAt IS NULL
                    AND (
                        ?1 IS NULL
                        OR cardNumber LIKE CONCAT('%', ?1, '%')
                        OR CAST(withdrawAmount AS string) LIKE CONCAT('%', ?1, '%')
                        OR CAST(withdrawTime AS string) LIKE CONCAT('%', ?1, '%')
                        OR LOWER(CAST(status AS string)) LIKE LOWER(CONCAT('%', ?1, '%'))
                    )
                """;

        var panacheQuery = find(query, Sort.descending("withdrawTime"), keyword)
                .page(page, size);

        return Uni.combine().all().unis(panacheQuery.list(), panacheQuery.count())
                .asTuple()
                .map(tuple -> new PagedResult<>(tuple.getItem1(), tuple.getItem2().intValue()));
    }

    public Uni<PagedResult<Withdraw>> findActiveWithdraws(FindAllWithdraws req) {
        return findAllWithdraws(req);
    }

    public Uni<PagedResult<Withdraw>> findTrashedWithdraws(FindAllWithdraws req) {
        String keyword = req.getSearch();
        int page = req.getPage();
        int size = req.getPageSize();
        var query = """
                    deletedAt IS NOT NULL
                    AND (
                        ?1 IS NULL
                        OR cardNumber LIKE CONCAT('%', ?1, '%')
                        OR CAST(withdrawAmount AS string) LIKE CONCAT('%', ?1, '%')
                        OR CAST(withdrawTime AS string) LIKE CONCAT('%', ?1, '%')
                        OR LOWER(CAST(status AS string)) LIKE LOWER(CONCAT('%', ?1, '%'))
                    )
                """;

        var panacheQuery = find(query, Sort.descending("withdrawTime"), keyword)
                .page(page, size);

        return Uni.combine().all().unis(panacheQuery.list(), panacheQuery.count())
                .asTuple()
                .map(tuple -> new PagedResult<>(tuple.getItem1(), tuple.getItem2().intValue()));
    }

    public Uni<List<Withdraw>> findByCardNumber(String cardNumber) {
        return list("deletedAt IS NULL AND cardNumber = ?1", Sort.descending("withdrawTime"), cardNumber);
    }

    public Uni<PagedResult<Withdraw>> findAllByCardNumber(FindAllWithdrawCardNumber req) {
        String cardNumber = req.getCardNumber();
        String keyword = req.getSearch();
        int page = req.getPage();
        int size = req.getPageSize();
        var query = """
                    deletedAt IS NULL
                    AND cardNumber = ?1
                    AND (
                        ?2 IS NULL
                        OR CAST(withdrawAmount AS string) LIKE CONCAT('%', ?2, '%')
                        OR CAST(withdrawTime AS string) LIKE CONCAT('%', ?2, '%')
                        OR LOWER(CAST(status AS string)) LIKE LOWER(CONCAT('%', ?2, '%'))
                    )
                """;

        var panacheQuery = find(query, Sort.descending("withdrawTime"), cardNumber, keyword)
                .page(page, size);

        return Uni.combine().all().unis(panacheQuery.list(), panacheQuery.count())
                .asTuple()
                .map(tuple -> new PagedResult<>(tuple.getItem1(), tuple.getItem2().intValue()));
    }

    public Uni<List<Withdraw>> findPendingCompensation(int maxAttempts) {
        // Candidates are records that have been marked for compensation (attempts >= 1),
        // are not yet terminal (still PENDING), and have not been hard-deleted.
        // Exhausted records are flipped to FAILED by exhaustCompensation, so they are
        // excluded by the status filter regardless of the per-record max attempts.
        return find("compensationAttempts >= 1 AND status = ?1 AND deletedAt IS NULL",
                com.sanedge.common.enums.Status.PENDING).list();
    }
}
