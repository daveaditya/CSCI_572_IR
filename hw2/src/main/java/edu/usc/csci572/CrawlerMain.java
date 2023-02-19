package edu.usc.csci572;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class CrawlerMain {

    @Parameter(names = {"--max-pages", "-p"})
    private int maxPages;

    @Parameter(names = {"--max-depth", "-d"})
    private int maxDepth;

    public static void main(String[] args) throws Exception {
        CrawlerMain crawlerMain = new CrawlerMain();

        JCommander.newBuilder()
                .addObject(crawlerMain)
                .build()
                .parse(args);
        crawlerMain.run();
    }

    public void run() {

        System.out.printf("%d %d", maxPages, maxDepth);

       /* String crawlStorageFolder = "./data/crawl/root";
        int numberOfCrawlers = 7;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);

        // Instantiate the controller for this crawl.
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        // For each crawl, you need to add some seed urls. These are the first
        // URLs that are fetched and then the crawler starts following links
        // which are found in these pages
        controller.addSeed("https://www.ics.uci.edu/~lopes/");
        controller.addSeed("https://www.ics.uci.edu/~welling/");
        controller.addSeed("https://www.ics.uci.edu/");

        // The factory which creates instances of crawlers.
        CrawlController.WebCrawlerFactory<WebCrawler> factory = MyCrawler::new;

        // Start the crawl. This is a blocking operation, meaning that your code
        // will reach the line after this only when crawling is finished.
        controller.start(factory, numberOfCrawlers);*/
    }

}