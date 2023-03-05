package edu.usc.csci572.beans;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
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

    public static List<Visit> loadFromCsv(String filePath) throws IOException {
        List<Visit> visits;

        HeaderColumnNameTranslateMappingStrategy<Visit> mappingStrategy =
                new HeaderColumnNameTranslateMappingStrategy<>();
        mappingStrategy.setColumnMapping(columnMappings);
        mappingStrategy.setType(Visit.class);

        Path path = Paths.get(filePath);
        try(BufferedReader reader = Files.newBufferedReader(path)) {

            CsvToBean<Visit> csvToBean = new CsvToBeanBuilder<Visit>(reader)
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .withIgnoreEmptyLine(true)
                    .withMappingStrategy(mappingStrategy)
                    .build();

            visits = csvToBean.parse();

        } catch (IOException e) {
            e.printStackTrace();
            throw(e);
        }
        return visits;
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
}
