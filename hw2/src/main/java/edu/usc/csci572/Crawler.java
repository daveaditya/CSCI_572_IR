package edu.usc.csci572;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.usc.csci572.beans.Fetch;
import edu.usc.csci572.beans.Url;
import edu.usc.csci572.beans.Visit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Crawler extends WebCrawler {

    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);

    private final CrawlData crawlData;

    private final String outputDirectory;

    private final String domain;

    private final int batchSize;

    private final static Pattern EXCLUSIONS = Pattern.compile(".*(\\.(css|js|xml|mp3|mp4|zip|gz|json))$");

    private final static Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList("text/html", "application/pdf", "image/jpeg", "image/png", "image/bmp", "image/gif", "image/svg+xml", "image/tiff", "image/webp", "image/avif",
            "application/msword"));

    public Crawler(CrawlData crawlData, String outputDirectory, String domain, int batchSize) {
        this.crawlData = crawlData;
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
        this.crawlData.addUrl(new Url(
                url.getDocid(),
                urlSrc.replaceAll(",", "_"), // replace comma by underscore as required by the homework
                residesInside ? "OK" : "N_OK"
        ));

        return !EXCLUSIONS.matcher(href).matches() && residesInside;
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL().replaceAll(",", "_");
        int statusCode = page.getStatusCode();

        // Get content type for current fetch; avoid charset=utf-8
        String contentType = page.getContentType().split(";")[0];

        // IMPORTANT: Need to check if the content is the desired on as some URLs might not have extensions
        // If the content type is not allowed, skip it.
        // NO statistics should be taken into account for if content type is not desired one
        if(!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            logger.debug("Docid: {}, Url: {}, Content-Type: {}", docid, url, contentType);
            return;
        }

        this.crawlData.addFetch(new Fetch(docid, url, statusCode)); // record new fetch

        this.crawlData.incTotalUrls(); // increment total visited urls

        logger.debug("HERE### Total URLs: {}", crawlData.getTotalUrls());
        logger.debug("Docid: {}, Url: {}, Content-Type: {}", docid, url, contentType);

        if (statusCode >= 200 && statusCode < 300) {
            Visit visit = new Visit(); // create a new Visit
            visit.setDocid(docid);
            visit.setUrl(url);

            if (page.getParseData() instanceof HtmlParseData htmlParseData) {
                Set<WebURL> links = htmlParseData.getOutgoingUrls();

                visit.setNumOfOutlinks(links.size()); // Store number of outlinks
            }

            visit.setContentType(contentType);

            // Add content length for current fetch
            int size = page.getContentData().length;
            visit.setSize(size);

            this.crawlData.addVisit(visit); // add Visit to the stats
        }

        // Save data based on after every batchSize number of fetches
        synchronized (this) {
            if (crawlData.getTotalUrls() % batchSize == 0) {
                logger.debug("Saving stats now...");
                CrawlData.saveToCsv(outputDirectory, domain);
                crawlData.flush();
            }
        }
    }

    @Override
    public Object getMyLocalData() {
        return this.crawlData;
    }

}
