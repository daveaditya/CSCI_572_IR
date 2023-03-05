package edu.usc.csci572.beans;


import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvIgnore;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

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

    public static List<Url> loadFromCsv(String filePath) throws IOException {
        List<Url> urls;

        HeaderColumnNameTranslateMappingStrategy<Url> mappingStrategy =
                new HeaderColumnNameTranslateMappingStrategy<>();
        mappingStrategy.setColumnMapping(columnMappings);
        mappingStrategy.setType(Url.class);

        Path path = Paths.get(filePath);
        try(BufferedReader reader = Files.newBufferedReader(path)) {

            CsvToBean<Url> csvToBean = new CsvToBeanBuilder<Url>(reader)
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .withIgnoreEmptyLine(true)
                    .withMappingStrategy(mappingStrategy)
                    .build();

            urls = csvToBean.parse();

        } catch (IOException e) {
            e.printStackTrace();
            throw(e);
        }
        return urls;
    }

    @Override
    public String toString() {
        return "Url{" +
                "docid=" + docid +
                ", url='" + url + '\'' +
                ", withinWebsite=" + withinWebsite +
                '}';
    }
}
