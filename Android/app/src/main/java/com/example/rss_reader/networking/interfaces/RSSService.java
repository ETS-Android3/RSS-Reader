package com.example.rss_reader.networking.interfaces;

import com.example.rss_reader.models.RSSSite;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface RSSService {
    @GET()
    Call<RSSSite> getRSS(@Url String path);
}
