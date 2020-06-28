package com.lottery.generator.predictor.milliondayitaly;

import com.lottery.generator.category.Category;
import com.lottery.generator.category.MillionDayItalyCategories;
import com.lottery.generator.predictor.CategoryPredictor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CategoryAPredictor implements CategoryPredictor {

    private MillionDayItalyCategories millionDayItalyCategories;

    @Override
    public Category getCategory() {
        return millionDayItalyCategories.getCategoryA();
    }

    @Override
    public Map<Integer, Map<Integer, Double>> getIndexDepthProbabilities() {
        return null;
    }

    @Override
    public Map<Integer, Map<Integer, Double>> getNextIndexWithProbabilityInCategory() {
        return null;
    }
}
