package com.example.rss_reader.models;
//
//import io.realm.RealmObject;
//import io.realm.annotations.PrimaryKey;
//
//import org.bson.types.ObjectId;
//
//public class RSSFeed extends RealmObject {
//    @PrimaryKey
//    private ObjectId _id;
//
//    private String url;
//
//    private String user;
//
//    public RSSFeed(String user, String url) {
//        this.user = user;
//        this.url = url;
//    }
//
//    public RSSFeed(){}
//
//    // Standard getters & setters
//    public ObjectId get_id() {
//        return _id;
//    }
//
//    public void set_id(ObjectId _id) {
//        this._id = _id;
//    }
//
//    public String getUrl() {
//        return url;
//    }
//
//    public void setUrl(String url) {
//        this.url = url;
//    }
//
//    public String getUser() {
//        return user;
//    }
//
//    public void setUser(String user) {
//        this.user = user;
//    }
//}


public class RSSFeed {
    private String url;

    private String user;

    public RSSFeed(String user, String url) {
        this.user = user;
        this.url = url;
    }

    public RSSFeed() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
