package com.lottery.generator.filter;

import com.lottery.generator.model.LotteryResult;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@ToString
@Getter
public class PredictedIndexesFilterResult {

    private String filterName;
    private List<LotteryResult> oldResults;
    private List<Integer> predictedIndexes;
    private Boolean result; //not boolean(simple type), because it will be used for check if the theory set the value or it was forget
    private String reason;
}
