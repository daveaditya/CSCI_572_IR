package edu.usc.csci572;

import edu.usc.csci572.beans.Fetch;
import edu.usc.csci572.beans.Url;
import edu.usc.csci572.beans.Visit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrawlStats {

    private List<Url> urls;

    private List<Fetch> fetches;

    private List<Visit> visits;

    private int totalUrls = 0;

    private static CrawlStats _crawlStats = null;

    private CrawlStats() {
        this.urls = new ArrayList<>();
        this.fetches = new ArrayList<>();
        this.visits = new ArrayList<>();
    }

    public static synchronized CrawlStats getInstance() {
        if (_crawlStats == null) {
            _crawlStats = new CrawlStats();
        }
        return _crawlStats;
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

    public static CrawlStats loadFromCsv(List<Url> urls, List<Fetch> fetches, List<Visit> visits) {
        CrawlStats instance = CrawlStats.getInstance();
        instance.urls = urls;
        instance.visits = visits;
        instance.fetches = fetches;
        return instance;
    }

    @Override
    public String toString() {
        return "CrawlStats{" +
                "urls=" + urls +
                ", fetches=" + fetches +
                ", visits=" + visits +
                ", totalUrls=" + totalUrls +
                '}';
    }
}
