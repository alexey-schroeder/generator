package com.lottery.generator.resultmapper;

import com.lottery.generator.model.LotteryResult;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MillionDayItalyLotteryResultMapper extends LotteryResultMapper {
    @Override
    public LotteryResult lineToLotteryResult(String resultLine) throws ParseException {
        String[] columns = resultLine.split(";");
        if (columns.length != 6) {
            String wrongColumnsSizeMessage = MessageFormat.format(
                    "The line \"{0}\" can not be parsed. Reason: the line should have five columns", resultLine);
            throw new IllegalArgumentException(wrongColumnsSizeMessage);
        }

        Instant date = getDate(columns[0]);

        String[] basicNumbersArray = Arrays.copyOfRange(columns, 1, 6);

        List<Integer> basicNumberList = Arrays.stream(basicNumbersArray)
                .map(Integer::valueOf)
                .collect(Collectors.toList());

        basicNumberList.sort(Integer::compareTo);

        return LotteryResult.builder()
                .date(date)
                .basisNumbers(basicNumberList)
                .build();
    }

    private Instant getDate(String string) {
        String[] dateItem = string.split(" ");

        return Instant.now().atZone(ZoneOffset.UTC)
                .withYear(Integer.valueOf(dateItem[3]))
                .withMonth(replaceMonthStringWithNumber(dateItem[2]))
                .withDayOfMonth(Integer.valueOf(dateItem[1]))
                .toInstant();
    }

    private int replaceMonthStringWithNumber(String monthString) {
        switch (monthString) {
            case "Dicembre":
                return 12;
            case "Novembre":
                return 11;
            case "Ottobre":
                return  10;
            case "Settembre":
                return  9;
            case "Agosto":
                return 8;
            case "Luglio":
                return 7;
            case "Giugno":
                return 6;
            case "Maggio":
                return 5;
            case "Aprile":
                return 4;
            case "Marzo":
                return 3;
            case "Febbraio":
                return 2;
            case "Gennaio":
                return 1;
            default: throw new IllegalStateException("Unexpected value: " + monthString);
        }

    }
}
