package com.example.rss_reader.networking.repositories;

import com.example.rss_reader.networking.clients.RetrofitClient;
import com.example.rss_reader.networking.interfaces.PostService;

import java.net.MalformedURLException;
import java.net.URL;

import retrofit2.Call;

public class PostRepository {
    private PostRepository() {
    }

    private static String base;
    private static String path;
    private PostService service;
    private static volatile PostRepository instance = null;

    public static PostRepository getInstance(String url) throws MalformedURLException {
        String base;
        String path;
        URL link = new URL(url);
        base = link.getProtocol() + "://" + link.getHost() + '/';
        path = link.getPath();

        if (instance == null)
            instance = new PostRepository();

        PostRepository.base = base;
        PostRepository.path = path;

        return instance;
    }

    public PostRepository initializeService() {
        this.service = RetrofitClient.getClient(RetrofitClient.Converter.SCALARS ,base).create(PostService.class);

        return instance;
    }

    public Call<String> getArticle() throws Exception {
        if (this.service != null)
            return this.service.getArticle(path);
        else
            throw new Exception("Service isn't initialized");
    }
}
