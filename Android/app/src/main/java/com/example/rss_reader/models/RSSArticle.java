package com.example.rss_reader.models;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "item", strict = false)
public class RSSArticle {
    @Element(name = "title", required = false, data = true)
    private String title;

    @Element(name = "description", required = false, data = true)
    private String description;

    @Element(name = "pubDate", required = false)
    private String pubDate;

    @Element(name = "link", required = false)
    private String link;

    @Element(name = "guid", required = false)
    private String guid;

    public RSSArticle(String title, String pubDate, String description, String link, String guid) {
        this.title = title;
        this.pubDate = pubDate;
        this.description = description;
        this.link = link;
        this.guid = guid;
    }

    public RSSArticle(){}

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getLink() {
        return link;
    }

    public String getGuid() {
        return guid;
    }
}