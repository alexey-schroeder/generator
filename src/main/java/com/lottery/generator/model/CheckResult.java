package com.lottery.generator.model;

import com.lottery.generator.filter.PredictedResultFilter;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Builder
@Data
@ToString
public class CheckResult {
    private PredictedResultFilter resultFilter;
    @Getter
    private boolean approved;
    private String reason;
}
