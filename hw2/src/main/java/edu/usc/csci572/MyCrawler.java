package edu.usc.csci572;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.usc.csci572.beans.Fetch;
import edu.usc.csci572.beans.Url;
import edu.usc.csci572.beans.Visit;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class MyCrawler extends WebCrawler {

    private static final Logger logger = LoggerFactory.getLogger(MyCrawler.class);

    private final String author;

    private final String id;

    private final int numberOfCrawlers;

    private final String orgWebsite;

    private final CrawlStats crawlStats;

    private final String outputDirectory;

    private final String domain;

    private final int batchSize;

    private final static Pattern EXCLUSIONS = Pattern.compile(".*(\\.(css|js|xml|mp3|mp4|zip|gz|json))$");

    private final static Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList("text/html", "application/pdf", "image/jpeg", "image/png", "image/bmp", "image/gif", "image/svg+xml", "image/tiff", "image/webp", "image/avif",
            "application/msword"));

    public MyCrawler(String author, String id, int numberOfCrawlers, String orgWebsite, CrawlStats crawlStats, String outputDirectory, String domain, int batchSize) {
        this.author = author;
        this.id = id;
        this.numberOfCrawlers = numberOfCrawlers;
        this.orgWebsite = orgWebsite;
        this.crawlStats = crawlStats;
        this.outputDirectory = outputDirectory;
        this.domain = domain;
        this.batchSize = batchSize;
    }

    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "orgWebsite". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String urlSrc = url.getURL();
        String href = urlSrc.toLowerCase();
        boolean residesInside = href.matches("https?://(www.)?" + domain + "/?.*");

        // Store all the URLs checked or visited and also mention whether it is within the website or not
        this.crawlStats.addUrl(new Url(
                url.getDocid(),
                urlSrc,
                residesInside ? Url.ResidesWithinWebsite.OK : Url.ResidesWithinWebsite.N_OK
        ));
        this.crawlStats.addUniqueUrl(urlSrc, residesInside);

        boolean shouldVisit = !EXCLUSIONS.matcher(href).matches() && residesInside;

        return shouldVisit;
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL();
        int statusCode = page.getStatusCode();
        String statusMessage = EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, Locale.ENGLISH);

        this.crawlStats.incStatusCodeCounts(String.format("%d %s", statusCode, statusMessage.toUpperCase())); // Update status code count stat

        this.crawlStats.addFetch(new Fetch(docid, url, statusCode)); // record new fetch

        this.crawlStats.incNumOfFetches(); // increment number of fetches
        this.crawlStats.incTotalUrls(); // increment total visited urls

        // Add content type for current fetch; avoid charset=utf-8
        String contentType = page.getContentType().split(";")[0];

        logger.debug("Docid: {}, Url: {}, Content-Type: {}", docid, url, contentType);
        if(!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            logger.debug("Content-Type: {}", contentType);
            return;
        }

        if (statusCode >= 200 && statusCode < 300) {
            this.crawlStats.incNumOfSuccessfulFetches(); // Increment Successful Visit Count

            Visit visit = new Visit(); // create a new Visit
            visit.setDocid(docid);
            visit.setUrl(url);

            if (page.getParseData() instanceof HtmlParseData htmlParseData) {
                Set<WebURL> links = htmlParseData.getOutgoingUrls();

                visit.setNumOfOutlinks(links.size()); // Store number of outlinks
            }

            visit.setContentType(contentType);

            this.crawlStats.addContentTypeCount(contentType); // count Content-Type occurrences

            // Add content length for current fetch
            int size = page.getContentData().length;
            visit.setSize(size);

            this.crawlStats.addFileSizeCount(size); // count Size range

            this.crawlStats.addVisit(visit); // add Visit to the stats
        } else {
            this.crawlStats.incNumOfFailedFetches(); // Increment failed/aborted visit counts
        }

        // Save data based on after every batchSize number of fetches
        synchronized (this) {
            if (crawlStats.getTotalUrls() % batchSize == 0) {
                Utils.writeStats(outputDirectory, domain, crawlStats, author, id, numberOfCrawlers);
            }
        }
    }

    @Override
    public Object getMyLocalData() {
        return this.crawlStats;
    }

}
