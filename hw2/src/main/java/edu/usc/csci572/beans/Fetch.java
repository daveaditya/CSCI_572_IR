package edu.usc.csci572.beans;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvIgnore;

public class Fetch {

    @CsvIgnore
    private final int docid;

    @CsvBindByName(column = "URL")
    @CsvBindByPosition(position = 0)
    private final String url;

    @CsvBindByName(column = "Status")
    @CsvBindByPosition(position = 1)
    private final int statusCode;

    public Fetch(int docid, String url, int statusCode) {
        this.docid = docid;
        this.url = url;
        this.statusCode = statusCode;
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
