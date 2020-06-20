package com.lottery.generator.resultmapper;

import com.lottery.generator.model.LotteryResult;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EuroJackpotLotteryResultMapper extends LotteryResultMapper {

    @Override
    public LotteryResult lineToLotteryResult(String resultLine) throws ParseException {
        String[] columns = resultLine.split(",");
        if (columns.length != 5) {
            String wrongColumnsSizeMessage =  MessageFormat.format(
                    "The line \"{0}\" can not be parsed. Reason: the line should have five columns", resultLine);
            throw new IllegalArgumentException(wrongColumnsSizeMessage);
        }

        Instant date = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(columns[0]).toInstant();

        String[] basicNumbersArray = columns[1].split("-");
        if (basicNumbersArray.length != 5) {
            String wrongColumnsSizeMessage = MessageFormat.format(
                    "The line \"{0}\" can not be parsed. Reason: the line should have five basis numbers", resultLine);
            throw new IllegalArgumentException(wrongColumnsSizeMessage);
        }

        List<Integer> basicNumberList = Arrays.stream(basicNumbersArray)
                .map(Integer::valueOf)
                .collect(Collectors.toList());

        basicNumberList.sort(Integer::compareTo);

        String[] additionallyNumbersArray = columns[2].split("-");
        if (additionallyNumbersArray.length != 2) {
            String wrongColumnsSizeMessage = MessageFormat.format(
                    "The line \"{0}\" can not be parsed. Reason: the line should have two additionally numbers", resultLine);
            throw new IllegalArgumentException(wrongColumnsSizeMessage);
        }

        List<Integer> additionallyNumberList = Arrays.stream(additionallyNumbersArray).map(Integer::valueOf)
                .collect(Collectors.toList());

        additionallyNumberList.sort(Integer::compareTo);

        return LotteryResult.builder()
                .date(date)
                .basisNumbers(basicNumberList)
                .additionallyNumbers(additionallyNumberList)
                .build();
    }
}
