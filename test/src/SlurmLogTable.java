import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class SlurmLogTable {

    static class JobInfo {
        String jobId;
        String submitTime;
        String startTime;
        String endTime;
        int cpuCount;
        long duration;
        String partition;
        boolean hasError;

        JobInfo(String jobId, String submitTime) {
            this.jobId = jobId;
            this.submitTime = submitTime;
        }
    }

    static List<JobInfo> jobs = new ArrayList<>();

    public static void main(String[] args) {
        String logFile = "/Users/syahid1011/Downloads/extracted_log";  // Path to the uploaded log file

        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                parseLogLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        calculateMetrics();
        printTable();
    }

    private static void parseLogLine(String line) {
        if (line.contains("_slurm_rpc_submit_batch_job")) {
            String jobId = extractValue(line, "JobId=");
            String submitTime = extractTime(line);
            jobs.add(new JobInfo(jobId, submitTime));
        } else if (line.contains("sched: Allocate")) {
            String jobId = extractValue(line, "JobId=");
            JobInfo job = findJobById(jobId);
            if (job != null) {
                job.startTime = extractTime(line);
                job.cpuCount = Integer.parseInt(extractValue(line, "#CPUs="));
                job.partition = extractValue(line, "Partition=");
            }
        } else if (line.contains("_job_complete")) {
            String jobId = extractValue(line, "JobId=");
            JobInfo job = findJobById(jobId);
            if (job != null) {
                job.endTime = extractTime(line);
            }
        } else if (line.contains("error: This association")) {
            String jobId = extractValue(line, "JobId=");
            JobInfo job = findJobById(jobId);
            if (job != null) {
                job.hasError = true;
            }
        }
    }

    private static String extractValue(String line, String key) {
        int startIndex = line.indexOf(key) + key.length();
        int endIndex = line.indexOf(" ", startIndex);
        if (endIndex == -1) {
            endIndex = line.length();
        }
        return line.substring(startIndex, endIndex);
    }

    private static String extractTime(String line) {
        return line.substring(1, 24);
    }

    private static JobInfo findJobById(String jobId) {
        for (JobInfo job : jobs) {
            if (job.jobId.equals(jobId)) {
                return job;
            }
        }
        return null;
    }

    private static void calculateMetrics() {
        for (JobInfo job : jobs) {
            if (job.startTime != null && job.endTime != null) {
                job.duration = calculateDuration(job.startTime, job.endTime);
            }
        }
    }

    private static long calculateDuration(String startTime, String endTime) {
        long startMillis = parseTimestampToMillis(startTime);
        long endMillis = parseTimestampToMillis(endTime);
        return endMillis - startMillis;
    }

    private static long parseTimestampToMillis(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return 0;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        try {
            Date date = sdf.parse(timestamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static void printTable() {
        String format = "| %-8s | %-20s | %-20s | %-20s | %-4d | %-10d | %-10s | %-5s |\n";
        System.out.println("+----------+----------------------+----------------------+----------------------+------+------------+------------+-------+");
        System.out.printf("| Job ID   | Submit Time          | Start Time           | End Time             | CPUs | Duration   | Partition  | Error |\n");
        System.out.println("+----------+----------------------+----------------------+----------------------+------+------------+------------+-------+");
        for (JobInfo job : jobs) {
            System.out.printf(format, job.jobId, job.submitTime, job.startTime, job.endTime, job.cpuCount, job.duration, job.partition, job.hasError ? "Yes" : "No");
        }
        System.out.println("+----------+----------------------+----------------------+----------------------+------+------------+------------+-------+");
    }
}

