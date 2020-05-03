package com.lottery.generator;

import com.lottery.generator.model.LotteryResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

@Component
public class LotteryResultGenerator {

    public LotteryResult generateLotteryResult() {
        ArrayList<Integer> numbersList = new ArrayList(5);
        Random random = new Random();
        while (numbersList.size() != 5) {
            int number = random.nextInt((50 - 1) + 1) + 1;
            if (!numbersList.contains(number)) {
                numbersList.add(number);
            }
        }
        numbersList.sort(Comparator.comparingInt(o -> o));
        return LotteryResult.builder()
                .basisNumbers(numbersList)
                .build();

    }
}
