package com.lottery.generator.model;

import com.lottery.generator.model.LotteryResult;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Builder
@ToString
public class TheoryResult {
    private String theoryName;
    private List<LotteryResult> oldResults;
    @Getter
    private Boolean result; //not boolean(simple type), because it will be used for check if the theory set the value or it was forget
    private String reason;
}
