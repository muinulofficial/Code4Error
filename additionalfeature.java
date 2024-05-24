
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class additionalfeature {

    public static void main(String[] args) {
        String logFilePath = "C:\\1 Sarah Labtop\\1 Sarah\\2 Bacholar degree\\1 2023 2034\\2 Sem2\\2 Classes\\WIX1002 FUNDAMENTALS OF PROGRAMMING\\fop assignment\\assignment\\Code4Error\\extracted_log.txt"; // Adjust this path as needed

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
