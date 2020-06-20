package com.lottery.generator.resultreader;

import com.lottery.generator.resultmapper.MillionDayItalyLotteryResultMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class MillionDayItalyLotteryResultsReader extends LotteryResultsReader {

    //http://www.millionday.cloud/archivio-estrazioni.php

    @Getter
    @Value("${milliondayitaly.results.file.name}")
    private String resultsFileName;

    public MillionDayItalyLotteryResultsReader(MillionDayItalyLotteryResultMapper lotteryResultMapper, ResourceLoader resourceLoader) {
        super(lotteryResultMapper, resourceLoader);
    }
}
