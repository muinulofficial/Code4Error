package Code4Error;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;

public class table extends Application {

    private final TableView<JobData> table = new TableView<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        String filePath = "C:\\1 Sarah Labtop\\1 Sarah\\2 Bacholar degree\\1 2023 2034\\2 Sem2\\2 Classes\\WIX1002 FUNDAMENTALS OF PROGRAMMING\\fop assignment\\assignment\\Code4Error\\extracted_log.txt";

        assign dataProcessor = new assign();
        dataProcessor.processLogFile(filePath);

        TableColumn<JobData, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<JobData, Integer> createdColumn = new TableColumn<>("Jobs Created");
        createdColumn.setCellValueFactory(new PropertyValueFactory<>("jobsCreated"));

        TableColumn<JobData, Integer> endedColumn = new TableColumn<>("Jobs Ended");
        endedColumn.setCellValueFactory(new PropertyValueFactory<>("jobsEnded"));

        TableColumn<JobData, Integer> opteronColumn = new TableColumn<>("Opteron Jobs");
        opteronColumn.setCellValueFactory(new PropertyValueFactory<>("opteronJobs"));

        TableColumn<JobData, Integer> epycColumn = new TableColumn<>("EPYC Jobs");
        epycColumn.setCellValueFactory(new PropertyValueFactory<>("epycJobs"));

        TableColumn<JobData, Integer> gpuColumn = new TableColumn<>("GPU Jobs");
        gpuColumn.setCellValueFactory(new PropertyValueFactory<>("gpuJobs"));

        table.getColumns().addAll(dateColumn, createdColumn, endedColumn, opteronColumn, epycColumn, gpuColumn);

        for (String date : dataProcessor.getJobsCreated().keySet()) {
            int created = dataProcessor.getJobsCreated().getOrDefault(date, 0);
            int ended = dataProcessor.getJobsEnded().getOrDefault(date, 0);
            int opteronJobs = dataProcessor.getJobsByPartition().getOrDefault(date, new HashMap<>()).getOrDefault("cpu-opteron", 0);
            int epycJobs = dataProcessor.getJobsByPartition().getOrDefault(date, new HashMap<>()).getOrDefault("cpu-epyc", 0);
            int gpuJobs = dataProcessor.getJobsByPartition().getOrDefault(date, new HashMap<>()).getOrDefault("gpu", 0);
            table.getItems().add(new JobData(date, created, ended, opteronJobs, epycJobs, gpuJobs));
        }

        VBox vbox = new VBox(table);
        Scene scene = new Scene(vbox);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Job Data");
        primaryStage.show();
    }

    public static class JobData {

        private final String date;
        private final int jobsCreated;
        private final int jobsEnded;
        private final int opteronJobs;
        private final int epycJobs;
        private final int gpuJobs;

        public JobData(String date, int jobsCreated, int jobsEnded, int opteronJobs, int epycJobs, int gpuJobs) {
            this.date = date;
            this.jobsCreated = jobsCreated;
            this.jobsEnded = jobsEnded;
            this.opteronJobs = opteronJobs;
            this.epycJobs = epycJobs;
            this.gpuJobs = gpuJobs;
        }

        public String getDate() {
            return date;
        }

        public int getJobsCreated() {
            return jobsCreated;
        }

        public int getJobsEnded() {
            return jobsEnded;
        }

        public int getOpteronJobs() {
            return opteronJobs;
        }

        public int getEpycJobs() {
            return epycJobs;
        }

        public int getGpuJobs() {
            return gpuJobs;
        }
    }
}


