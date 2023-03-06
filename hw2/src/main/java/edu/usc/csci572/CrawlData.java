package edu.usc.csci572;

import com.opencsv.CSVWriter;
import edu.usc.csci572.beans.Fetch;
import edu.usc.csci572.beans.Url;
import edu.usc.csci572.beans.Visit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.nio.file.Files.*;

public class CrawlData {

    public static final Logger logger = LoggerFactory.getLogger(CrawlData.class);

    private final List<Url> urls;

    private final List<Fetch> fetches;

    private final List<Visit> visits;

    private final Set<String> uniqueUrls;

    private int totalUrls = 0;

    private static CrawlData _crawlData = null;

    private CrawlData() {
        this.urls = new ArrayList<>();
        this.fetches = new ArrayList<>();
        this.visits = new ArrayList<>();
        this.uniqueUrls = new LinkedHashSet<>();
    }

    public static synchronized CrawlData getInstance() {
        if (_crawlData == null) {
            _crawlData = new CrawlData();
        }
        return _crawlData;
    }

    public List<Url> getUrls() {
        return Collections.unmodifiableList(urls);
    }

    public synchronized void addUrl(Url url) {
        this.urls.add(url);
        this.uniqueUrls.add(url.getUrl());
    }

    public List<Fetch> getFetches() {
        return Collections.unmodifiableList(fetches);
    }

    public synchronized void addFetch(Fetch fetch) {
        this.fetches.add(fetch);
    }

    public List<Visit> getVisits() {
        return Collections.unmodifiableList(visits);
    }

    public synchronized void addVisit(Visit visit) {
        this.visits.add(visit);
    }

    public int getTotalUrls() {
        return totalUrls;
    }

    public synchronized void incTotalUrls() {
        this.totalUrls++;
    }


    public synchronized boolean find(String url) {
        return this.uniqueUrls.contains(url);
    }

    public static synchronized void saveToCsv(String outputDirectory, String domain) throws IOException {
        CrawlData crawlData = getInstance();

        // Create output directory if not present
        Utils.createDirectoryIfNotExists(outputDirectory);

        String identifier = domain.split("\\.")[0];

        try {
            // Write CSV files
            // Store URLs
            String urlsCsvFilePath = String.format("%s/urls_%s.csv", outputDirectory, identifier);
            boolean doesUrlsFilesExists = exists(Paths.get(urlsCsvFilePath));
            try (
                    FileWriter fileWriter = new FileWriter(urlsCsvFilePath, true);
                    CSVWriter csvWriter = new CSVWriter(fileWriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                            CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
            ) {
                if(!doesUrlsFilesExists) {
                    csvWriter.writeNext(new String[] {"URL", "URL Type"});
                }

                for(Url url: crawlData.urls) {
                    csvWriter.writeNext(new String[] { url.getUrl(), url.getWithinWebsite() });
                }
            }

            // Store Fetches
            String fetchCsvFilePath = String.format("%s/fetch_%s.csv", outputDirectory, identifier);
            boolean doesFetchFileExists = exists(Paths.get(fetchCsvFilePath));
            try (
                    FileWriter fileWriter = new FileWriter(fetchCsvFilePath, true);
                    CSVWriter csvWriter = new CSVWriter(fileWriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                            CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
            ) {
                if(!doesFetchFileExists) {
                    csvWriter.writeNext(new String[] {"URL", "Status"});
                }

                for(Fetch fetch: crawlData.fetches) {
                    csvWriter.writeNext(new String[] { fetch.getUrl(), String.valueOf(fetch.getStatusCode()) });
                }
            }

            // Store Visits
            String visitsCsvFilePath = String.format("%s/visit_%s.csv", outputDirectory, identifier);
            boolean doesVisitsFileExists = exists(Paths.get(visitsCsvFilePath));
            try (
                    FileWriter fileWriter = new FileWriter(visitsCsvFilePath, true);
                    CSVWriter csvWriter = new CSVWriter(fileWriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                            CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
            ) {
                if(!doesVisitsFileExists) {
                    csvWriter.writeNext(new String[] {"URL", "Size (bytes)", "# of Outlinks", "Content-Type"});
                }

                for(Visit visit: crawlData.visits) {
                    csvWriter.writeNext(new String[] { visit.getUrl(), String.valueOf(visit.getSize()), String.valueOf(visit.getNumOfOutlinks()), visit.getContentType() });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public synchronized void flush() {
        CrawlData instance = getInstance();
        instance.fetches.clear();
        instance.visits.clear();
        instance.urls.clear();
        instance.totalUrls = 0;
    }

    @Override
    public String toString() {
        return "CrawlData{" +
                "urls=" + urls +
                ", fetches=" + fetches +
                ", visits=" + visits +
                ", totalUrls=" + totalUrls +
                '}';
    }
}
