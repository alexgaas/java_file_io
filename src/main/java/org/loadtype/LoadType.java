package org.loadtype;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class LoadType {
    private static final String baseMainPath = "./src/main/resources/";

    public static final String Title = "Time of file processing based on access/load time";

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

        NumberAxis domain = new NumberAxis("file size in GB");
        domain.setTickUnit(new NumberTickUnit(2));
        LogarithmicAxis range = new LogarithmicAxis("time in seconds");
        range.setRange(0, 10);

        XYSplineRenderer r = new XYSplineRenderer(16);
        XYPlot xyplot = new XYPlot(dataset, domain, range, r);
        XYItemRenderer renderer = xyplot.getRenderer();
        renderer.setSeriesPaint(0, Color.MAGENTA);

        JFreeChart chart = new JFreeChart(xyplot);
        return chart;
    }

    private static double[] getSeriesFromFile(String fileName) throws IOException {
        String content = Files.readAllLines(Paths.get(baseMainPath + fileName), Charset.defaultCharset()).get(0);
        String[] values = content.split(" ");
        double[] output = new double[4];
        output[0] = Double.parseDouble(values[0]) / 1000;
        output[1] = Double.parseDouble(values[1]) / 1000;
        output[2] = Double.parseDouble(values[2]) / 1000;
        output[3] = Double.parseDouble(values[3]) / 1000;
        return output;
    }

    private static XYSeriesCollection getXySeriesCollection() throws IOException {
        double[] seriesOutput = getSeriesFromFile("seqReading.txt");
        XYSeries series = new XYSeries("Sequential reading");
        series.add(0, seriesOutput[0]);
        series.add(2, seriesOutput[1]);
        series.add(4, seriesOutput[2]);
        series.add(8, seriesOutput[3]);

        double[] series2Output = getSeriesFromFile("appendOnlyWrite.txt");
        XYSeries series2 = new XYSeries("Append-only write");
        series2.add(0, series2Output[0]);
        series2.add(2, series2Output[1]);
        series2.add(4, series2Output[2]);
        series2.add(8, series2Output[3]);

        double[] series3Output = getSeriesFromFile("randomRead.txt");
        XYSeries series3 = new XYSeries("Random read");
        series3.add(0, series3Output[0]);
        series3.add(2, series3Output[1]);
        series3.add(4, series3Output[2]);
        series3.add(8, series3Output[3]);

        double[] series4Output = getSeriesFromFile("randomWrite.txt");
        XYSeries series4 = new XYSeries("Random write");
        series4.add(0, series4Output[0]);
        series4.add(2, series4Output[1]);
        series4.add(4, series4Output[2]);
        series4.add(8, series4Output[3]);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        dataset.addSeries(series2);
        dataset.addSeries(series3);
        dataset.addSeries(series4);
        return dataset;
    }
}