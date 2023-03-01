# CSCI-572 Homework 02

### Requirements

1. Java: 19.0.1 Temurin
2. Gradle: 7.6

### Quick Start

1. Clone this project [https://github.com/daveaditya/CSCI_572_IR](https://github.com/daveaditya/CSCI_572_IR)

2. Install SDKMAN! and Java

```bash
$ curl -s "https://get.sdkman.io" | bash # install SDKMAN! (https://sdkman.io)

$ source "$HOME/.sdkman/bin/sdkman-init.sh" # add SDKMAN! to shell start

$ sdk --version # should return sdk man version

$ sdk install java 19.0.1-tem
```

3. Open project in terminal.
4. Execute run.sh script: `./run.sh <parameters>`. This parameters can be obtained from the program help.

### Program Help (`./run.sh --help`)

```text
Usage: hw2-1.0.jar [options]
  Options:
    --author
      Author name for the generated report.
      Default: John Doe
    --batch-size, -bs
      Save after every x fetches.
      Default: 50
    --help, -h
      Prints the usage of this program.
    --id
      ID for the author in the generated report.
      Default: xxxxxxxxxx
    --max-depth, -d
      The maximum depth to crawl to.
      Default: 16
    --max-pages, -mp
      The maximum number of pages to crawl.
      Default: 20000
    --num-crawlers, -nt
      Number of crawlers/thread to create.
      Default: 8
    --output-dir, -od
      Directory to store the statistics in.
      Default: submission/
    --politeness, -p
      The politeness delay specifies to number of milliseconds to wait between 
      requests. 
      Default: 2000
    --seed-url, -u
      URL of the website to crawl.
      Default: https://www.usatoday.com
```

### Description

Your task is to configure and compile the crawler and then have it crawl a news website. In the interest of distributing
the load evenly and not overloading the news servers, we have pre-assigned the news sites to be crawled according to
your USC ID number, given in the table below.

The maximum pages to fetch can be set in crawler4j and it should be set to 20,000 to ensure a reasonable execution time
for this exercise. Also, maximum depth should be set to 16 to ensure that we limit the crawling.

### Deliverables

Your primary task is to enhance the crawler, so it collects information about:

1. the URLs it attempts to fetch, a two column spreadsheet, column 1 containing the URL and
   column 2 containing the HTTP/HTTPS status code received; name the file fetch_NewsSite.csv (where the name “NewsSite”
   is replaced by the news website name in the table above that you are crawling). The number of rows should be no more
   than 20,000 as that is our pre-set limit. Column names for this file can be URL and Status
2. the files it successfully downloads, a four column spreadsheet, column 1 containing the URLs successfully downloaded,
   column 2 containing the size of the downloaded file (in Bytes, or you can choose your own preferred unit (
   bytes,kb,mb)), column 3 containing the # of outlinks found, and column 4 containing the resulting content-type; name
   the file visit_NewsSite.csv; clearly the number of rows will be less than the number of rows in fetch_NewsSite.csv
3. all the URLs (including repeats) that were discovered and processed in some way; a two column spreadsheet where
   column 1 contains the encountered URL and column two an indicator of whether the URL a. resides in the website (OK),
   or b. points outside of the website (N_OK). (A file points out of the website if its URL does not start with the
   initial host/domain name, e.g. when crawling USA Today news website all inside URLs must start with
   .) Name the file urls_NewsSite.csv. This file will be much larger than fetch_*.csv and visit_*.csv.
   For example for New York Times-the URL and the URL are both considered as residing in the same website whereas the
   following URL is not considered to be in the same website, http://store.nytimes.com/
   Note1: you should modify the crawler, so it outputs the above data into three separate csv files; you will use them
   for processing later;
   Note2: all uses of NewsSite above should be replaced by the name given in the column labeled NewsSite Name in the
   table on page 1.
   Note 3: You should denote the units in size column of visit.csv. The best way would be to write the units that you
   are using in column header name and let the rest of the size data be in numbers for easier statistical analysis. The
   hard requirement is only to show the units clearly and correctly.

Based on the information recorded by the crawler in the output files above, you are to collate the following statistics
for a crawl of your designated news website:

- Fetch statistics:
    - \# fetches attempted:
      The total number of URLs that the crawler attempted to fetch. This is usually equal to the MAXPAGES setting if the
      crawler reached that limit; less if the website is smaller than that.
    - \# fetches succeeded:
      The number of URLs that were successfully downloaded in their entirety, i.e. returning a HTTP status code of 2XX.
    - \# fetches failed or aborted:
      The number of fetches that failed for whatever reason, including, but not limited to: HTTP
      2
      redirections (3XX), client errors (4XX), server errors (5XX) and other network-related errors.1

- Outgoing URLs: statistics about URLs extracted from visited HTML pages
    - Total URLs extracted: The grand total number of URLs extracted (including repeats) from all visited pages o
    - \# unique URLs extracted: The number of unique URLs encountered by the crawler
    - \# unique URLs within your news website: The number of unique URLs encountered that are associated with the news
      website,
      i.e. the URL begins with the given root URL of the news website, but the remainder of the URL is distinct
    - \# unique URLs outside the news website: The number of unique URLs encountered that were not from the news
      website.

- Status codes: number of times various HTTP status codes were encountered during crawling, including (but not limited
  to): 200, 301, 401, 402, 404, etc.
- File sizes: statistics about file sizes of visited URLs – the number of files in each size range (See Appendix A).
    - 1KB = 1024B; 1MB = 1024KB
- Content Type: a list of the different content-types encountered

These statistics should be collated and submitted as a plain text file whose name is CrawlReport_NewsSite.txt, following
the format given in Appendix A at the end of this document. Make sure you understand the crawler code and required
to be output before you commence collating these statistics.
For efficient crawling it is a good idea to have multiple crawling threads. You are required to use multiple threads in
this exercise.

crawler4j supports multi-threading and our examples show setting the number of crawlers to seven (see the line in the
code int numberOfCrawlers = 7;). However, if you do a naive implementation the threads will trample on each other when
outputting to your statistics collection files. Therefore, you need to be a bit smarter about how to collect the
statistics, and crawler4j documentation has a good example of how to do this.

### Rubrics

Total Points 10

Crawl Report

1. Number of threads used
2. \# fetches attempted = # fetches succeeded + # fetches failed or aborted
3. Number of rows of fetch_*.csv statistics should be close to 20,000 (close means within a
   1,000 or 2,000; if not explain why.
4. \# unique URLs extracted = # unique URLs within news site + # unique URLs outside the
   news site
5. The total number of URLs extracted should be equal to the number of outgoing links
   encountered on the fetched pages.
6. Status code - 200 codes should be equal to fetches succeeded
7. Number of files in the size statistics should be less than or equal to the number of
   fetches succeeded.
8. Number of files in the content types should be less than or equal to the number of
   fetches succeeded.
   CSV files
9. Inspect fetch.csv, visit.csv: all the data in both files will be cross validated against the crawl reports.
10. Note: column headers need to be included

### Appendix A

Use the following format to tabulate the statistics that you collated based on the crawler outputs.
Note: The status codes and content types shown are only a sample. The status codes and content types that you encounter
may vary, and should all be listed and reflected in your report. Do NOT lump everything else that is not in this sample
under an “Other” heading. You may, however, exclude status codes and types for which you have a count of zero. Also,
note the use of multiple threads. You are required to use multiple threads in this exercise.

File: `CrawlReport_NewsSite.txt`

```text
Name: Tommy Trojan
USC ID: 1234567890
News site crawled: nytimes.com Number of threads: 7

Fetch Statistics
================
# fetches attempted:
# fetches succeeded:
# fetches failed or aborted:

Outgoing URLs:
==============
Total URLs extracted:
# unique URLs extracted:
# unique URLs within News Site: # unique URLs outside News Site:

Status Codes:
=============
200 OK:
301 Moved Permanently: 401 Unauthorized:
403 Forbidden:
404 Not Found:

File Sizes:
===========
< 1KB:
1KB ~ <10KB: 
10KB ~ <100KB: 
100KB ~ <1MB:
>= 1MB:

Content Types:
============== 
text/html: 
image/gif: 
image/jpeg: 
image/png: 
application/pdf:
```

Homework Documents:

1. [data/hw2.pdf](data/hw2.pdf) - contains the detailed description of the homework
2. [data/rubrics.pdf](data/rubrics.pdf) - contains the grading rubrics for the homework
3. [data/crawler4j_installation.pdf](data/crawler4j_installation.pdf) - contains the installation and setup instruction
   for Crawler4J. Note that this project is in gradle and does not follow these instructions.
4. [data/crawler4j_process.pdf](data/crawler4j_process.pdf) - contains the flow diagram for the homework