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

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class MyCrawler extends WebCrawler {

    private static final Logger logger = LoggerFactory.getLogger(MyCrawler.class);

    private final String orgWebsite;

    private final CrawlStats stats;

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(html|doc|docx|pdf|bmp|gif|jpeg|jpg|png|svg))$");

    public MyCrawler(String orgWebsite, CrawlStats stats) {
        this.orgWebsite = orgWebsite;
        this.stats = stats;
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
        String href = url.getURL().toLowerCase();
        boolean residesInside = href.startsWith(this.orgWebsite);

        // Store all the URLs checked or visited and also mention whether it is within the website or not
        this.stats.addUrl(new Url(
                url.getDocid(),
                url.getURL(),
                residesInside ? Url.ResidesWithinWebsite.OK : Url.ResidesWithinWebsite.N_OK
        ));

        return FILTERS.matcher(href).matches() && residesInside;
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

        this.stats.incStatusCodeCounts(String.format("%d %s", statusCode, statusMessage.toUpperCase())); // Update status code count stat

        this.stats.addFetch(new Fetch(docid, url, statusCode)); // record new fetch

        this.stats.incNumOfFetches(); // increment number of fetches
        this.stats.incTotalUrls(); // increment total visited urls

        if(statusCode == 200) {
            this.stats.incNumOfSuccessfulFetches();             // Increment Successful Visit Count

            Visit visit = new Visit(); // create a new Visit
            visit.setDocid(docid);
            visit.setUrl(url);

            if (page.getParseData() instanceof HtmlParseData htmlParseData) {
                Set<WebURL> links = htmlParseData.getOutgoingUrls();

                visit.setNumOfOutlinks(links.size()); // Store number of outlinks
            }

            // Add content type for current fetch; avoid charset=utf-8
            String contentType = page.getContentType().split(";")[0];
            visit.setContentType(contentType);

            this.stats.addContentTypeCount(contentType); // count Content-Type occurrences

            // Add content length for current fetch
            int size = page.getContentData().length;
            visit.setSize(size);

            this.stats.addFileSizeCount(size); // count Size range

            this.stats.addVisit(visit); // add Visit to the stats
        } else {
            this.stats.incNumOfFailedFetches(); // Increment failed/aborted visit counts
        }

        // TODO: Save data based on frequency like after every 50 pages
    }

    @Override
    public Object getMyLocalData() {
        return this.stats;
    }

}
