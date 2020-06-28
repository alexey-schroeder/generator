package com.lottery.generator.printer;

import com.lottery.generator.model.LotteryResult;

import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Printer {

    private  static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.mm.yyyy")
            .withZone(ZoneId.of("UTC"));

    public static void print(Map<LotteryResult, List<Integer>> values) {
        ArrayList<LotteryResult> keys = new ArrayList<>(values.keySet());
        keys.sort((o1, o2) -> (int) (o2.getDate().getEpochSecond() - o1.getDate().getEpochSecond()));

        for (LotteryResult lotteryResult : keys) {
            System.out.println(
                    MessageFormat.format("{0}:{1} -> {2}",
                            DATE_TIME_FORMATTER.format(lotteryResult.getDate()), lotteryResult.getBasisNumbers(), values.get(lotteryResult)));
        }
    }
}
