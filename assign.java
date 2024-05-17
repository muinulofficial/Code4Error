package Code4Error;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;


public class assign {
    public static void main(String[] args) {
        String filepath = "C:\\1 Sarah Labtop\\1 Sarah\\2 Bacholar degree\\1 2023 2034\\2 Sem2\\2 Classes\\WIX1002 FUNDAMENTALS OF PROGRAMMING\\fop assignment\\assignment\\Code4Error\\extracted_log.txt";

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
            e.printStackTrace(); } }
}
