package com.lottery.generator.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@ToString
public class CheckResult {
    private TheoryResult  theoryResult;
    @Getter
    private boolean approved;
    private String reason;
}
