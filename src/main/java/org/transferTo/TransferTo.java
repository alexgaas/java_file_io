package org.transferTo;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TransferTo {
    private static final String baseMainPath = "./src/main/resources/";

    public static final String Title = "Naive copy vs transferTo";

    public static void main(String[] args) {
        try {
            EventQueue.invokeLater(() -> {
                try {
                    display();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private static void display() throws IOException {
        JFreeChart chart = getjFreeChart();
        ChartPanel chartPanel = new ChartPanel(chart){
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(4096, 2304);
            }
        };
        JFrame frame = new JFrame(Title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(chartPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JFreeChart getjFreeChart() throws IOException {
        XYSeriesCollection dataset = getXySeriesCollection();

        SymbolAxis domain = new SymbolAxis("file size in MB",
                new String[]{"64", "256", "512", "1024"});
        NumberAxis range = new NumberAxis("time in millis");

        XYSplineRenderer r = new XYSplineRenderer(16);
        XYPlot xyplot = new XYPlot(dataset, domain, range, r);

        JFreeChart chart = new JFreeChart(xyplot);
        return chart;
    }

    private static double[] getSeriesFromFile(String fileName) throws IOException {
        String content = Files.readAllLines(Paths.get(baseMainPath + fileName), Charset.defaultCharset()).get(0);
        String[] values = content.split(" ");
        double[] output = new double[7];
        output[0] = Double.parseDouble(values[0]);
        output[1] = Double.parseDouble(values[1]);
        output[2] = Double.parseDouble(values[2]);
        output[3] = Double.parseDouble(values[3]);
        return output;
    }

    private static XYSeriesCollection getXySeriesCollection() throws IOException {
        double[] seriesOutput = getSeriesFromFile("naiveCopy.txt");
        XYSeries series = new XYSeries("naive copy");
        series.add(0, seriesOutput[0]);
        series.add(1, seriesOutput[1]);
        series.add(2, seriesOutput[2]);
        series.add(3, seriesOutput[3]);

        double[] series2Output = getSeriesFromFile("transferToCopy.txt");
        XYSeries series2 = new XYSeries("use transferTo");
        series2.add(0, series2Output[0]);
        series2.add(1, series2Output[1]);
        series2.add(2, series2Output[2]);
        series2.add(3, series2Output[3]);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        dataset.addSeries(series2);
        return dataset;
    }
}
