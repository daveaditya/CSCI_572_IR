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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static void createDirectoryIfNotExists(String directory) {
        // Create output directory if not present
        File dir = new File(directory);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException(String.format("Cannot create output path: %s", directory));
            }
        }
    }


    public static synchronized void saveToCsv(String outputDirectory, String domain, CrawlData crawlData) {
        // Create output directory if not present
        createDirectoryIfNotExists(outputDirectory);

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
                        .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                        .build();

                sbc.write(crawlData.getUrls());
            }

            // Store Fetches
            Path fetchCsvFilePath = Paths.get(String.format("%s/fetch_%s.csv", outputDirectory, identifier));
            try (BufferedWriter writer = Files.newBufferedWriter(fetchCsvFilePath)) {
                CustomMappingStrategy<Fetch> fetchCustomMappingStrategy = new CustomMappingStrategy<>();
                fetchCustomMappingStrategy.setType(Fetch.class);

                StatefulBeanToCsv<Fetch> sbc = new StatefulBeanToCsvBuilder<Fetch>(writer)
                        .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                        .withMappingStrategy(fetchCustomMappingStrategy)
                        .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                        .build();

                sbc.write(crawlData.getFetches());
            }

            // Store Visits
            Path visitsCsvFilePath = Paths.get(String.format("%s/visit_%s.csv", outputDirectory, identifier));
            try (BufferedWriter writer = Files.newBufferedWriter(visitsCsvFilePath)) {
                CustomMappingStrategy<Visit> visitCustomMappingStrategy = new CustomMappingStrategy<>();
                visitCustomMappingStrategy.setType(Visit.class);

                StatefulBeanToCsv<Visit> sbc = new StatefulBeanToCsvBuilder<Visit>(writer)
                        .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                        .withMappingStrategy(visitCustomMappingStrategy)
                        .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                        .build();

                sbc.write(crawlData.getVisits());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    public static synchronized void generateReport(String outputDirectory, String domain, String author, String id, int nThreads) {
        // Create output directory if not present
        createDirectoryIfNotExists(outputDirectory);

        String identifier = domain.split("\\.")[0];

        Path reportFilePath = Paths.get(String.format("%s/CrawlReport_%s.txt", outputDirectory, identifier));
        Path fetchFilePath = Paths.get(String.format("%s/fetch_%s.csv", outputDirectory, identifier));
        Path urlsFilePath = Paths.get(String.format("%s/urls_%s.csv", outputDirectory, identifier));
        Path visitFilePath = Paths.get(String.format("%s/visit_%s.csv", outputDirectory, identifier));

        try (
            BufferedWriter writer = Files.newBufferedWriter(reportFilePath)
        ) {
            StringBuilder report = new StringBuilder();

            report.append(String.format("""
                    Name: %s
                    USC ID: %s
                    News site crawled: %s
                    Number of threads: %d
                    """, author, id, domain, nThreads));

            // Calculate Fetch Statistics
            Iterator<Fetch> fetchStream = Fetch.loadFetchCsvStream(fetchFilePath).iterator();

            Fetch.Stats fetchStats = new Fetch.Stats();
            for (Iterator<Fetch> it = fetchStream; it.hasNext(); ) {
                Fetch fetch = it.next();

                fetchStats.incTotalFetchCount();
                if(fetch.getStatusCode() >= 200 && fetch.getStatusCode() < 300) {
                    fetchStats.incSucceededFetchCount();
                } else {
                    fetchStats.incFailedFetchCount();
                }

                fetchStats.updateStatusCodeCount(fetch.getStatusCode());
            }

            // Calculate URLs count
            Url.Stats urlStats = new Url.Stats();

            // Using BufferedReader directly as OpenCSV stream iterator had memory issues
            Iterator<String> urlIterator = Files.newBufferedReader(urlsFilePath).lines().iterator();

            for (Iterator<String> it = urlIterator; it.hasNext(); ) {
                String[] row = it.next().split(",");
                urlStats.addUrls(row[0].replace("\"", ""), row[1].replace("\"", ""));
            }

            Iterator<Visit> visitIterator = Visit.loadVisitCsvStream(visitFilePath).iterator();
            Visit.Stats visitStats = new Visit.Stats();

            for (Iterator<Visit> it = visitIterator; it.hasNext();) {
                Visit visit = it.next();
                visitStats.addOutlinks(visit.getNumOfOutlinks());
                visitStats.addContentType(visit);
                visitStats.countFileSize(visit);
            }

            report.append(String.format("""
                    \nFetch Statistics
                    ================
                    # fetches attempted: %d
                    # fetches succeeded: %d
                    # fetches failed or aborted: %d
                    """, fetchStats.getTotalFetchCount(), fetchStats.getSucceededFetchCount(), fetchStats.getFailedFetchCount()));


            report.append(String.format("""
                    \nOutgoing URLs:
                    ==============
                    Total URLs extracted: %d
                    # unique URLs extracted: %d
                    # unique URLs within News Site: %d
                    # unique URLs outside News Site: %d
                    """, visitStats.getTotalNumOfOutlinks(), urlStats.getUniqueUrlCount(), urlStats.getUniqueWithinUrlCount(), urlStats.getUniqueOutsideUrlCount()));

            report.append("""
                    \nStatus Codes:
                    =============
                    """);

            // Loop and append all Status Codes + Counts as String
            for (Map.Entry<Integer, Integer> pair : fetchStats.getStatusCodeCounts().entrySet()) {
                String statusCodeWithMessage = EnglishReasonPhraseCatalog.INSTANCE.getReason(pair.getKey(), Locale.ENGLISH);
                report.append(String.format("%d %s: %d\n", pair.getKey(), statusCodeWithMessage, pair.getValue()));
            }

            int[] fileSizeCountsByRange = visitStats.getFileSizeCountsByRange();

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

            // Loop and get all Content Types + Count as String
            for (Map.Entry<String, Long> pair : visitStats.getContentTypeCounts().entrySet()) {
                report.append(String.format("%s: %d\n", pair.getKey(), pair.getValue()));
            }

            writer.write(report.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
