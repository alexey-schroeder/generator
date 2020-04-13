package com.lottery.generator.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class LotteryResult {

    private Instant date;
    private List<Integer> basisNumbers;
    private List<Integer> additionallyNumbers;
}
