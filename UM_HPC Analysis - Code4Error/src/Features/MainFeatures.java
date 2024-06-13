/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package javafxapplication9;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class MainFeatures extends Application {

    private BarChart<String, Number> executionTimeBarChart;
    private BarChart<String, Number> jobsPerPartitionBarChart;
    private BarChart<String, Number> jobsPerNodeBarChart;
    private LineChart<String, Number> jobDurationLineChart;

    private static final String LOG_FILE_PATH = "/Users/muinulislam/Desktop/extracted_log";

    private List<String> partitions = new ArrayList<>();
    private List<Integer> partitionCounts = new ArrayList<>();
    private LocalDateTime startTime = null;
    private LocalDateTime endTime = null;
    private int createdCount = 0;
    private int endedCount = 0;
    private int errorJobs = 0;
    private int completedWithZero = 0;
    private int completedWithPositive = 0;
    private int killedJobs = 0;
    private Map<String, Integer> userErrorCount = new HashMap<>();
    private long totalExecutionTimeSeconds = 0;
    private int jobCount = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("UNIVERSITY OF MALAYA : DATA INTENSIVE COMPUTING CENTRE");

        BorderPane borderPane = new BorderPane();
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        TextArea statisticsArea = new TextArea();
        statisticsArea.setEditable(false);

        vbox.getChildren().addAll(statisticsArea);
        borderPane.setTop(vbox);

        setupCharts();


        VBox detailsBox = new VBox(25);
        detailsBox.setPadding(new Insets(25));
        loadData();
        setupDetails(detailsBox);
        borderPane.setBottom(detailsBox);

        ScrollPane scrollPane = new ScrollPane(borderPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 1700, 900);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load and process the log file
        try {
            List<String> logLines = readFile(LOG_FILE_PATH);
            HashMap<Integer, Job> jobMap = Job.fromLogLines(logLines);
            
            updateStatistics(jobMap, statisticsArea);
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

    

    private void updateStatistics(HashMap<Integer, Job> jobMap, TextArea statisticsArea) {
        statisticsArea.clear();
        statisticsArea.appendText("Average execution time: " + Job.getAverageExecutionTime(jobMap) + " ms\n");
        statisticsArea.appendText("Maximum execution time: " + Job.getMaxExecutionTime(jobMap) + " ms\n");

        Map<String, List<Job>> jobsByPartition = Job.getJobsByPartition(jobMap);
        statisticsArea.appendText("\nJobs by Partition:\n");
        jobsByPartition.forEach((partition, jobs) -> {
            statisticsArea.appendText("  Partition: " + partition + ", Number of jobs: " + jobs.size() + "\n");
        });

        Map<String, List<Job>> jobsByNode = Job.getJobsByNode(jobMap);
        statisticsArea.appendText("\nJobs by Node:\n");
        jobsByNode.forEach((node, jobs) -> {
            statisticsArea.appendText("  Node: " + node + ", Number of jobs: " + jobs.size() + "\n");
        });

        Map<Integer, List<Job>> jobsByExitCode = Job.getJobsByExitCode(jobMap);
        statisticsArea.appendText("\nJobs by Exit Code:\n");
        jobsByExitCode.forEach((exitCode, jobs) -> {
            statisticsArea.appendText("  Exit Code: " + exitCode + ", Number of jobs: " + jobs.size() + "\n");
        });

        Map<String, Long> totalExecutionTimePerPartition = Job.getTotalExecutionTimePerPartition(jobMap);
        statisticsArea.appendText("\nTotal Execution Time per Partition:\n");
        totalExecutionTimePerPartition.forEach((partition, totalDuration) -> {
            statisticsArea.appendText("  Partition: " + partition + ", Total Execution Time: " + totalDuration + " ms\n");
        });
    }

    private void setupDetails(VBox detailsBox) {
        detailsBox.getChildren().add(new Label("Job Partition Counts:"));
        TableView<PartitionCount> partitionTable = createPartitionTable();
        BarChart<String, Number> partitionChart = createPartitionChart();
        detailsBox.getChildren().addAll(partitionTable, partitionChart);

        detailsBox.getChildren().add(new Label("Job Timing Information:"));
        detailsBox.getChildren().add(new Label("Start Time: " + startTime));
        detailsBox.getChildren().add(new Label("End Time: " + endTime));
        detailsBox.getChildren().add(new Label("Number of Jobs Created: " + createdCount));
        detailsBox.getChildren().add(new Label("Number of Jobs Ended: " + endedCount + "\n"));

        detailsBox.getChildren().add(new Label("\nJob Error Information:"));
        TableView<UserError> errorTable = createUserErrorTable();
        BarChart<String, Number> errorChart = createErrorChart();
        detailsBox.getChildren().addAll(errorTable, errorChart);

    }

    private void loadData() {
        Pattern allocatePattern = Pattern.compile("\\[(.+?)\\] sched: Allocate JobId=(\\d+) NodeList=(\\S+) #CPUs=(\\d+) Partition=(\\S+)");
        Pattern submitPattern = Pattern.compile("\\[(.+?)\\] _slurm_rpc_submit_batch_job: JobId=(\\d+)");
        Pattern completePattern = Pattern.compile("\\[(.+?)\\] _job_complete: JobId=(\\d+) WEXITSTATUS (\\d+)");
        Pattern errorPattern = Pattern.compile("error:.*user='(\\S+)'");
        Pattern exitStatusPattern = Pattern.compile("_job_complete: JobId=(\\d+) WEXITSTATUS (\\d+)");
        Pattern killPattern = Pattern.compile("_slurm_rpc_kill_job");

        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher allocateMatcher = allocatePattern.matcher(line);
                if (allocateMatcher.find()) {
                    String partition = line.substring(line.lastIndexOf("Partition=") + 10).trim();
                    int index = partitions.indexOf(partition);
                    if (index == -1) {
                        partitions.add(partition);
                        partitionCounts.add(1);
                    } else {
                        partitionCounts.set(index, partitionCounts.get(index) + 1);
                    }
                }

                LocalDateTime timestamp = LocalDateTime.parse(line.substring(1, 24), DateTimeFormatter.ISO_DATE_TIME);
                if (startTime == null || timestamp.isBefore(startTime)) {
                    startTime = timestamp;
                }
                if (endTime == null || timestamp.isAfter(endTime)) {
                    endTime = timestamp;
                }
                if (submitPattern.matcher(line).find()) {
                    createdCount++;
                } else if (completePattern.matcher(line).find()) {
                    endedCount++;
                    Matcher exitStatusMatcher = exitStatusPattern.matcher(line);
                    if (exitStatusMatcher.find()) {
                        int exitStatus = Integer.parseInt(exitStatusMatcher.group(2));
                        if (exitStatus == 0) {
                            completedWithZero++;
                        } else {
                            completedWithPositive++;
                        }
                    }

                    Duration executionTime = calculateExecutionTime(startTime, timestamp);
                    totalExecutionTimeSeconds += executionTime.getSeconds();
                    jobCount++;
                }

                Matcher errorMatcher = errorPattern.matcher(line);
                if (errorMatcher.find()) {
                    errorJobs++;
                    String user = errorMatcher.group(1);
                    userErrorCount.put(user, userErrorCount.getOrDefault(user, 0) + 1);
                }

                if (killPattern.matcher(line).find()) {
                    killedJobs++;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the log file: " + e.getMessage());
        }
    }

    private TableView<PartitionCount> createPartitionTable() {
        TableView<PartitionCount> table = new TableView<>();
        table.setPrefHeight(200);

        TableColumn<PartitionCount, String> partitionColumn = new TableColumn<>("Partition");
        partitionColumn.setCellValueFactory(new PropertyValueFactory<>("partition"));

        TableColumn<PartitionCount, Integer> countColumn = new TableColumn<>("Count");
        countColumn.setCellValueFactory(new PropertyValueFactory<>("count"));

        table.getColumns().add(partitionColumn);
        table.getColumns().add(countColumn);

        ObservableList<PartitionCount> data = FXCollections.observableArrayList();
        for (int i = 0; i < partitions.size(); i++) {
            data.add(new PartitionCount(partitions.get(i), partitionCounts.get(i)));
        }
        table.setItems(data);

        return table;
    }

    private TableView<UserError> createUserErrorTable() {
        TableView<UserError> table = new TableView<>();
        table.setPrefHeight(200);

        TableColumn<UserError, String> userColumn = new TableColumn<>("User");
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));

        TableColumn<UserError, Integer> errorCountColumn = new TableColumn<>("Error Count");
        errorCountColumn.setCellValueFactory(new PropertyValueFactory<>("errorCount"));

        table.getColumns().add(userColumn);
        table.getColumns().add(errorCountColumn);

        ObservableList<UserError> data = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : userErrorCount.entrySet()) {
            data.add(new UserError(entry.getKey(), entry.getValue()));
        }
        table.setItems(data);

        return table;
    }

    private BarChart<String, Number> createPartitionChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Partition");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Count");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Job Partition Counts");

        XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();
        for (int i = 0; i < partitions.size(); i++) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(partitions.get(i), partitionCounts.get(i));
            dataSeries.getData().add(data);
            int index = i;
            data.nodeProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    newValue.setStyle("-fx-bar-fill: " + getColor(index) + ";");
                }
            });
        }

        barChart.getData().add(dataSeries);

        return barChart;
    }

    private BarChart<String, Number> createErrorChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("User");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Error Count");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("User Error Counts");

        XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();
        int index = 0;
        for (Map.Entry<String, Integer> entry : userErrorCount.entrySet()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            dataSeries.getData().add(data);
            int idx = index++;
            data.nodeProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    newValue.setStyle("-fx-bar-fill: " + getColor(idx) + ";");
                }
            });
        }

        barChart.getData().add(dataSeries);

        return barChart;
    }

    private static Duration calculateExecutionTime(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end);
    }

    private static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String getColor(int index) {
        List<String> colors = Arrays.asList(
                "#FF6347", // Tomato
                "#4682B4", // SteelBlue
                "#32CD32", // LimeGreen
                "#FFD700", // Gold
                "#FF69B4", // HotPink
                "#1E90FF", // DodgerBlue
                "#8A2BE2", // BlueViolet
                "#7FFF00", // Chartreuse
                "#FF4500", // OrangeRed
                "#00CED1", // DarkTurquoise
                "#FF0000", // Red
                "#00FF00", // Green
                "#0000FF", // Blue
                "#FFFF00", // Yellow
                "#FF00FF", // Magenta
                "#00FFFF", // Cyan
                "#800000", // Maroon
                "#808000", // Olive
                "#800080", // Purple
                "#008080" // Teal
        );
        return colors.get(index % colors.size());
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

    public static class PartitionCount {

        private final SimpleStringProperty partition;
        private final SimpleIntegerProperty count;

        public PartitionCount(String partition, int count) {
            this.partition = new SimpleStringProperty(partition);
            this.count = new SimpleIntegerProperty(count);
        }

        public String getPartition() {
            return partition.get();
        }

        public int getCount() {
            return count.get();
        }
    }

    public static class UserError {

        private final SimpleStringProperty user;
        private final SimpleIntegerProperty errorCount;

        public UserError(String user, int errorCount) {
            this.user = new SimpleStringProperty(user);
            this.errorCount = new SimpleIntegerProperty(errorCount);
        }

        public String getUser() {
            return user.get();
        }

        public int getErrorCount() {
            return errorCount.get();
        }
    }
}

