package com.example.rss_reader.models;

public class RSSPost {
    private final RSSArticle rssArticle;
    private final String html;

    public RSSPost(RSSArticle article, String html) {
        this.rssArticle = article;
        this.html = html;
    }

    public RSSArticle getRSSArticle() {
        return rssArticle;
    }

    public String getHtml() {
        return html;
    }
}
