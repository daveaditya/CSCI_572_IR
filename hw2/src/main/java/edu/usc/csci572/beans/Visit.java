package edu.usc.csci572.beans;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvIgnore;

public class Visit {

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

    @CsvBindByName(column = "Content Type")
    @CsvBindByPosition(position = 3)
    private String contentType;

    public Visit() {
    }

    public void setDocid(int docid) {
        this.docid = docid;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setNumOfOutlinks(int numOfOutlinks) {
        this.numOfOutlinks = numOfOutlinks;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
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
