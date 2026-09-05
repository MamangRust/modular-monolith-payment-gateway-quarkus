package com.sanedge.topup.repository;

import java.util.List;

import com.sanedge.common.domain.response.PagedResult;
import com.sanedge.topup.domain.requests.FindAllTopups;
import com.sanedge.topup.domain.requests.FindAllTopupsByCardNumber;
import com.sanedge.topup.entity.Topup;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TopupQueryRepository implements PanacheRepository<Topup> {

    public Uni<PagedResult<Topup>> findTopups(FindAllTopups req) {
        String search = req.getSearch();
        int page = (req.getPage() != null && req.getPage() > 0) ? req.getPage() - 1 : 0;
        int size = (req.getPageSize() != null && req.getPageSize() > 0) ? req.getPageSize() : 10;
        var query = """
                    deletedAt IS NULL
                    AND (
                        ?1 IS NULL
                        OR LOWER(cardNumber) LIKE LOWER(CONCAT('%', ?1, '%'))
                        OR LOWER(CAST(topupNo AS string)) LIKE LOWER(CONCAT('%', ?1, '%'))
                        OR LOWER(topupMethod) LIKE LOWER(CONCAT('%', ?1, '%'))
                        OR LOWER(CAST(status AS string)) LIKE LOWER(CONCAT('%', ?1, '%'))
                    )
                """;

        var panacheQuery = find(query, Sort.descending("topupTime"), search)
                .page(page, size);

        return Uni.combine().all().unis(panacheQuery.list(), panacheQuery.count())
                .asTuple()
                .map(tuple -> new PagedResult<>(tuple.getItem1(), tuple.getItem2().intValue()));
    }

    public Uni<PagedResult<Topup>> findTopupByCard(FindAllTopupsByCardNumber req) {
        String cardNumber = req.getCardNumber();
        String search = req.getSearch();
        int page = (req.getPage() != null && req.getPage() > 0) ? req.getPage() - 1 : 0;
        int size = (req.getPageSize() != null && req.getPageSize() > 0) ? req.getPageSize() : 10;
        var query = """
                    deletedAt IS NULL
                    AND cardNumber = ?1
                    AND (
                        ?2 IS NULL
                        OR LOWER(CAST(topupNo AS string)) LIKE LOWER(CONCAT('%', ?2, '%'))
                        OR LOWER(topupMethod) LIKE LOWER(CONCAT('%', ?2, '%'))
                        OR LOWER(CAST(status AS string)) LIKE LOWER(CONCAT('%', ?2, '%'))
                    )
                """;

        var panacheQuery = find(query, Sort.descending("topupTime"), cardNumber, search)
                .page(page, size);

        return Uni.combine().all().unis(panacheQuery.list(), panacheQuery.count())
                .asTuple()
                .map(tuple -> new PagedResult<>(tuple.getItem1(), tuple.getItem2().intValue()));
    }

    public Uni<PagedResult<Topup>> findActiveTopups(FindAllTopups req) {
        return findTopups(req);
    }

    public Uni<PagedResult<Topup>> findTrashedTopups(FindAllTopups req) {
        String search = req.getSearch();
        int page = (req.getPage() != null && req.getPage() > 0) ? req.getPage() - 1 : 0;
        int size = (req.getPageSize() != null && req.getPageSize() > 0) ? req.getPageSize() : 10;
        var query = """
                    deletedAt IS NOT NULL
                    AND (
                        ?1 IS NULL
                        OR LOWER(cardNumber) LIKE LOWER(CONCAT('%', ?1, '%'))
                        OR LOWER(CAST(topupNo AS string)) LIKE LOWER(CONCAT('%', ?1, '%'))
                        OR LOWER(topupMethod) LIKE LOWER(CONCAT('%', ?1, '%'))
                        OR LOWER(CAST(status AS string)) LIKE LOWER(CONCAT('%', ?1, '%'))
                    )
                """;

        var panacheQuery = find(query, Sort.descending("topupTime"), search)
                .page(page, size);

        return Uni.combine().all().unis(panacheQuery.list(), panacheQuery.count())
                .asTuple()
                .map(tuple -> new PagedResult<>(tuple.getItem1(), tuple.getItem2().intValue()));
    }

    public Uni<List<Topup>> findByCardNumber(String cardNumber) {
        return list("cardNumber = ?1 AND deletedAt IS NULL", Sort.descending("topupTime"), cardNumber);
    }

    public Uni<Topup> findTopupById(Long id) {
        return find("topupId = ?1 AND deletedAt IS NULL", id).firstResult();
    }

    public Uni<List<Topup>> findPendingCompensation(int maxAttempts) {
        return find("compensationAttempts < ?1 AND deletedAt IS NULL", maxAttempts).list();
    }
}
