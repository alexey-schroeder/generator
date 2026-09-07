package com.lottery.generator.resultmapper;

import com.lottery.generator.model.LotteryResult;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EuroJackpotLotteryResultMapper extends LotteryResultMapper {

    @Override
    public LotteryResult lineToLotteryResult(String resultLine) throws ParseException {
        String[] columns = resultLine.split(",");
        if (columns.length != 3 && columns.length != 5) {
            String wrongColumnsSizeMessage = MessageFormat.format(
                    "The line \"{0}\" can not be parsed. Reason: the line should have three normalized columns or five legacy columns",
                    resultLine);
            throw new IllegalArgumentException(wrongColumnsSizeMessage);
        }

        Instant date = Instant.parse(columns[0]);

        String[] basicNumbersArray = columns[1].split("-");
        if (basicNumbersArray.length != 5) {
            String wrongColumnsSizeMessage = MessageFormat.format(
                    "The line \"{0}\" can not be parsed. Reason: the line should have five basis numbers", resultLine);
            throw new IllegalArgumentException(wrongColumnsSizeMessage);
        }

        List<Integer> basicNumberList = Arrays.stream(basicNumbersArray)
                .map(Integer::valueOf)
                .sorted()
                .collect(Collectors.toList());

        String[] additionallyNumbersArray = columns[2].split("-");
        if (additionallyNumbersArray.length != 2) {
            String wrongColumnsSizeMessage = MessageFormat.format(
                    "The line \"{0}\" can not be parsed. Reason: the line should have two additionally numbers", resultLine);
            throw new IllegalArgumentException(wrongColumnsSizeMessage);
        }

        List<Integer> additionallyNumberList = Arrays.stream(additionallyNumbersArray)
                .map(Integer::valueOf)
                .sorted()
                .collect(Collectors.toList());

        return LotteryResult.builder()
                .date(date)
                .basisNumbers(basicNumberList)
                .additionallyNumbers(additionallyNumberList)
                .build();
    }
}
