package com.lottery.generator.category;

import com.lottery.generator.model.LotteryResult;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.temporal.ValueRange;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ValueRange.of;

@Getter
@Component
public class Categories {
    private Category categoryA;
    private Category categoryB;
    private Category categoryC;
    private Category categoryD;
    private Category categoryE;

    public Categories() {
        initCategoryA();
        initCategoryB();
        initCategoryC();
        initCategoryD();
        initCategoryE();
    }

    public int calculateIndex(LotteryResult lotteryResult) {
        return calculateIndexes(lotteryResult).stream().map(index -> Math.abs(index)).mapToInt(Integer::intValue).sum();
    }

    public List<Integer> calculateIndexes(LotteryResult lotteryResult) {
        List<Integer> basisNumbers = lotteryResult.getBasisNumbers();
        int a = categoryA.getIndexForNumber(basisNumbers.get(0));
        int b = categoryB.getIndexForNumber(basisNumbers.get(1));
        int c = categoryC.getIndexForNumber(basisNumbers.get(2));
        int d = categoryD.getIndexForNumber(basisNumbers.get(3));
        int e = categoryE.getIndexForNumber(basisNumbers.get(4));

        return List.of(a, b, c, d, e);
    }

    private void initCategoryA() {
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(1, 6),
                1, of(7, 12),
                2, of(13, 17),
                3, of(18, 22),
                4, of(23, 50));
        categoryA = new Category("A", rangeMap);
    }

    private void initCategoryB() {
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(7, 16),
                1, of(17, 24),
                2, of(25, 33),
                3, of(34, 50),
                -1, of(1, 6));
        categoryB = new Category("B", rangeMap);
    }

    private void initCategoryC() {
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(17, 33),
                1, of(34, 41),
                2, of(42, 50),
                -1, of(12, 16),
                -2, of(1, 11));
        categoryC = new Category("C", rangeMap);
    }

    private void initCategoryD() {
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(33, 43),
                1, of(44, 50),
                -1, of(29, 33),
                -2, of(18, 28),
                -3, of(1, 17));
        categoryD = new Category("D", rangeMap);
    }

    private void initCategoryE() {
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(44, 50),
                -1, of(42, 43),
                -2, of(34, 41),
                -3, of(23, 33),
                -4, of(1, 22));
        categoryE = new Category("E", rangeMap);
    }
}
