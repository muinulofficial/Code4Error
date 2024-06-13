/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package javafxapplication9;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */

import javafx.scene.chart.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.format.DateTimeFormatter;


public class AdditionalFeatures_JavaFx extends Application {

    private BarChart<String, Number> executionTimeBarChart;
    private BarChart<String, Number> jobsPerPartitionBarChart;
    private BarChart<String, Number> jobsPerNodeBarChart;
    private LineChart<String, Number> jobDurationLineChart;

    private static final String LOG_FILE_PATH = "/Users/muinulislam/Desktop/extracted_log";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("UNIVERSITY OF MALAYA : DATA INTENSIVE COMPUTING CENTRE");

        BorderPane borderPane = new BorderPane();
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        borderPane.setTop(vbox);

        HBox chartsBox = new HBox(10);
        chartsBox.setPadding(new Insets(10));
        setupCharts();
        chartsBox.getChildren().addAll(executionTimeBarChart, jobsPerPartitionBarChart, jobsPerNodeBarChart, jobDurationLineChart);
        borderPane.setCenter(chartsBox);

        Scene scene = new Scene(borderPane, 1700, 900);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load and process the log file
        try {
            List<String> logLines = readFile(LOG_FILE_PATH);
            HashMap<Integer, Job> jobMap = Job.fromLogLines(logLines);
            updateCharts(jobMap);
           
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void setupCharts() {
        executionTimeBarChart = createBarChart("Partition", "Total Execution Time (ms)", "Total Execution Time per Partition");
        jobsPerPartitionBarChart = createBarChart("Partition", "Number of Jobs", "Total Number of Jobs per Partition");
        jobsPerNodeBarChart = createBarChart("Node", "Number of Jobs", "Total Number of Jobs per Node");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Start Time");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Duration (ms)");

        jobDurationLineChart = new LineChart<>(xAxis, yAxis);
        jobDurationLineChart.setTitle("Job Duration Over Time");
    }

    private BarChart<String, Number> createBarChart(String xAxisLabel, String yAxisLabel, String title) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisLabel);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle(title);
        return barChart;
    }

    private void updateCharts(HashMap<Integer, Job> jobMap) {
        updateBarChart(executionTimeBarChart, Job.getTotalExecutionTimePerPartition(jobMap));
        updateBarChart(jobsPerPartitionBarChart, Job.getJobsByPartitionCount(jobMap));
        updateBarChart(jobsPerNodeBarChart, Job.getJobsByNodeCount(jobMap));
        updateLineChart(jobDurationLineChart, jobMap);
    }

    private void updateBarChart(BarChart<String, Number> barChart, Map<String, Long> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));
        barChart.getData().clear();
        barChart.getData().add(series);
    }

    private void updateLineChart(LineChart<String, Number> lineChart, HashMap<Integer, Job> jobMap) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        jobMap.values().forEach(job -> {
            if (job.getStart() != null && job.getDuration() > 0) {
                series.getData().add(new XYChart.Data<>(job.getStart().format(formatter), job.getDuration()));
            }
        });

        lineChart.getData().clear();
        lineChart.getData().add(series);
    }

    

    public List<String> readFile(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static void main(String[] args) {
        launch(args);
    }
}