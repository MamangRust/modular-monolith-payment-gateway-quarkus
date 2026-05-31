package com.sanedge.card.domain.response.stats.amount;

import com.sanedge.card.entity.CardYearAmount;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
public class CardResponseYearAmount {
    private String reportYear;
    private Long totalAmount;

    public static CardResponseYearAmount from(CardYearAmount model) {
        if (model == null) {
            return null;
        }
        return CardResponseYearAmount.builder()
                .reportYear(model.getYear())
                .totalAmount(model.getTotalAmount())
                .build();
    }
}