/*import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class table extends Application {

    private final TableView<JobData> table = new TableView<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        String filePath = "C:\\1 Sarah Labtop\\1 Sarah\\2 Bacholar degree\\1 2023 2034\\2 Sem2\\2 Classes\\WIX1002 FUNDAMENTALS OF PROGRAMMING\\fop assignment\\assignment\\Code4Error\\extracted_log.txt";
        HashMap<String, Integer> jobsCreated = new HashMap<>();
        HashMap<String, Integer> jobsEnded = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> jobsByPartition = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");
                String timestamp = tokens[0];
                String date = timestamp.substring(0, 10);

                if (line.contains("slurm_rpc_submit_batch_job")) {
                    jobsCreated.merge(date, 1, Integer::sum);
                } else if (line.contains("_job_complete:")) {
                    jobsEnded.merge(date, 1, Integer::sum);
                } else if (line.contains("Partition=cpu-opteron") || line.contains("Partition=cpu-epyc") || line.contains("Partition=gpu")) {
                    String partition = line.split("Partition=")[1].split(" ")[0];
                    jobsByPartition.putIfAbsent(date, new HashMap<>());
                    jobsByPartition.get(date).merge(partition, 1, Integer::sum);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        TableColumn<JobData, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<JobData, Integer> createdColumn = new TableColumn<>("Jobs Created");
        createdColumn.setCellValueFactory(new PropertyValueFactory<>("jobsCreated"));

        TableColumn<JobData, Integer> endedColumn = new TableColumn<>("Jobs Ended");
        endedColumn.setCellValueFactory(new PropertyValueFactory<>("jobsEnded"));

        TableColumn<JobData, Integer> opteronColumn = new TableColumn<>("Opteron Jobs");
        opteronColumn.setCellValueFactory(new PropertyValueFactory<>("opteronJobs"));

        TableColumn<JobData, Integer> epycColumn = new TableColumn<>("EPYC Jobs");
        epycColumn.setCellValueFactory(new PropertyValueFactory<>("epycJobs"));

        TableColumn<JobData, Integer> gpuColumn = new TableColumn<>("GPU Jobs");
        gpuColumn.setCellValueFactory(new PropertyValueFactory<>("gpuJobs"));

        table.getColumns().add(dateColumn);
        table.getColumns().add(createdColumn);
        table.getColumns().add(endedColumn);
        table.getColumns().add(opteronColumn);
        table.getColumns().add(epycColumn);
        table.getColumns().add(gpuColumn);

        for (String date : jobsCreated.keySet()) {
            int created = jobsCreated.getOrDefault(date, 0);
            int ended = jobsEnded.getOrDefault(date, 0);
            int opteronJobs = jobsByPartition.getOrDefault(date, new HashMap<>()).getOrDefault("cpu-opteron", 0);
            int epycJobs = jobsByPartition.getOrDefault(date, new HashMap<>()).getOrDefault("cpu-epyc", 0);
            int gpuJobs = jobsByPartition.getOrDefault(date, new HashMap<>()).getOrDefault("gpu", 0);
            table.getItems().add(new JobData(date, created, ended, opteronJobs, epycJobs, gpuJobs));
        }

        VBox vbox = new VBox(table);
        Scene scene = new Scene(vbox);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Job Data");
        primaryStage.show();
    }

    public static class JobData {
        private final String date;
        private final int jobsCreated;
        private final int jobsEnded;
        private final int opteronJobs;
        private final int epycJobs;
        private final int gpuJobs;

        public JobData(String date, int jobsCreated, int jobsEnded, int opteronJobs, int epycJobs, int gpuJobs) {
            this.date = date;
            this.jobsCreated = jobsCreated;
            this.jobsEnded = jobsEnded;
            this.opteronJobs = opteronJobs;
            this.epycJobs = epycJobs;
            this.gpuJobs = gpuJobs;
        }

        public String getDate() {
            return date;
        }

        public int getJobsCreated() {
            return jobsCreated;
        }

        public int getJobsEnded() {
            return jobsEnded;
        }

        public int getOpteronJobs() {
            return opteronJobs;
        }

        public int getEpycJobs() {
            return epycJobs;
        }

        public int getGpuJobs() {
            return gpuJobs;
        }
    }
}
 */
