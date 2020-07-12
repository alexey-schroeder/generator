package com.lottery.generator.filter;

import com.lottery.generator.category.Category;
import com.lottery.generator.category.MillionDayItalyCategories;
import com.lottery.generator.model.LotteryResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.temporal.ValueRange;
import java.util.List;

@Component
@AllArgsConstructor
public class NotPossiblePredictedLotteryResultFilter implements MillionDayItalyPredictedIndexesFilter {

    private MillionDayItalyCategories categories;

    @Override
    public PredictedIndexesFilterResult filter(List<LotteryResult> lotteryResults, List<Integer> predictedIndexes) {

        for (int i = 0; i < lotteryResults.get(0).getBasisNumbers().size() - 1; i++) {
            Category category = categories.getCategoryByIndexInBasisNumbers(i);
            Category nextCategory = categories.getCategoryByIndexInBasisNumbers(i + 1);
            Integer predictedIndex = predictedIndexes.get(i);
            Integer nextPredictedIndex = predictedIndexes.get(i + 1);
            ValueRange valueRange = category.getIndexes().get(predictedIndex);
            ValueRange nextValueRange = nextCategory.getIndexes().get(nextPredictedIndex);
            if (valueRange.getMinimum() > nextValueRange.getMaximum()) {
                return PredictedIndexesFilterResult.builder().
                        filterName(this.getClass().getSimpleName()).
                        reason(MessageFormat.format("Category {0} has minimum greater than maximum in category {1}.", category.getName(), nextCategory.getName())).
                        predictedIndexes(predictedIndexes).
                        oldResults(lotteryResults).
                        result(false).
                        build();
            }
        }
        return PredictedIndexesFilterResult.builder().
                filterName(this.getClass().getSimpleName()).
                result(true).
                predictedIndexes(predictedIndexes).
                oldResults(lotteryResults).
                build();
    }
}
