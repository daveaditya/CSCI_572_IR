package edu.usc.csci572.beans;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvIgnore;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Fetch implements Serializable {

    @Serial
    @CsvIgnore
    private static final long serialVersionUID = 1L;

    @CsvIgnore
    private int docid;

    @CsvBindByName(column = "URL")
    @CsvBindByPosition(position = 0)
    private String url;

    @CsvBindByName(column = "Status")
    @CsvBindByPosition(position = 1)
    private int statusCode;

    @CsvIgnore
    private static final Map<String, String> columnMappings = Map.of(
            "URL", "url",
            "Status", "status"
    );

    public Fetch() {
    }

    public Fetch(int docid, String url, int statusCode) {
        this.docid = docid;
        this.url = url;
        this.statusCode = statusCode;
    }

    public int getDocid() {
        return docid;
    }

    public void setDocid(int docid) {
        this.docid = docid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public static Stream<Fetch> loadFetchCsvStream(Path path) throws IOException {
        ColumnPositionMappingStrategy<Fetch> mappingStrategy =
                new ColumnPositionMappingStrategy<>();
        mappingStrategy.setType(Fetch.class);

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
            throw e;
        }
    }

    @Override
    public String toString() {
        return "Fetch{" +
                "docid=" + docid +
                ", url='" + url + '\'' +
                ", statusCode=" + statusCode +
                '}';
    }


    public static class Stats {
        private int totalFetchCount;

        private int succeededFetchCount;

        private int failedFetchCount;

        private Map<Integer, Integer> statusCodeCounts;

        public Stats() {
            totalFetchCount = 0;
            succeededFetchCount = 0;
            failedFetchCount = 0;
            statusCodeCounts = new LinkedHashMap<>();
        }

        public Stats(int totalFetchCount, int succeededFetchCount, int failedFetchCount) {
            this.totalFetchCount = totalFetchCount;
            this.succeededFetchCount = succeededFetchCount;
            this.failedFetchCount = failedFetchCount;
        }

        public int getTotalFetchCount() {
            return totalFetchCount;
        }

        public void incTotalFetchCount() {
            this.totalFetchCount++;
        }

        public int getSucceededFetchCount() {
            return succeededFetchCount;
        }

        public void incSucceededFetchCount() {
            this.succeededFetchCount++;
        }

        public int getFailedFetchCount() {
            return failedFetchCount;
        }

        public void incFailedFetchCount() {
            this.failedFetchCount++;
        }

        public Map<Integer, Integer> getStatusCodeCounts() {
            return statusCodeCounts;
        }

        public void updateStatusCodeCount(int statusCode) {
            if (statusCodeCounts.containsKey(statusCode)) {
                this.statusCodeCounts.put(statusCode, statusCodeCounts.get(statusCode) + 1);
            } else {
                this.statusCodeCounts.put(statusCode, 1);
            }
        }
    }
}
