package edu.usc.csci572;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Parameter(names = {"--seed-url", "-u"}, description = "URL of the website to crawl.")
    private String seedUrl = "https://www.usatoday.com/";

    @Parameter(names = {"--max-pages", "-mp"}, description = "The maximum number of pages to crawl.")
    private int maxPages = 20000;

    @Parameter(names = {"--max-depth", "-d"}, description = "The maximum depth to crawl to.")
    private int maxDepth = 16;

    @Parameter(names = {"--num-crawlers", "-nt"}, description = "Number of crawlers/thread to create.")
    private int numberOfCrawlers = 8;

    @Parameter(names = {"--politeness", "-p"}, description = "The politeness delay specifies to number of milliseconds"
            + "to wait between requests.")
    private int politenessDelay = 2000;

    @Parameter(names = {"--output-dir", "-od"}, description = "Directory to store the statistics in.")
    private String outputDirectory = "submission/";

    public static void main(String[] args) {
        Main main = new Main();

        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);
        main.run();
    }

    public void run() {
        try {
            logger.debug("Seed URL: {}", seedUrl);
            logger.debug("Max Pages: {}", maxPages);
            logger.debug("Max Depth: {}", maxDepth);
            logger.debug("Number of Crawlers: {}", numberOfCrawlers);
            logger.debug("Politeness Delay: {} ms", politenessDelay);

            // Exit if the URL is not valid
            if(!isValidURL(seedUrl)) {
                logger.error("Invalid URL: {}", seedUrl);
                System.exit(1);
            }

            String crawlStorageFolder = "results";
            int numberOfCrawlers = this.numberOfCrawlers;

            CrawlConfig config = new CrawlConfig();
            config.setCrawlStorageFolder(crawlStorageFolder);
            config.setMaxPagesToFetch(maxPages);
            config.setPolitenessDelay(politenessDelay);
            config.setMaxDepthOfCrawling(maxDepth);

            // Instantiate the controller for this crawl.
            PageFetcher pageFetcher = new PageFetcher(config);
            RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
            RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
            CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

            // For each crawl, you need to add some seed urls. These are the first
            // URLs that are fetched and then the crawler starts following links
            // which are found in these pages
            controller.addSeed(seedUrl);

            // The factory which creates instances of crawlers.
            CrawlStats crawlStats = CrawlStats.getInstance();
            CrawlController.WebCrawlerFactory<MyCrawler> factory = () -> new MyCrawler(seedUrl, crawlStats);

            // Start the crawl. This is a blocking operation, meaning that your code
            // will reach the line after this only when crawling is finished.
            controller.start(factory, numberOfCrawlers);

            // NOTE: No need to aggregate thread results as CrawlStats is Singleton and thread-safe

            // Get domain name
            String domain = seedUrl.replaceAll("http(s)?://|www\\.|/.*", "").toLowerCase();

            // Store Stats
            Utils.writeStats(outputDirectory, domain, crawlStats);

            logger.debug("Done.");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

}