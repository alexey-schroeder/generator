package com.lottery.generator;

import com.lottery.generator.model.LotteryResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class LotteryResultsReader {

    @Value("${results.file.name}")
    private String resultsFileName;

    private final ResourceLoader resourceLoader;

    private final LotteryResultMapper lotteryResultMapper;

    public LotteryResultsReader(LotteryResultMapper lotteryResultMapper, ResourceLoader resourceLoader) {
        this.lotteryResultMapper = lotteryResultMapper;
        this.resourceLoader = resourceLoader;
    }

    public List<LotteryResult> readLotteryResults() throws IOException, ParseException {

        File resultsFile = resourceLoader.getResource(resultsFileName).getFile();
        List<String> lines = Files.readAllLines(resultsFile.toPath(), Charset.defaultCharset());
        ArrayList<LotteryResult> lotteryResults = new ArrayList<>(lines.size());
        for (String line : lines) {
            LotteryResult lotteryResult = lotteryResultMapper.lineToLotteryResult(line);
            lotteryResults.add(lotteryResult);
        }

        lotteryResults.sort(Comparator.comparing(LotteryResult::getDate).reversed());

        return lotteryResults;
    }
}
