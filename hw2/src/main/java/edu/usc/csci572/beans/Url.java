package edu.usc.csci572.beans;


import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvIgnore;

public class Url {

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

    @Override
    public String toString() {
        return "Url{" +
                "docid=" + docid +
                ", url='" + url + '\'' +
                ", withinWebsite=" + withinWebsite +
                '}';
    }
}
