/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package javaapplication28;

/**
 *
 * @author muinulislam
 */

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class assign {
    public static void main(String[] args) {
        String filepath = "/Users/muinulislam/Desktop/extracted_log";

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        int createdCount = 0;
        int endedCount = 0;

        try {
            BufferedReader rd = new BufferedReader(new FileReader(filepath));
            String line;

            while ((line = rd.readLine()) != null) {
                LocalDateTime timestamp = LocalDateTime.parse(line.substring(1, 24), DateTimeFormatter.ISO_DATE_TIME);
                if (startTime == null || timestamp.isBefore(startTime)) {
                    startTime = timestamp;
                }
                if (endTime == null || timestamp.isAfter(endTime)) {
                    endTime = timestamp;
                }
                if (line.contains("_slurm_rpc_submit_batch_job")) {
                    createdCount++;
                } else if (line.contains("_job_complete")) {
                    endedCount++;
                }
            }

            rd.close();

            System.out.println("Start time: " + startTime);
            System.out.println("End time: " + endTime);
            System.out.println("Number of jobs created: " + createdCount);
            System.out.println("Number of jobs ended: " + endedCount);

        } catch (IOException e) {
            e.printStackTrace(); } 
    
    
         String logFilePath = "/Users/muinulislam/Desktop/extracted_log"; 

        // Variables for job completion with exit status
        int completedWithZero = 0;
        int completedWithPositive = 0;
        Pattern exitStatusPattern = Pattern.compile("_job_complete: JobId=(\\d+) WEXITSTATUS (\\d+)");

        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (exitStatusPattern.matcher(line).find()) {
                    Matcher exitStatusMatcher = exitStatusPattern.matcher(line);
                    if (exitStatusMatcher.find()) {
                        int exitStatus = Integer.parseInt(exitStatusMatcher.group(2));
                        if (exitStatus == 0) {
                            completedWithZero++;
                        } else {
                            completedWithPositive++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the log file: " + e.getMessage());
        }

        // Print job completion with exit status information
        System.out.println("Job Completion Information:");
        System.out.println("Number of jobs completed without error WEXITSTATUS 0: " + completedWithZero);
        System.out.println("Number of jobs completed but with error WEXITSTATUS >0 : " + completedWithPositive);
    }
        
    
    
    }

