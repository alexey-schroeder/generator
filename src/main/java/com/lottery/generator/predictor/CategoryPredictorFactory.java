package com.lottery.generator.predictor;

import com.lottery.generator.category.Category;
import com.lottery.generator.model.LotteryResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryPredictorFactory {

    CategoryPredictor categoryPredictor(Category category, List<LotteryResult> oldLotteryResults){
        return new CategoryPredictor(category, oldLotteryResults);
    }
}
