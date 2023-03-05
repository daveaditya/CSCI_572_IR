import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import edu.usc.csci572.CrawlStats;
import edu.usc.csci572.Utils;
import edu.usc.csci572.beans.Fetch;
import edu.usc.csci572.beans.Url;
import edu.usc.csci572.beans.Visit;
import org.apache.hc.core5.http.impl.EnglishReasonPhraseCatalog;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class MainTest {

    @Test
    public void csvReaderTest() {

        ColumnPositionMappingStrategy<Fetch> mappingStrategy =
                new ColumnPositionMappingStrategy<>();
        mappingStrategy.setType(Fetch.class);

        Path path = Paths.get("submission/fetch_usatoday.csv");
        try(BufferedReader reader = Files.newBufferedReader(path)) {
//            CsvToBean<Fetch> csvToBean = new CsvToBeanBuilder<Fetch>(reader)
//                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
//                    .withIgnoreEmptyLine(true)
//                    .withMappingStrategy(mappingStrategy)
//                    .withSkipLines(1)
//                    .build();

//            String line = reader.readLine();
//            long count = 0;
//            while(line != null) {
//               String[] row = line.split(",");
//                if (Objects.equals(row[1], "\"200\"")) {
//                    count++;
//                }
//                line = reader.readLine();
//            }
//            System.out.println("Count is ... " + count);

            System.out.println(reader.lines().map(l -> l.split(",")).filter(r -> r[1].equals("\"200\"")).count());;


//            for (Fetch fetch : csvToBean) {
//                System.out.println(fetch.toString());
//            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void urlStreamTest() {

        ColumnPositionMappingStrategy<Url> mappingStrategy =
                new ColumnPositionMappingStrategy<>();
        mappingStrategy.setType(Url.class);

        Path path = Paths.get("submission/urls_usatoday.csv");
        try(BufferedReader reader = Files.newBufferedReader(path)) {
            CsvToBean<Url> csvToBean = new CsvToBeanBuilder<Url>(reader)
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .withIgnoreEmptyLine(true)
                    .withMappingStrategy(mappingStrategy)
                    .withSkipLines(1)
                    .build();

            System.out.println("Count is ... " + csvToBean.stream().filter(url -> url.getWithinWebsite().equals("OK")).count());;

//            for (Fetch fetch : csvToBean) {
//                System.out.println(fetch.toString());
//            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void reportGenerationTest() {
        String domain = "usatoday.com";
        String outputDirectory = "submission";

        String author = "John  Doe";
        String id = "xxxxxxxxxx";
        int nThreads = 8;

        String identifier = domain.split("\\.")[0];

        // Write Report
        Path reportFilePath = Paths.get(String.format("%s/CrawlReport_%s.txt", outputDirectory, identifier));
        try (
                BufferedWriter writer = Files.newBufferedWriter(reportFilePath)
        ) {
            StringBuilder report = new StringBuilder();

            report.append(String.format("""
                    Name: %s
                    USC ID: %s
                    News site crawled: %s
                    Number of threads: %d
                    """, author, id, domain, nThreads));

            // Calculate Fetch Statistics
            Iterator<Fetch> fetchStream = loadFetchStream("submission/fetch_usatoday.csv").iterator();

            Fetch.Stats fetchStats = new Fetch.Stats();
            for (Iterator<Fetch> it = fetchStream; it.hasNext(); ) {
                Fetch fetch = it.next();

                fetchStats.incTotalFetchCount();
                if(fetch.getStatusCode() >= 200 && fetch.getStatusCode() < 300) {
                    fetchStats.incSucceededFetchCount();
                } else {
                    fetchStats.incFailedFetchCount();
                }

                fetchStats.updateStatusCodeCount(fetch.getStatusCode());
            }

            report.append(String.format("""
                    \nFetch Statistics
                    ================
                    # fetches attempted: %d
                    # fetches succeeded: %d
                    # fetches failed or aborted: %d
                    """, fetchStats.getTotalFetchCount(), fetchStats.getSucceededFetchCount(), fetchStats.getFailedFetchCount()));

            // Calculate URLs count
            Url.Stats urlStats = new Url.Stats();
            Iterator<String> urlIterator = Files.newBufferedReader(Paths.get("submission/urls_usatoday.csv")).lines().iterator();

            for (Iterator<String> it = urlIterator; it.hasNext(); ) {
                String[] row = it.next().split(",");
                urlStats.addUrls(row[0].replace("\"", ""), row[1].replace("\"", ""));
            }

            report.append(String.format("""
                    \nOutgoing URLs:
                    ==============
                    Total URLs extracted: %d
                    # unique URLs extracted: %d
                    # unique URLs within News Site: %d
                    # unique URLs outside News Site: %d
                    """, urlStats.getTotalCount(), urlStats.getUniqueUrlCount(), urlStats.getUniqueWithinUrlCount(), urlStats.getUniqueOutsideUrlCount()));

            report.append("""
                    \nStatus Codes:
                    =============
                    """);

            // Loop and append all Status Codes + Counts as String
            for (Map.Entry<Integer, Integer> pair : fetchStats.getStatusCodeCounts().entrySet()) {
                String statusCodeWithMessage = EnglishReasonPhraseCatalog.INSTANCE.getReason(pair.getKey(), Locale.ENGLISH);
                report.append(String.format("%d %s: %d\n", pair.getKey(), statusCodeWithMessage, pair.getValue()));
            }


            Iterator<Visit> visitIterator = loadVisitStream("submission/visit_usatoday.csv").iterator();
            Visit.Stats visitStats = new Visit.Stats();

            for (Iterator<Visit> it = visitIterator; it.hasNext();) {
                Visit visit = it.next();
                visitStats.addContentType(visit);
                visitStats.countFileSize(visit);
            }

            int[] fileSizeCountsByRange = visitStats.getFileSizeCountsByRange();

            report.append(String.format("""
                    \nFile Sizes:
                    ===========
                    < 1KB: %d
                    1KB ~ <10KB: %d
                    10KB ~ <100KB: %d
                    100KB ~ <1MB: %d
                    >= 1MB: %d
                    """, fileSizeCountsByRange[0], fileSizeCountsByRange[1], fileSizeCountsByRange[2], fileSizeCountsByRange[3], fileSizeCountsByRange[4]));

            report.append("""
                    \nContent Types:
                    ==============
                    """);

            // Loop and get all Content Types + Count as String
            for (Map.Entry<String, Long> pair : visitStats.getContentTypeCounts().entrySet()) {
                report.append(String.format("%s: %d\n", pair.getKey(), pair.getValue()));
            }

            writer.write(report.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Stream<Fetch> loadFetchStream(String filePath) {
        ColumnPositionMappingStrategy<Fetch> mappingStrategy =
                new ColumnPositionMappingStrategy<>();
        mappingStrategy.setType(Fetch.class);

        Path path = Paths.get(filePath);
        try {
            BufferedReader reader = Files.newBufferedReader(path);
            return new CsvToBeanBuilder<Fetch>(reader)
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .withIgnoreEmptyLine(true)
                    .withMappingStrategy(mappingStrategy)
                    .withSkipLines(1)
                    .build()
                    .stream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Stream<Url> loadUrlStream(String filePath) throws IOException {
        ColumnPositionMappingStrategy<Url> mappingStrategy =
                new ColumnPositionMappingStrategy<>();
        mappingStrategy.setType(Url.class);

        Path path = Paths.get(filePath);
        try {
            BufferedReader reader = Files.newBufferedReader(path);
            return new CsvToBeanBuilder<Url>(reader)
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .withIgnoreEmptyLine(true)
                    .withMappingStrategy(mappingStrategy)
                    .withSkipLines(1)
                    .build()
                    .stream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Stream<Visit> loadVisitStream(String filePath) throws IOException {
        ColumnPositionMappingStrategy<Visit> mappingStrategy =
                new ColumnPositionMappingStrategy<>();
        mappingStrategy.setType(Visit.class);

        Path path = Paths.get(filePath);
        try {
            BufferedReader reader = Files.newBufferedReader(path);
            return new CsvToBeanBuilder<Visit>(reader)
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .withIgnoreEmptyLine(true)
                    .withMappingStrategy(mappingStrategy)
                    .withSkipLines(1)
                    .build()
                    .stream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
