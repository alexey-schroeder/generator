package com.lottery.generator.predictor;

import com.lottery.generator.ActualStatistic;
import com.lottery.generator.category.Category;
import com.lottery.generator.category.MillionDayItalyCategories;
import com.lottery.generator.filter.MillionDayItalyPredictedIndexesFilter;
import com.lottery.generator.filter.PredictedIndexesFilterResult;
import com.lottery.generator.model.LotteryResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
@Slf4j
public class MillionDayLotteryPredictor {

    private ActualStatistic actualStatistic;
    private List<MillionDayItalyPredictedIndexesFilter> filters;
    private MillionDayItalyCategories millionDayItalyCategories;
    private CategoryPredictorFactory categoryPredictorFactory;

    public List<Integer> predictBasisNumbers(List<LotteryResult> oldLotteryResults) {
        List<List<Integer>> result = calculateIndexes(oldLotteryResults);


        return List.of();
    }

    public List<Integer> predict(List<LotteryResult> lotteryResults) {
        List<List<Integer>> predictedIndexes = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            predictedIndexes.add(predictBasisNumbers(lotteryResults));
        }

        Map<Integer, Integer>[] indexCountMaps = new Map[lotteryResults.get(0).getBasisNumbers().size()];
        for (int i = 0; i < indexCountMaps.length; i++) {
            indexCountMaps[i] = new HashMap();
        }

        for (List<Integer> predictedIndex : predictedIndexes) {
            for (int i = 0; i < predictedIndex.size(); i++) {
                Integer index = predictedIndex.get(i);
                Map<Integer, Integer> integerIntegerMap = indexCountMaps[i];
                integerIntegerMap.merge(index, 1, (integer, integer2) -> integer + integer2);
            }
        }

        List<Integer> bestIndexes = new ArrayList<>();
        for (int i = 0; i < indexCountMaps.length; i++) {
            Map<Integer, Integer> indexCountMap = indexCountMaps[i];
            Integer bestIndex = null;
            Integer bestCount = null;
            for (Map.Entry<Integer, Integer> map : indexCountMap.entrySet()) {
                if (bestIndex == null) {
                    bestIndex = map.getKey();
                    bestCount = map.getValue();
                } else {
                    Integer count = map.getValue();
                    if (count > bestCount) {
                        bestIndex = map.getKey();
                        bestCount = map.getValue();
                    }
                }
            }

            bestIndexes.add(bestIndex);
        }
        return bestIndexes;
    }

    private boolean areIndexesValid(List<LotteryResult> oldLotteryResults, List<Integer> result) {
        for (MillionDayItalyPredictedIndexesFilter filter : filters) {
            PredictedIndexesFilterResult filterResult = filter.filter(oldLotteryResults, result);
            if (!filterResult.getResult()) {
                log.info(MessageFormat.format("Predicted indexes {0} are not valid. Reason: {1}, Filter: {2}", result, filterResult.getReason(), filter.getClass().getSimpleName()));
                return false;
            }
        }
        return true;
    }

    private List<List<Integer>> calculateIndexes(List<LotteryResult> oldLotteryResults) {
//        List<Integer> result = new ArrayList<>(5);
//        for (Category category : categories.getAllCategories()) {
//            CategoryPredictor categoryPredictor = categoryPredictorFactory.categoryPredictor(category, oldLotteryResults);
//            int predictedIndex = categoryPredictor.predictNextIndexInCategory(oldLotteryResults);
//            result.add(category.getIndexInBasisNumbers(), predictedIndex);
//        }
//        return result;

        List<Map.Entry<String, Integer>> map = actualStatistic.calculateStatisticZeroPositionsInResult(oldLotteryResults, millionDayItalyCategories, 1.0);

        int basePositionWithZero = actualStatistic.getBasePositionWithZero(oldLotteryResults, millionDayItalyCategories);

        List<List<Integer>> bestPlaces = actualStatistic.calculateMinimalListWithZerosByThreshold(map, basePositionWithZero, 0.5 * oldLotteryResults.size());

        List<List<Integer>> results = new ArrayList<>();
        for (List<Integer> bestPlace : bestPlaces) {
            List<Integer> predictedIndexes = predictIndexes(oldLotteryResults, bestPlace);
            while (!areIndexesValid(oldLotteryResults, predictedIndexes)) {
                predictedIndexes = predictIndexes(oldLotteryResults, bestPlace);
            }
            results.add(predictedIndexes);
        }
        return results;
    }

    private List<Integer> predictIndexes(List<LotteryResult> oldLotteryResults, List<Integer> bestPlace) {
        List<Integer> predictedIndexes = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            if (bestPlace.contains(i)) {
                predictedIndexes.add(Integer.valueOf(0));
            } else {
                Category category = millionDayItalyCategories.getCategoryByIndexInBasisNumbers(i);
                CategoryPredictor categoryPredictor = categoryPredictorFactory.categoryPredictor(category, oldLotteryResults);
                int predictedIndex = 0;
                while (predictedIndex == 0) {
                    predictedIndex = categoryPredictor.predictNextIndexInCategory(oldLotteryResults);
                }
                predictedIndexes.add(predictedIndex);
            }
        }
        return predictedIndexes;
    }
}
