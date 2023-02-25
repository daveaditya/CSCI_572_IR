package edu.usc.csci572;

import com.opencsv.CSVWriter;
import com.opencsv.bean.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static <T> void writeCsv(Path path, List<T> data) throws Exception {
        CustomMappingStrategy<T> strategy = new CustomMappingStrategy<>();

        // WARN:
        //noinspection unchecked
        strategy.setType((Class<? extends T>) data.get(0).getClass());

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            StatefulBeanToCsv<T> sbc = new StatefulBeanToCsvBuilder<T>(writer)
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .withMappingStrategy(strategy)
                    .build();

            sbc.write(data);
        }
    }

    public static synchronized void writeStats(String outputDirectory, String domain, CrawlStats crawlStats) {
        // Create output directory if not present
        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException(String.format("Cannot create output path: %s", outputDirectory));
            }
        }

        String identifier = domain.split("\\.")[0];

        // Write Report
        Path reportFilePath = Paths.get(String.format("%s/CrawlReport_%s.txt", outputDirectory, identifier));
        try (BufferedWriter writer = Files.newBufferedWriter(reportFilePath)) {
            // Write CSV files
            // Store URLs
            Path urlsCsvFilePath = Paths.get(String.format("%s/urls_%s.csv", outputDirectory, identifier));
            Utils.writeCsv(urlsCsvFilePath, crawlStats.getUrls());

            // Store Fetches
            Path fetchCsvFilePath = Paths.get(String.format("%s/fetch_%s.csv", outputDirectory, identifier));
            Utils.writeCsv(fetchCsvFilePath, crawlStats.getFetches());

            // Store Visits
            Path visitsCsvFilePath = Paths.get(String.format("%s/visit_%s.csv", outputDirectory, identifier));
            Utils.writeCsv(visitsCsvFilePath, crawlStats.getVisits());

            StringBuilder report = new StringBuilder();

            report.append(String.format("""
                    Name: %s
                    USC ID: %s
                    News site crawled: %s
                    Number of threads: %d
                    """, "Aditya Ashok Dave", "xxxxxxxx", "usatoday.com", 8));

            report.append(String.format("""
                    \nFetch Statistics
                    ================
                    # fetches attempted: %d
                    # fetches succeeded: %d
                    # fetches failed or aborted: %d
                    """, crawlStats.getNumOfFetches(), crawlStats.getNumOfSuccessfulFetches(), crawlStats.getNumOfFailedFetches()));

            report.append(String.format("""
                    \nOutgoing URLs:
                    ==============
                    Total URLs extracted:
                    # unique URLs extracted: %d
                    # unique URLs within News Site: %d
                    # unique URLs outside News Site: %d
                    """, 1, 1, 1));

            int[] fileSizeCounts = crawlStats.getFileSizeByRangeCounts();
            report.append("""
                    \nStatus Codes:
                    =============
                    """);

            // Loop and get all Status Codes + Counts as String
            for (Map.Entry<String, Integer> pair : crawlStats.getStatusCodeCounts().entrySet()) {
                report.append(String.format("%s: %d\n", pair.getKey(), pair.getValue()));
            }

            report.append(String.format("""
                    \nFile Sizes:
                    ===========
                    < 1KB: %d
                    1KB ~ <10KB: %d
                    10KB ~ <100KB: %d
                    100KB ~ <1MB: %d
                    >= 1MB: %d
                    """, fileSizeCounts[0], fileSizeCounts[1], fileSizeCounts[2], fileSizeCounts[3], fileSizeCounts[4]));

            report.append("""
                    \nContent Types:
                    ==============
                    """);

            // Loop and get all Content Types + Count as String
            for (Map.Entry<String, Integer> pair : crawlStats.getContentTypeCounts().entrySet()) {
                report.append(String.format("%s: %d\n", pair.getKey(), pair.getValue()));
            }

            writer.write(report.toString());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}
