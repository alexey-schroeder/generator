package com.lottery.generator.category;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.temporal.ValueRange;
import java.util.Map;

import static java.time.temporal.ValueRange.of;

@Getter
@Component
public class EuroJackpotCategories extends Categories{

    protected void initCategoryA() {
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(1, 6),
                1, of(7, 12),
                2, of(13, 17),
                3, of(18, 22),
                4, of(23, 50));
        categoryA = new Category("A",0, rangeMap);
    }

    protected void initCategoryB() {
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(7, 16),
                1, of(17, 24),
                2, of(25, 33),
                3, of(34, 50),
                -1, of(1, 6));
        categoryB = new Category("B",1, rangeMap);
    }

    protected void initCategoryC() {
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(17, 33),
                1, of(34, 41),
                2, of(42, 50),
                -1, of(12, 16),
                -2, of(1, 11));
        categoryC = new Category("C",2, rangeMap);
    }

    protected void initCategoryD() {
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(33, 43),
                1, of(44, 50),
                -1, of(29, 33),
                -2, of(18, 28),
                -3, of(1, 17));
        categoryD = new Category("D",3, rangeMap);
    }

    protected void initCategoryE() {
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(44, 50),
                -1, of(42, 43),
                -2, of(34, 41),
                -3, of(23, 33),
                -4, of(1, 22));
        categoryE = new Category("E",4, rangeMap);
    }
}
