/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javafxapplication7;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Job {
    private int jobId;
    private LocalDateTime start;
    private LocalDateTime end;
    private int exitCode;
    private long duration;
    private String cpu;
    private String partition;
    private String nodeList;

    // Getters
    public int getJobId() { return jobId; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public int getExitCode() { return exitCode; }
    public long getDuration() { return duration; }
    public String getCpu() { return cpu; }
    public String getPartition() { return partition; }
    public String getNodeList() { return nodeList; }

    // Method to parse log lines and store Job instances in a HashMap
    public static HashMap<Integer, Job> fromLogLines(List<String> logLines) {
        HashMap<Integer, Job> jobMap = new HashMap<>();

        // Patterns for each type of log line
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

        // Calculate duration for each job
        for (Job job : jobMap.values()) {
            if (job.start != null && job.end != null) {
                job.duration = java.time.Duration.between(job.start, job.end).toMillis();
            }
        }

        return jobMap;
    }

    // Utility method to get average execution time
    public static double getAverageExecutionTime(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .mapToLong(job -> job.duration)
                .average()
                .orElse(0);
    }

    // Utility method to get maximum execution time
    public static long getMaxExecutionTime(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .mapToLong(job -> job.duration)
                .max()
                .orElse(0);
    }

    // Utility method to get minimum execution time
    public static long getMinExecutionTime(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .mapToLong(job -> job.duration)
                .min()
                .orElse(0);
    }

    // Utility method to get jobs by partition count
    public static Map<String, Long> getJobsByPartitionCount(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .collect(Collectors.groupingBy(job -> Optional.ofNullable(job.partition).orElse("UNKNOWN"), Collectors.counting()));
    }

    // Utility method to get jobs by node count
    public static Map<String, Long> getJobsByNodeCount(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .collect(Collectors.groupingBy(job -> Optional.ofNullable(job.nodeList).orElse("UNKNOWN"), Collectors.counting()));
    }

    // Utility method to get jobs by partition
    public static Map<String, List<Job>> getJobsByPartition(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .collect(Collectors.groupingBy(job -> Optional.ofNullable(job.partition).orElse("UNKNOWN")));
    }

    // Utility method to get jobs by node
    public static Map<String, List<Job>> getJobsByNode(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .collect(Collectors.groupingBy(job -> Optional.ofNullable(job.nodeList).orElse("UNKNOWN")));
    }

    // Utility method to get jobs by exit code
    public static Map<Integer, List<Job>> getJobsByExitCode(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .collect(Collectors.groupingBy(job -> job.exitCode));
    }

    // Utility method to get total execution time per partition
    public static Map<String, Long> getTotalExecutionTimePerPartition(HashMap<Integer, Job> jobMap) {
        return jobMap.values().stream()
                .collect(Collectors.groupingBy(job -> Optional.ofNullable(job.partition).orElse("UNKNOWN"),
                        Collectors.summingLong(job -> job.duration)));
    }

    @Override
    public String toString() {
        return "Job{" +
                "jobId=" + jobId +
                ", start=" + start +
                ", end=" + end +
                ", exitCode=" + exitCode +
                ", duration=" + duration +
                ", cpu='" + cpu + '\'' +
                ", partition='" + partition + '\'' +
                ", nodeList='" + nodeList + '\'' +
                '}';
    }
}
