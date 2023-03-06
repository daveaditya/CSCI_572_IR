package edu.usc.csci572;

import com.opencsv.CSVWriter;
import edu.usc.csci572.beans.Fetch;
import edu.usc.csci572.beans.Url;
import edu.usc.csci572.beans.Visit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static edu.usc.csci572.Utils.createDirectoryIfNotExists;

public class CrawlData {

    public static final Logger logger = LoggerFactory.getLogger(CrawlData.class);

    private final List<Url> urls;

    private final List<Fetch> fetches;

    private final List<Visit> visits;

    private int totalUrls = 0;

    private static CrawlData _crawlData = null;

    private CrawlData() {
        this.urls = new ArrayList<>();
        this.fetches = new ArrayList<>();
        this.visits = new ArrayList<>();
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


    public void flush() {
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
