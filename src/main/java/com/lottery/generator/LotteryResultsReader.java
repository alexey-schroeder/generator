package com.lottery.generator;

import com.lottery.generator.model.LotteryResult;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class LotteryResultsReader {

    private String resultsFileName = "eurojackpot_archiv.csv";

    private LotteryResultMapper lotteryResultMapper;

    public LotteryResultsReader(LotteryResultMapper lotteryResultMapper) {
        this.lotteryResultMapper = lotteryResultMapper;
    }

    public List<LotteryResult> readLotteryResults(){
        return Collections.emptyList();
    }
}
