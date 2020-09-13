package com.lottery.generator.category;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.time.temporal.ValueRange.of;

@Getter
@Component
public class MillionDayItalyCategories extends Categories {

    protected void initCategoryA() {
        Map<Integer, CategoryIndexValues> rangeMap = Map.of(
                0, CategoryIndexValues.from(of(1, 7), List.of()),
                1, CategoryIndexValues.from(of(8, 12),List.of()),
                2, CategoryIndexValues.from(of(13, 18),List.of()),
                3, CategoryIndexValues.from(of(19, 29),List.of()),
                4, CategoryIndexValues.from(of(30, 55),List.of()));
        categoryA = new Category("A", 0, rangeMap);
    }

    protected void initCategoryB() {
        Map<Integer, CategoryIndexValues> rangeMap = Map.of(
                0, CategoryIndexValues.from(of(8, 19),List.of()),
                1, CategoryIndexValues.from(of(20, 28),List.of()),
                2, CategoryIndexValues.from(of(29, 35),List.of()),
                3, CategoryIndexValues.from(of(36, 55),List.of()),
                -1, CategoryIndexValues.from(of(1, 7),List.of()));
        categoryB = new Category("B", 1, rangeMap);
    }

    protected void initCategoryC() {
        Map<Integer, CategoryIndexValues> rangeMap = Map.of(
                0, CategoryIndexValues.from(of(20, 33),List.of()),
                1, CategoryIndexValues.from(of(34, 44),List.of()),
                2, CategoryIndexValues.from(of(45, 55),List.of()),
                -1, CategoryIndexValues.from(of(12, 19),List.of()),
                -2, CategoryIndexValues.from(of(1, 11),List.of()));
        categoryC = new Category("C", 2, rangeMap);
    }

    protected void initCategoryD() {
        Map<Integer, CategoryIndexValues> rangeMap = Map.of(
                0, CategoryIndexValues.from(of(34, 50),List.of()),
                1, CategoryIndexValues.from(of(51, 55),List.of()),
                -1, CategoryIndexValues.from(of(29, 33),List.of()),
                -2, CategoryIndexValues.from(of(18, 28),List.of()),
                -3, CategoryIndexValues.from(of(1, 17),List.of()));
        categoryD = new Category("D", 3, rangeMap);
    }

    protected void initCategoryE() {
        Map<Integer, CategoryIndexValues> rangeMap = Map.of(
                0, CategoryIndexValues.from(of(50, 55),List.of()),
                -1, CategoryIndexValues.from(of(45, 49),List.of()),
                -2, CategoryIndexValues.from(of(36, 44),List.of()),
                -3, CategoryIndexValues.from(of(30, 35),List.of()),
                -4, CategoryIndexValues.from(of(1, 29),List.of()));
        categoryE = new Category("E", 4, rangeMap);
    }
}
