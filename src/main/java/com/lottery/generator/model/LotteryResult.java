package com.lottery.generator.model;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class LotteryResult {

    private Instant date;
    private List<Integer> basisNumbers;
    private List<Integer> additionallyNumbers;
}
