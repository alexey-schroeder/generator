package com.lottery.generator.category;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.time.temporal.ValueRange.of;

@Getter
@Component
public class EuroJackpotCategories extends Categories {

    protected void initCategoryA() {
        Map<Integer, CategoryIndexValues> rangeMap = Map.of(
                0, CategoryIndexValues.from(of(1, 6), List.of()),
                1, CategoryIndexValues.from(of(7, 12), List.of()),
                2, CategoryIndexValues.from(of(13, 17), List.of()),
                3, CategoryIndexValues.from(of(18, 22), List.of()),
                4, CategoryIndexValues.from(of(23, 50), List.of()));
        categoryA = new Category("A", 0, rangeMap);
    }

    protected void initCategoryB() {
        Map<Integer, CategoryIndexValues> rangeMap = Map.of(
                0, CategoryIndexValues.from(of(7, 19), List.of(13, 17)),
                1, CategoryIndexValues.from(of(13, 28), List.of(14, 15, 16, 18, 19,24, 25)),
                2, CategoryIndexValues.from(of(24, 33), List.of(26, 27, 28)),
                3, CategoryIndexValues.from(of(34, 50), List.of()),
                -1, CategoryIndexValues.from(of(1, 6), List.of()));
        categoryB = new Category("B", 1, rangeMap);
    }

    protected void initCategoryC() {
        Map<Integer, CategoryIndexValues> rangeMap = Map.of(
                0, CategoryIndexValues.from(of(17, 32), List.of(18, 19)),
                1, CategoryIndexValues.from(of(33, 40), List.of(39)),
                2, CategoryIndexValues.from(of(39, 50), List.of(40)),
                -1, CategoryIndexValues.from(of(13, 19), List.of(17)),
                -2, CategoryIndexValues.from(of(1, 12), List.of()));
        categoryC = new Category("C", 2, rangeMap);
    }

    protected void initCategoryD() {
        Map<Integer, CategoryIndexValues> rangeMap = Map.of(
                0, CategoryIndexValues.from(of(33, 43), List.of()),
                1, CategoryIndexValues.from(of(44, 50), List.of()),
                -1, CategoryIndexValues.from(of(29, 32), List.of()),
                -2, CategoryIndexValues.from(of(18, 28), List.of()),
                -3, CategoryIndexValues.from(of(1, 17), List.of()));
        categoryD = new Category("D", 3, rangeMap);
    }

    protected void initCategoryE() {
        Map<Integer, CategoryIndexValues> rangeMap = Map.of(
                0, CategoryIndexValues.from(of(44, 50),List.of()),
                -1, CategoryIndexValues.from(of(41, 43),List.of()),
                -2, CategoryIndexValues.from(of(34, 40),List.of()),
                -3, CategoryIndexValues.from(of(23, 33),List.of()),
                -4, CategoryIndexValues.from(of(1, 22),List.of()));
        categoryE = new Category("E", 4, rangeMap);
    }
}
