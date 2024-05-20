package Azka;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class SlurmLogErrorUserCounter {

    public static void main(String[] args) {
        String logFilePath = "C:\\Users\\azka\\Downloads\\extracted_log.txt";
        
        int errorJobs = 0;
        Map<String, Integer> userErrorCount = new HashMap<>();

        Pattern errorPattern = Pattern.compile("error:.*user='(\\S+)'");

        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher errorMatcher = errorPattern.matcher(line);
                if (errorMatcher.find()) {
                    errorJobs++;
                    String user = errorMatcher.group(1);
                    userErrorCount.put(user, userErrorCount.getOrDefault(user, 0) + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Print the total number of job errors
        System.out.println("Number of jobs causing errors: " + errorJobs);

        // Print the table header
        System.out.printf("%-20s %-10s%n", "User", "Error Count");
        System.out.println("-------------------- ----------");

        // Print the error counts per user in a table format
        userErrorCount.forEach((user, count) -> 
            System.out.printf("%-20s %-10d%n", user, count)
        );
    }
}

