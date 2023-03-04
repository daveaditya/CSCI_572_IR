package edu.usc.csci572;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import edu.usc.csci572.beans.Fetch;
import edu.usc.csci572.beans.Url;
import edu.usc.csci572.beans.Visit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private static void createOutputDirectoryIfNotExists(String outputDirectory) {
        // Create output directory if not present
        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException(String.format("Cannot create output path: %s", outputDirectory));
            }
        }
    }

    public static synchronized void writeCsvStats(String outputDirectory, String domain, CrawlStats crawlStats) {
        // Create output directory if not present
        createOutputDirectoryIfNotExists(outputDirectory);

        String identifier = domain.split("\\.")[0];

        try {
            // Write CSV files
            // Store URLs
            Path urlsCsvFilePath = Paths.get(String.format("%s/urls_%s.csv", outputDirectory, identifier));
            try (BufferedWriter writer = Files.newBufferedWriter(urlsCsvFilePath)) {
                CustomMappingStrategy<Url> urlCustomMappingStrategy = new CustomMappingStrategy<>();
                urlCustomMappingStrategy.setType(Url.class);

                StatefulBeanToCsv<Url> sbc = new StatefulBeanToCsvBuilder<Url>(writer)
                        .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                        .withMappingStrategy(urlCustomMappingStrategy)
                        .build();

                sbc.write(crawlStats.getUrls());
            }

            // Store Fetches
            Path fetchCsvFilePath = Paths.get(String.format("%s/fetch_%s.csv", outputDirectory, identifier));
            try (BufferedWriter writer = Files.newBufferedWriter(fetchCsvFilePath)) {
                CustomMappingStrategy<Fetch> fetchCustomMappingStrategy = new CustomMappingStrategy<>();
                fetchCustomMappingStrategy.setType(Fetch.class);

                StatefulBeanToCsv<Fetch> sbc = new StatefulBeanToCsvBuilder<Fetch>(writer)
                        .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                        .withMappingStrategy(fetchCustomMappingStrategy)
                        .build();

                sbc.write(crawlStats.getFetches());
            }

            // Store Visits
            Path visitsCsvFilePath = Paths.get(String.format("%s/visit_%s.csv", outputDirectory, identifier));
            try (BufferedWriter writer = Files.newBufferedWriter(visitsCsvFilePath)) {
                CustomMappingStrategy<Visit> visitCustomMappingStrategy = new CustomMappingStrategy<>();
                visitCustomMappingStrategy.setType(Visit.class);

                StatefulBeanToCsv<Visit> sbc = new StatefulBeanToCsvBuilder<Visit>(writer)
                        .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                        .withMappingStrategy(visitCustomMappingStrategy)
                        .build();

                sbc.write(crawlStats.getVisits());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    public static synchronized void writeStatsReport(String outputDirectory, String domain, CrawlStats crawlStats, String author, String id, int nThreads) {
        String identifier = domain.split("\\.")[0];

        // Write Report
        Path reportFilePath = Paths.get(String.format("%s/CrawlReport_%s.txt", outputDirectory, identifier));
        try (BufferedWriter writer = Files.newBufferedWriter(reportFilePath)) {
            StringBuilder report = new StringBuilder();

            report.append(String.format("""
                    Name: %s
                    USC ID: %s
                    News site crawled: %s
                    Number of threads: %d
                    """, author, id, domain, nThreads));

            report.append(String.format("""
                    \nFetch Statistics
                    ================
                    # fetches attempted: %d
                    # fetches succeeded: %d
                    # fetches failed or aborted: %d
                    """, 0, 0, 0));

            report.append(String.format("""
                    \nOutgoing URLs:
                    ==============
                    Total URLs extracted:
                    # unique URLs extracted: %d
                    # unique URLs within News Site: %d
                    # unique URLs outside News Site: %d
                    """, 0, 0,0));


            report.append("""
                    \nStatus Codes:
                    =============
                    """);

            // Loop and get all Status Codes + Counts as String
//            for (Map.Entry<String, Integer> pair : crawlStats.getStatusCodeCounts().entrySet()) {
//                report.append(String.format("%s: %d\n", pair.getKey(), pair.getValue()));
//            }

            int[] fileSizeCounts = new int[]{ 0, 0, 0, 0, 0 };
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
//            for (Map.Entry<String, Integer> pair : crawlStats.getContentTypeCounts().entrySet()) {
//                report.append(String.format("%s: %d\n", pair.getKey(), pair.getValue()));
//            }

            writer.write(report.toString());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}
