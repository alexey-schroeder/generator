package com.lottery.generator.category;

import com.lottery.generator.model.LotteryResult;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
public abstract class Categories {
    protected Category categoryA;
    protected Category categoryB;
    protected Category categoryC;
    protected Category categoryD;
    protected Category categoryE;

    public Categories() {
        initCategoryA();
        initCategoryB();
        initCategoryC();
        initCategoryD();
        initCategoryE();
    }

    public List<Category> getAllCategories() {
        return List.of(categoryA, categoryB, categoryC, categoryD, categoryE);
    }

    public int calculateAbsIndex(LotteryResult lotteryResult) {
        return calculateIndexes(lotteryResult).stream().map(index -> Math.abs(index)).mapToInt(Integer::intValue).sum();
    }

    public int calculateIndex(LotteryResult lotteryResult) {
        return calculateIndexes(lotteryResult).stream().mapToInt(Integer::intValue).sum();
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

    public Category getCategoryByIndexInBasisNumbers(int index) {
        return getAllCategories().stream().
                filter(category -> category.getIndexInBasisNumbers() == index).
                findFirst().
                orElseThrow();
    }

    protected abstract void initCategoryA();

    protected abstract void initCategoryB();

    protected abstract void initCategoryC();

    protected abstract void initCategoryD();

    protected abstract void initCategoryE();
}