class Job {

    private int jobId;
    private LocalDateTime start;
    private LocalDateTime end;
    private int exitCode;
    private long duration;
    private String cpu;
    private String partition;
    private String nodeList;

    public int getJobId() {
        return jobId;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public int getExitCode() {
        return exitCode;
    }

    public long getDuration() {
        return duration;
    }

    public String getCpu() {
        return cpu;
    }

    public String getPartition() {
        return partition;
    }

    public String getNodeList() {
        return nodeList;
    }

    public static HashMap<Integer, Job> fromLogLines(List<String> logLines) {
        HashMap<Integer, Job> jobMap = new HashMap<>();

        Pattern submitPattern = Pattern.compile("\\[(.+?)\\] _slurm_rpc_submit_batch_job: JobId=(\\d+)");
        Pattern allocatePattern = Pattern.compile("\\[(.+?)\\] sched: Allocate JobId=(\\d+) NodeList=(\\S+) #CPUs=(\\d+) Partition=(\\S+)");
        Pattern completePattern = Pattern.compile("\\[(.+?)\\] _job_complete: JobId=(\\d+) WEXITSTATUS (\\d+)");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        for (String line : logLines) {
            Matcher submitMatcher = submitPattern.matcher(line);
            Matcher allocateMatcher = allocatePattern.matcher(line);
            Matcher completeMatcher = completePattern.matcher(line);

            if (submitMatcher.find()) {
                int jobId = Integer.parseInt(submitMatcher.group(2));
                Job job = jobMap.getOrDefault(jobId, new Job());
                job.start = LocalDateTime.parse(submitMatcher.group(1), formatter);
                job.jobId = jobId;
                jobMap.put(jobId, job);
            } else if (allocateMatcher.find()) {
                int jobId = Integer.parseInt(allocateMatcher.group(2));
                Job job = jobMap.getOrDefault(jobId, new Job());
                job.nodeList = allocateMatcher.group(3);
                job.cpu = allocateMatcher.group(4);
                job.partition = allocateMatcher.group(5);
                jobMap.put(jobId, job);
            } else if (completeMatcher.find()) {
                int jobId = Integer.parseInt(completeMatcher.group(2));
                Job job = jobMap.getOrDefault(jobId, new Job());
                job.end = LocalDateTime.parse(completeMatcher.group(1), formatter);
                job.exitCode = Integer.parseInt(completeMatcher.group(3));
                jobMap.put(jobId, job);
            }
        }

        for (Job job : jobMap.values()) {
            if (job.start != null && job.end != null) {
                job.duration = java.time.Duration.between(job.start, job.end).toMillis();
            }
        }

        return jobMap;
    }

    public static double getAverageExecutionTime(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .mapToLong(job -> job.duration)
                .average()
                .orElse(0);
    }

    public static long getMaxExecutionTime(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .mapToLong(job -> job.duration)
                .max()
                .orElse(0);
    }

    public static long getMinExecutionTime(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .mapToLong(job -> job.duration)
                .min()
                .orElse(0);
    }

    public static Map<String, Long> getJobsByPartitionCount(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .collect(Collectors.groupingBy(job -> Optional.ofNullable(job.partition).orElse("UNKNOWN"), Collectors.counting()));
    }

    public static Map<String, Long> getJobsByNodeCount(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .collect(Collectors.groupingBy(job -> Optional.ofNullable(job.nodeList).orElse("UNKNOWN"), Collectors.counting()));
    }

    public static Map<String, List<Job>> getJobsByPartition(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .collect(Collectors.groupingBy(job -> Optional.ofNullable(job.partition).orElse("UNKNOWN")));
    }

    public static Map<String, List<Job>> getJobsByNode(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .collect(Collectors.groupingBy(job -> Optional.ofNullable(job.nodeList).orElse("UNKNOWN")));
    }

    public static Map<Integer, List<Job>> getJobsByExitCode(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .collect(Collectors.groupingBy(job -> job.exitCode));
    }

    public static Map<String, Long> getTotalExecutionTimePerPartition(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .collect(Collectors.groupingBy(job -> Optional.ofNullable(job.partition).orElse("UNKNOWN"),
                        Collectors.summingLong(job -> job.duration)));
    }

    @Override
    public String toString() {
        return "Job{"
                + "jobId=" + jobId
                + ", start=" + start
                + ", end=" + end
                + ", exitCode=" + exitCode
                + ", duration=" + duration
                + ", cpu='" + cpu + '\''
                + ", partition='" + partition + '\''
                + ", nodeList='" + nodeList + '\''
                + '}';
    }
}
