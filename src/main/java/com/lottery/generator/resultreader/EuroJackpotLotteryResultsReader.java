package com.lottery.generator.resultreader;

import com.lottery.generator.resultmapper.EuroJackpotLotteryResultMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class EuroJackpotLotteryResultsReader extends LotteryResultsReader {

    //https://www.lottozahlenonline.com/eurojackpot/eurojackpot_archiv.csv

    @Getter
    @Value("${eurojackpot.results.file.name}")
    private String resultsFileName;

    public EuroJackpotLotteryResultsReader(EuroJackpotLotteryResultMapper lotteryResultMapper, ResourceLoader resourceLoader) {
        super(lotteryResultMapper, resourceLoader);
    }
}
