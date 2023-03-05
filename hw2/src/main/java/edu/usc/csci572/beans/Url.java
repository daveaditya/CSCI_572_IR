package edu.usc.csci572.beans;


import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvIgnore;

import java.io.Serial;
import java.io.Serializable;

public class Url implements Serializable {

    @Serial
    @CsvIgnore
    private static final long serialVersionUID = 2L;

    @CsvIgnore
    int docid;

    @CsvBindByName(column = "URL")
    @CsvBindByPosition(position = 0)
    private final String url;

    @CsvBindByName(column = "URL Type")
    @CsvBindByPosition(position = 1)
    private final String withinWebsite;

    public Url(int docid, String url, String withinWebsite) {
        this.docid = docid;
        this.url = url;
        this.withinWebsite = withinWebsite;
    }

    public int getDocid() {
        return docid;
    }

    public String getUrl() {
        return url;
    }

    public String getWithinWebsite() {
        return withinWebsite;
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
