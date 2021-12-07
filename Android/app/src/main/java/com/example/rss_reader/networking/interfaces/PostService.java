package com.example.rss_reader.networking.interfaces;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface PostService {
    @GET()
    Call<String> getArticle(@Url String path);
}
