package edu.usc.csci572;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import edu.usc.csci572.beans.Fetch;
import edu.usc.csci572.beans.Url;
import edu.usc.csci572.beans.Visit;
import org.apache.hc.core5.http.impl.EnglishReasonPhraseCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

            // Calculate Fetch Statistics
            List<Fetch> fetches = crawlStats.getFetches();
            int totalFetches = fetches.size();
            long succeededFetchesCount = fetches.stream().filter(fetch -> fetch.getStatusCode() >= 200 && fetch.getStatusCode() < 300).count();
            long failedFetchesCount = fetches.stream().filter(fetch -> fetch.getStatusCode() >= 300).count();

            report.append(String.format("""
                    \nFetch Statistics
                    ================
                    # fetches attempted: %d
                    # fetches succeeded: %d
                    # fetches failed or aborted: %d
                    """, totalFetches, succeededFetchesCount, failedFetchesCount));

            // Calculate URLs count
            List<Url> urls = crawlStats.getUrls();
            int totalUrlsCount = urls.size();

            Set<String> uniqueUrls = new HashSet<>();
            Set<String> uniqueWithinUrls = new HashSet<>();
            Set<String> uniqueOutsideUrls = new HashSet<>();

            for(Url url: crawlStats.getUrls()) {
                uniqueUrls.add(url.getUrl());

                if(url.getWithinWebsite().equals("OK")) {
                    uniqueWithinUrls.add(url.getUrl());
                } else {
                    uniqueOutsideUrls.add(url.getUrl());
                }
            }

            report.append(String.format("""
                    \nOutgoing URLs:
                    ==============
                    Total URLs extracted: %d
                    # unique URLs extracted: %d
                    # unique URLs within News Site: %d
                    # unique URLs outside News Site: %d
                    """, totalUrlsCount, uniqueUrls.size(), uniqueWithinUrls.size(), uniqueOutsideUrls.size()));


            report.append("""
                    \nStatus Codes:
                    =============
                    """);

            // Count all status codes
            Map<Integer, Long> statusCodeCounts = fetches.stream().collect(Collectors.groupingBy(Fetch::getStatusCode, Collectors.counting()));

            // Loop and append all Status Codes + Counts as String
            for (Map.Entry<Integer, Long> pair : statusCodeCounts.entrySet()) {
                String statusCodeWithMessage = EnglishReasonPhraseCatalog.INSTANCE.getReason(pair.getKey(), Locale.ENGLISH);
                report.append(String.format("%s: %d\n",statusCodeWithMessage, pair.getValue()));
            }

            List<Visit> visits = crawlStats.getVisits();

            int[] fileSizeCountsByRange = new int[]{ 0, 0, 0, 0, 0 };

            int KB = 1024;
            int MB = 1024 * KB;

            for(Visit visit: visits) {
                long size = visit.getSize();
                if (size < KB) {
                    fileSizeCountsByRange[0]++;
                } else if (size < 10 * KB) {
                    fileSizeCountsByRange[1]++;
                } else if (size < 100 * KB) {
                    fileSizeCountsByRange[2]++;
                } else if (size < MB) {
                    fileSizeCountsByRange[3]++;
                } else {
                    fileSizeCountsByRange[4]++;
                }
            }

            report.append(String.format("""
                    \nFile Sizes:
                    ===========
                    < 1KB: %d
                    1KB ~ <10KB: %d
                    10KB ~ <100KB: %d
                    100KB ~ <1MB: %d
                    >= 1MB: %d
                    """, fileSizeCountsByRange[0], fileSizeCountsByRange[1], fileSizeCountsByRange[2], fileSizeCountsByRange[3], fileSizeCountsByRange[4]));

            report.append("""
                    \nContent Types:
                    ==============
                    """);

            // Count all Content-Types
            Map<String, Long> contentTypeCounts = visits.stream().collect(Collectors.groupingBy(Visit::getContentType, Collectors.counting()));

            // Loop and get all Content Types + Count as String
            for (Map.Entry<String, Long> pair : contentTypeCounts.entrySet()) {
                report.append(String.format("%s: %d\n", pair.getKey(), pair.getValue()));
            }

            writer.write(report.toString());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}
