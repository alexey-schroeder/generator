package com.lottery.generator.theory;

import com.lottery.generator.model.LotteryResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class XBasisNumbersWereGotYetTheoryTest {

    private XBasisNumbersWereGotYetTheory xBasisNumbersWereGotYetTheory = new XBasisNumbersWereGotYetTheory();

    @ParameterizedTest
    @MethodSource("testData")
    void wereBasisNumbersGotYet_shouldReturnCorrectResult(List<Integer> newNumbers, List<LotteryResult> oldLotteryResults,
                                                          int counter, List<LotteryResult> expectedResult) {
        //when
        List<LotteryResult> actualTheoryResult = xBasisNumbersWereGotYetTheory.wereBasisNumbersGotYet(newNumbers, oldLotteryResults, counter);

        //then
        assertThat(actualTheoryResult, is(expectedResult));
    }

    private static Stream<Arguments> testData() {
        List<Integer> list_1_2_3_4_5 = asList(1, 2, 3, 4, 5);
        List<Integer> list_11_12_13_14_15 = asList(11, 12, 13, 14, 15);

        LotteryResult lotteryResult_1_2_3_4_6 = createLotteryResult(asList(1, 2, 3, 4, 6));
        LotteryResult lotteryResult_1_7_3_4_5 = createLotteryResult(asList(1, 7, 3, 4, 5));

        return Stream.of(
                Arguments.of(null, singletonList(createLotteryResult(null)), 4, emptyList()),
                Arguments.of(null, singletonList(createLotteryResult(emptyList())), 4, emptyList()),
                Arguments.of(emptyList(), singletonList(createLotteryResult(null)), 4, emptyList()),
                Arguments.of(emptyList(), emptyList(), 4, emptyList()),
                Arguments.of(emptyList(), singletonList(lotteryResult_1_2_3_4_6), 4, emptyList()),
                Arguments.of(list_11_12_13_14_15, emptyList(), 4, emptyList()),
                Arguments.of(list_11_12_13_14_15, asList(lotteryResult_1_2_3_4_6, lotteryResult_1_7_3_4_5), 4, emptyList()),
                Arguments.of(list_1_2_3_4_5, singletonList(lotteryResult_1_2_3_4_6), 4, singletonList(lotteryResult_1_2_3_4_6)),
                Arguments.of(list_1_2_3_4_5, asList(lotteryResult_1_2_3_4_6, lotteryResult_1_7_3_4_5), 4, asList(lotteryResult_1_2_3_4_6, lotteryResult_1_7_3_4_5))
        );
    }

    private static LotteryResult createLotteryResult(List<Integer> basisNumbers) {
        Random random = new Random();
        return LotteryResult.builder()
                .basisNumbers(basisNumbers)
                .additionallyNumbers(asList(1, 2))
                .date(Instant.now().plusMillis(random.nextLong()))
                .build();
    }
}