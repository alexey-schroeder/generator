package com.lottery.generator.category;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.temporal.ValueRange;
import java.util.Map;

import static java.time.temporal.ValueRange.of;

@Getter
@Component
public class MillionDayItalyCategories extends Categories {

    protected void initCategoryA() {
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(1, 7),
                1, of(8, 12),
                2, of(13, 18),
                3, of(19, 29),
                4, of(30, 55));
        categoryA = new Category("A", rangeMap);
    }

    protected void initCategoryB() {
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(8, 19),
                1, of(20, 28),
                2, of(29, 35),
                3, of(36, 55),
                -1, of(1, 7));
        categoryB = new Category("B", rangeMap);
    }

    protected void initCategoryC() {
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(20, 33),
                1, of(34, 44),
                2, of(45, 55),
                -1, of(12, 19),
                -2, of(1, 11));
        categoryC = new Category("C", rangeMap);
    }

    protected void initCategoryD() {
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(34, 50),
                1, of(51, 55),
                -1, of(29, 33),
                -2, of(18, 28),
                -3, of(1, 17));
        categoryD = new Category("D", rangeMap);
    }

    protected void initCategoryE() {
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(50, 55),
                -1, of(45, 49),
                -2, of(36, 44),
                -3, of(30, 35),
                -4, of(1, 29));
        categoryE = new Category("E", rangeMap);
    }
}
