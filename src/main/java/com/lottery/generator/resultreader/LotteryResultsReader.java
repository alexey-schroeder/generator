package com.lottery.generator.resultreader;

import com.lottery.generator.model.LotteryResult;
import com.lottery.generator.resultmapper.LotteryResultMapper;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class LotteryResultsReader {
    private final ResourceLoader resourceLoader;
    private final LotteryResultMapper lotteryResultMapper;

    public LotteryResultsReader(LotteryResultMapper lotteryResultMapper, ResourceLoader resourceLoader) {
        this.lotteryResultMapper = lotteryResultMapper;
        this.resourceLoader = resourceLoader;
    }

    public ArrayList<LotteryResult> readLotteryResults() throws IOException, ParseException {

        List<String> lines = readLinesFromFile();
        ArrayList<LotteryResult> lotteryResults = new ArrayList<>(lines.size());
        for (String line : lines) {
            LotteryResult lotteryResult = lotteryResultMapper.lineToLotteryResult(line);
            lotteryResults.add(lotteryResult);
        }

        lotteryResults.sort(Comparator.comparing(LotteryResult::getDate).reversed());

        return lotteryResults;
    }

    public List<String> readLinesFromFile() throws IOException {
        File resultsFile = resourceLoader.getResource(getResultsFileName()).getFile();
        return Files.readAllLines(resultsFile.toPath(), Charset.defaultCharset());
    }

    protected abstract String getResultsFileName();
}
