import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JobPartitionCounter3 {
    public static void main(String[] args) {
        String filePath = "/Users/syahid1011/Downloads/extracted_log";

        List<String> partitions = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("sched: Allocate")) {
                    String partition = line.substring(line.lastIndexOf("Partition=") + 10);
                    int index = partitions.indexOf(partition);
                    if (index == -1) {
                        partitions.add(partition);
                        counts.add(1);
                    } else {
                        counts.set(index, counts.get(index) + 1);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }

        for (int i = 0; i < partitions.size(); i++) {
            System.out.println(partitions.get(i) + " Jobs: " + counts.get(i));
        }
    }
}
