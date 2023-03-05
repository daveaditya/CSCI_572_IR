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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public List<Fetch> loadFromCsv(String filePath) throws IOException {
        List<Fetch> fetches;

        HeaderColumnNameTranslateMappingStrategy<Fetch> mappingStrategy =
                new HeaderColumnNameTranslateMappingStrategy<>();
        mappingStrategy.setColumnMapping(columnMappings);
        mappingStrategy.setType(Fetch.class);

        Path path = Paths.get(filePath);
        try(BufferedReader reader = Files.newBufferedReader(path)) {

            CsvToBean<Fetch> csvToBean = new CsvToBeanBuilder<Fetch>(reader)
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .withIgnoreEmptyLine(true)
                    .withMappingStrategy(mappingStrategy)
                    .build();

            fetches = csvToBean.parse();

        } catch (IOException e) {
            e.printStackTrace();
            throw(e);
        }
        return fetches;
    }

    @Override
    public String toString() {
        return "Fetch{" +
                "docid=" + docid +
                ", url='" + url + '\'' +
                ", statusCode=" + statusCode +
                '}';
    }
}
