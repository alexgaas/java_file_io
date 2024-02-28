package org.directbuffer;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
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

public class DirectBuffer {
    private static final String baseMainPath = "./src/main/resources/";

    public static final String Title = "Latency distribution by percentile";

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

        SymbolAxis domain = new SymbolAxis("percentiles",
                new String[]{"0", "0.5", "0.9", "0.95", "0.99", "0.999", "0.9999"});
        PeriodAxis range = new PeriodAxis("time in millis");

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
        output[4] = Double.parseDouble(values[4]);
        output[5] = Double.parseDouble(values[5]);
        output[6] = Double.parseDouble(values[6]);
        return output;
    }

    private static XYSeriesCollection getXySeriesCollection() throws IOException {
        // *p*         | 0     | 0.5   | 0.9   | 0.95  | 0.99   | 0.999   | 0.9999

        double[] seriesOutput = getSeriesFromFile("alignedLatencyPercentile.txt");
        XYSeries series = new XYSeries("aligned");
        series.add(0, seriesOutput[0]);
        series.add(1, seriesOutput[1]);
        series.add(2, seriesOutput[2]);
        series.add(3, seriesOutput[3]);
        series.add(4, seriesOutput[4]);
        series.add(5, seriesOutput[5]);
        series.add(6, seriesOutput[6]);

        double[] series2Output = getSeriesFromFile("notAlignedLatencyPercentile.txt");
        XYSeries series2 = new XYSeries("non-aligned");
        series2.add(0, series2Output[0]);
        series2.add(1, series2Output[1]);
        series2.add(2, series2Output[2]);
        series2.add(3, series2Output[3]);
        series2.add(4, series2Output[4]);
        series2.add(5, series2Output[5]);
        series2.add(6, series2Output[6]);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        dataset.addSeries(series2);
        return dataset;
    }
}
