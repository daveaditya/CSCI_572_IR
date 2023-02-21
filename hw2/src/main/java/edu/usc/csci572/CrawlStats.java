package edu.usc.csci572;

import edu.usc.csci572.beans.Fetch;
import edu.usc.csci572.beans.Url;
import edu.usc.csci572.beans.Visit;

import java.util.*;

public class CrawlStats {

    private final List<Url> urls;

    private final List<Fetch> fetches;

    private final List<Visit> visits;

    private int numOfFetches = 0;

    private int numOfSuccessfulFetches = 0;

    private int numOfFailedFetches = 0;

    private int totalUrls = 0;

    private final Map<String, Integer> statusCodeCounts = new HashMap<>();

    private final int[] fileSizeByRangeCounts = {0, 0, 0, 0, 0};

    private final Map<String, Integer> contentTypeCounts = new HashMap<>();

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

    public int getNumOfFetches() {
        return numOfFetches;
    }

    public synchronized void incNumOfFetches() {
        this.numOfFetches++;
    }

    public int getNumOfSuccessfulFetches() {
        return numOfSuccessfulFetches;
    }

    public synchronized void incNumOfSuccessfulFetches() {
        this.numOfSuccessfulFetches++;
    }

    public int getNumOfFailedFetches() {
        return numOfFailedFetches;
    }

    public synchronized void incNumOfFailedFetches() {
        this.numOfFailedFetches++;
    }

    public int getTotalUrls() {
        return totalUrls;
    }

    public synchronized void incTotalUrls() {
        this.totalUrls++;
    }

    public Map<String, Integer> getStatusCodeCounts() {
        return Collections.unmodifiableMap(statusCodeCounts);
    }

    public synchronized void incStatusCodeCounts(String statusCode) {
        if (this.statusCodeCounts.containsKey(statusCode)) {
            this.statusCodeCounts.replace(statusCode, this.statusCodeCounts.get(statusCode) + 1);
        } else {
            this.statusCodeCounts.put(statusCode, 1);
        }
    }

    public int[] getFileSizeByRangeCounts() {
        return fileSizeByRangeCounts;
    }

    public synchronized void addFileSizeCount(int size) {
        int KB = 1024;
        int MB = 1024 * KB;

        if (size < KB) {
            this.fileSizeByRangeCounts[0]++;
        } else if (size < 10 * KB) {
            this.fileSizeByRangeCounts[1]++;
        } else if (size < 100 * KB) {
            this.fileSizeByRangeCounts[2]++;
        } else if (size < MB) {
            this.fileSizeByRangeCounts[3]++;
        } else {
            this.fileSizeByRangeCounts[4]++;
        }
    }

    public Map<String, Integer> getContentTypeCounts() {
        return Collections.unmodifiableMap(contentTypeCounts);
    }

    public synchronized void addContentTypeCount(String contentType) {
        if (this.contentTypeCounts.containsKey(contentType)) {
            this.contentTypeCounts.replace(contentType, this.contentTypeCounts.get(contentType));
        } else {
            this.contentTypeCounts.putIfAbsent(contentType, 1);
        }
    }

    @Override
    public String toString() {
        return "CrawlStats{" +
                "urls=" + urls +
                ", fetches=" + fetches +
                ", visits=" + visits +
                ", numOfFetches=" + numOfFetches +
                ", numOfSuccessfulFetches=" + numOfSuccessfulFetches +
                ", numOfFailedFetches=" + numOfFailedFetches +
                ", totalUrls=" + totalUrls +
                ", statusCodeCounts=" + statusCodeCounts +
                ", fileSizeByRangeCounts=" + Arrays.toString(fileSizeByRangeCounts) +
                ", contentTypeCounts=" + contentTypeCounts +
                '}';
    }
}
