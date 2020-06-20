package com.lottery.generator.resultmapper;

import com.lottery.generator.model.LotteryResult;

import java.text.ParseException;

public abstract class LotteryResultMapper {
    public abstract LotteryResult lineToLotteryResult(String resultLine) throws ParseException;
}
