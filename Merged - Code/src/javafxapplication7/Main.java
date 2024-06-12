/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package javafxapplication7;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends Application {

    private TableView<Job> tableView;
    private TextArea statisticsArea;
    private BarChart<String, Number> executionTimeBarChart;
    private BarChart<String, Number> jobsPerPartitionBarChart;
    private BarChart<String, Number> jobsPerNodeBarChart;
    private LineChart<String, Number> jobDurationLineChart;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Job Statistics");

        BorderPane borderPane = new BorderPane();
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        tableView = new TableView<>();
        statisticsArea = new TextArea();
        statisticsArea.setEditable(false);
        setupTableView();

        Button loadFileButton = new Button("Load Log File");
        loadFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Log File");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    List<String> logLines = readFile(file.getAbsolutePath());
                    HashMap<Integer, Job> jobMap = Job.fromLogLines(logLines);
                    updateTableView(jobMap);
                    updateCharts(jobMap);
                    updateStatistics(jobMap);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        vbox.getChildren().addAll(loadFileButton, tableView, statisticsArea);
        borderPane.setTop(vbox);

        HBox chartsBox = new HBox(10);
        chartsBox.setPadding(new Insets(10));
        setupCharts();
        chartsBox.getChildren().addAll(executionTimeBarChart, jobsPerPartitionBarChart, jobsPerNodeBarChart, jobDurationLineChart);
        borderPane.setCenter(chartsBox);

        Scene scene = new Scene(borderPane, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupTableView() {
        TableColumn<Job, Integer> jobIdColumn = new TableColumn<>("Job ID");
        jobIdColumn.setCellValueFactory(new PropertyValueFactory<>("jobId"));

        TableColumn<Job, String> startColumn = new TableColumn<>("Start Time");
        startColumn.setCellValueFactory(new PropertyValueFactory<>("start"));

        TableColumn<Job, String> endColumn = new TableColumn<>("End Time");
        endColumn.setCellValueFactory(new PropertyValueFactory<>("end"));

        TableColumn<Job, Integer> exitCodeColumn = new TableColumn<>("Exit Code");
        exitCodeColumn.setCellValueFactory(new PropertyValueFactory<>("exitCode"));

        TableColumn<Job, Long> durationColumn = new TableColumn<>("Duration (ms)");
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));

        TableColumn<Job, String> cpuColumn = new TableColumn<>("CPU");
        cpuColumn.setCellValueFactory(new PropertyValueFactory<>("cpu"));

        TableColumn<Job, String> partitionColumn = new TableColumn<>("Partition");
        partitionColumn.setCellValueFactory(new PropertyValueFactory<>("partition"));

        TableColumn<Job, String> nodeListColumn = new TableColumn<>("Node List");
        nodeListColumn.setCellValueFactory(new PropertyValueFactory<>("nodeList"));

        tableView.getColumns().addAll(jobIdColumn, startColumn, endColumn, exitCodeColumn, durationColumn, cpuColumn, partitionColumn, nodeListColumn);
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

    private void updateTableView(HashMap<Integer, Job> jobMap) {
        ObservableList<Job> jobs = FXCollections.observableArrayList(jobMap.values());
        tableView.setItems(jobs);
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

    private void updateStatistics(HashMap<Integer, Job> jobMap) {
        statisticsArea.clear();
        statisticsArea.appendText("Total number of jobs: " + jobMap.size() + "\n");
        statisticsArea.appendText("Average execution time: " + Job.getAverageExecutionTime(jobMap) + " ms\n");
        statisticsArea.appendText("Maximum execution time: " + Job.getMaxExecutionTime(jobMap) + " ms\n");
        statisticsArea.appendText("Minimum execution time: " + Job.getMinExecutionTime(jobMap) + " ms\n");

        Map<String, List<Job>> jobsByPartition = Job.getJobsByPartition(jobMap);
        statisticsArea.appendText("Jobs by Partition:\n");
        jobsByPartition.forEach((partition, jobs) -> {
            statisticsArea.appendText("  Partition: " + partition + ", Number of jobs: " + jobs.size() + "\n");
        });

        Map<String, List<Job>> jobsByNode = Job.getJobsByNode(jobMap);
        statisticsArea.appendText("Jobs by Node:\n");
        jobsByNode.forEach((node, jobs) -> {
            statisticsArea.appendText("  Node: " + node + ", Number of jobs: " + jobs.size() + "\n");
        });

        Map<Integer, List<Job>> jobsByExitCode = Job.getJobsByExitCode(jobMap);
        statisticsArea.appendText("Jobs by Exit Code:\n");
        jobsByExitCode.forEach((exitCode, jobs) -> {
            statisticsArea.appendText("  Exit Code: " + exitCode + ", Number of jobs: " + jobs.size() + "\n");
        });

        Map<String, Long> totalExecutionTimePerPartition = Job.getTotalExecutionTimePerPartition(jobMap);
        statisticsArea.appendText("Total Execution Time per Partition:\n");
        totalExecutionTimePerPartition.forEach((partition, totalDuration) -> {
            statisticsArea.appendText("  Partition: " + partition + ", Total Execution Time: " + totalDuration + " ms\n");
        });
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
