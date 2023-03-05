package edu.usc.csci572.beans;


import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvIgnore;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class Url implements Serializable {

    @Serial
    @CsvIgnore
    private static final long serialVersionUID = 2L;

    @CsvIgnore
    int docid;

    @CsvBindByName(column = "URL")
    @CsvBindByPosition(position = 0)
    private String url;

    @CsvBindByName(column = "URL Type")
    @CsvBindByPosition(position = 1)
    private String withinWebsite;

    @CsvIgnore
    private static final Map<String, String> columnMappings = Map.of(
            "URL", "url",
            "URL Type", "withinWebsite"
    );

    public Url() {
    }

    public Url(int docid, String url, String withinWebsite) {
        this.docid = docid;
        this.url = url;
        this.withinWebsite = withinWebsite;
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

    public String getWithinWebsite() {
        return withinWebsite;
    }

    public void setWithinWebsite(String withinWebsite) {
        this.withinWebsite = withinWebsite;
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
            throw e;
        }
    }

    @Override
    public String toString() {
        return "Url{" +
                "docid=" + docid +
                ", url='" + url + '\'' +
                ", withinWebsite=" + withinWebsite +
                '}';
    }

    public static class Stats {

        private int totalCount;

        private final Set<String> uniqueUrls;

        private final Set<String> uniqueWithinUrls;

        private final Set<String> uniqueOutsideUrls;

        public Stats() {
            this.totalCount = 0;
            this.uniqueUrls = new LinkedHashSet<>();
            this.uniqueWithinUrls = new LinkedHashSet<>();
            this.uniqueOutsideUrls = new LinkedHashSet<>();
        }

        public void addUrls(Url url) {
            this.totalCount += 1;

            String link = url.getUrl();
            this.uniqueUrls.add(link);
            if(url.getWithinWebsite().equals("OK")) {
                this.uniqueWithinUrls.add(link);
            } else {
                this.uniqueOutsideUrls.add(link);
            }
        }

        public void addUrls(String url, String withinWebsite) {
            this.totalCount += 1;

            String link = url;
            this.uniqueUrls.add(url);
            if(withinWebsite.equals("OK")) {
                this.uniqueWithinUrls.add(link);
            } else {
                this.uniqueOutsideUrls.add(link);
            }
        }

        public int getTotalCount() {
            return this.totalCount;
        }

        public int getUniqueUrlCount() {
            return uniqueUrls.size();
        }

        public int getUniqueWithinUrlCount() {
            return uniqueWithinUrls.size();
        }

        public int getUniqueOutsideUrlCount() {
            return uniqueOutsideUrls.size();
        }
    }
}
