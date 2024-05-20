import java.io.*;
import java.util.*;

public class JobErrorReport {
    public static void main(String[] args) {
        String path = "C:\\Users\\azka\\Downloads\\extracted_log.txt";
        HashMap<String, Integer> errorCountPerUser = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("error:")) { // Check for lines indicating errors
                    // Extracting the user ID from the error line
                    // Assuming the format is "error: ... user='username', ..."
                    int startIndex = line.indexOf("user='") + 6; // Start of the username
                    if (startIndex >= 6) { // Ensures "user='" was found
                        int endIndex = line.indexOf("'", startIndex); // End of the username
                        if (endIndex != -1) { // Ensures closing quote was found
                            String userID = line.substring(startIndex, endIndex);
                            errorCountPerUser.put(userID, errorCountPerUser.getOrDefault(userID, 0) + 1);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, Integer> entry : errorCountPerUser.entrySet()) {
            System.out.println("User: " + entry.getKey() + ", Number of Errors: " + entry.getValue());
        }
    }
}

