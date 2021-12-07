package com.example.rss_reader.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "rss", strict = false)
public class RSSSite {
    @Element(name = "title", data = true)
    @Path("channel")
    private String title;

    @ElementList(entry = "item", inline = true)
    @Path("channel")
    private List<RSSArticle> list;


    public RSSSite() {
    }

    public String getTitle() {
        return title;
    }

    public List<RSSArticle> getList() {
        return list;
    }
}
