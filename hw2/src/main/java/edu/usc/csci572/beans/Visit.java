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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Visit implements Serializable {

    @Serial
    @CsvIgnore
    private static final long serialVersionUID = 3L;

    @CsvIgnore
    private int docid;

    @CsvBindByName(column = "URL")
    @CsvBindByPosition(position = 0)
    private String url;

    @CsvBindByName(column = "Size (bytes)")
    @CsvBindByPosition(position = 1)
    private long size;

    @CsvBindByName(column = "# of Outlinks")
    @CsvBindByPosition(position = 2)
    private int numOfOutlinks;

    @CsvBindByName(column = "Content-Type")
    @CsvBindByPosition(position = 3)
    private String contentType;

    @CsvIgnore
    private static final Map<String, String> columnMappings = Map.of(
            "URL", "url",
            "Size (bytes)", "size",
            "# of Outlinks", "numOfOutlinks",
            "Content-Type", "contentType"
    );

    public Visit() {
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getNumOfOutlinks() {
        return numOfOutlinks;
    }

    public void setNumOfOutlinks(int numOfOutlinks) {
        this.numOfOutlinks = numOfOutlinks;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public static Stream<Visit> loadVisitCsvStream(Path path) throws IOException {
        ColumnPositionMappingStrategy<Visit> mappingStrategy =
                new ColumnPositionMappingStrategy<>();
        mappingStrategy.setType(Visit.class);

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
            throw e;
        }
    }

    @Override
    public String toString() {
        return "Visit{" +
                "docid=" + docid +
                ", url='" + url + '\'' +
                ", size=" + size +
                ", numOfOutlinks=" + numOfOutlinks +
                ", contentType='" + contentType + '\'' +
                '}';
    }

    public static class Stats {

        private long totalNumOfOutlinks;

        private final int[] fileSizeCountsByRange;

        private final Map<String, Long> contentTypeCounts;

        private static final int KB = 1024;

        private static final int MB = 1024 * KB;

        public Stats() {
            this.totalNumOfOutlinks = 0;
            this.fileSizeCountsByRange = new int[]{ 0, 0, 0, 0, 0 };
            this.contentTypeCounts = new LinkedHashMap<>();
        }

        public long getTotalNumOfOutlinks() {
            return totalNumOfOutlinks;
        }

        public void addOutlinks(long numOfOutlinks) {
            this.totalNumOfOutlinks += numOfOutlinks;
        }

        public int[] getFileSizeCountsByRange() {
            return fileSizeCountsByRange;
        }

        public void countFileSize(Visit visit) {
            long size = visit.getSize();
            if (size < KB) {
                this.fileSizeCountsByRange[0]++;
            } else if (size < 10 * KB) {
                this.fileSizeCountsByRange[1]++;
            } else if (size < 100 * KB) {
                this.fileSizeCountsByRange[2]++;
            } else if (size < MB) {
                this.fileSizeCountsByRange[3]++;
            } else {
                this.fileSizeCountsByRange[4]++;
            }
        }

        public Map<String, Long> getContentTypeCounts() {
            return contentTypeCounts;
        }

        public void addContentType(Visit visit) {
            String contentType = visit.contentType;
            if(this.contentTypeCounts.containsKey(contentType)) {
                this.contentTypeCounts.put(contentType, this.contentTypeCounts.get(contentType) + 1);
            } else {
                this.contentTypeCounts.put(contentType, 1L);
            }
        }
    }
}
