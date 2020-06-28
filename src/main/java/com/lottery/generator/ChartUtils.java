package com.lottery.generator;

import com.lottery.generator.model.LotteryResult;
import com.lottery.generator.theory.TheoryUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

//https://www.tutorialspoint.com/jfreechart/jfreechart_xy_chart.htm

public class ChartUtils {

    public static void saveLotteryResultsCategoriesAsImage(List<LotteryResult> lotteryResults, int maxNumber, String fileName) throws IOException {
        int[][] statistic = new int[5][maxNumber + 1];
        for (LotteryResult lotteryResult : lotteryResults) {
            for (int i = 0; i < lotteryResult.getBasisNumbers().size(); i++) {
                int[] categoryArray = statistic[i];
                int number = lotteryResult.getBasisNumbers().get(i);
                categoryArray[number] = categoryArray[number] + 1;
            }
        }

        List<Integer> maximas = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            int[] categoryArray = statistic[i];
            int maxInCategory = TheoryUtils.getMax(categoryArray);
            maximas.add(maxInCategory);
        }

        int maxInAllCategories = TheoryUtils.getMax(maximas);
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (int category = 0; category < 5; category++) {
            int[] scaledCategoryValues = TheoryUtils.scalToMaximum(statistic[category], maxInAllCategories);
            XYSeries xySeries = new XYSeries(category + 1 + "");
            for (int i = 1; i < scaledCategoryValues.length; i++) {
                xySeries.add(i, scaledCategoryValues[i]);
            }
            dataset.addSeries(xySeries);
        }

        JFreeChart chart = ChartFactory.createXYLineChart("statistic", "x", "y", dataset);

        final XYPlot plot = chart.getXYPlot();

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.BLACK);
        renderer.setSeriesPaint(2, Color.BLUE);
        renderer.setSeriesPaint(3, Color.YELLOW);
        renderer.setSeriesPaint(4, Color.ORANGE);
        renderer.setSeriesStroke(0, new BasicStroke(4.0f));
        renderer.setSeriesStroke(1, new BasicStroke(4.0f));
        renderer.setSeriesStroke(2, new BasicStroke(4.0f));
        renderer.setSeriesStroke(3, new BasicStroke(4.0f));
        renderer.setSeriesStroke(4, new BasicStroke(4.0f));
        plot.setRenderer(renderer);
        File XYChart = new File(fileName);
        org.jfree.chart.ChartUtils.saveChartAsJPEG(XYChart, chart, 1500, 600);
    }

    public static void showBarChartInFrame(Map<Integer, Integer> values) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Integer, Integer> entry : values.entrySet()) {
            if (entry.getValue() > 0) {
                dataset.addValue(entry.getValue(), Integer.valueOf(0), entry.getKey());
            }
        }

        JFreeChart chart = ChartFactory.createBarChart("statistic", "x", "y", dataset);
        BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
        renderer.setItemMargin(-20);
        showChartInFrame(chart);
    }


    public static void showChartInFrame(JFreeChart chart) {
        JFrame frame = new JFrame("test");
        frame.setBackground(Color.WHITE);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setTitle("Hazelcast Jet Source Builder Sample");
        frame.setBounds(0, 0, 1000, 400);
        frame.setLayout(new BorderLayout());
        frame.add(new ChartPanel(chart));
        frame.setVisible(true);
    }
}
