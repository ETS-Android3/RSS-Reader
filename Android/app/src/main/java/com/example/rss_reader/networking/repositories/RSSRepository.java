package com.example.rss_reader.networking.repositories;

import com.example.rss_reader.models.RSSSite;
import com.example.rss_reader.networking.clients.RetrofitClient;
import com.example.rss_reader.networking.interfaces.RSSService;

import java.net.MalformedURLException;
import java.net.URL;

import retrofit2.Call;


public class RSSRepository {
    private RSSRepository() {
    }

    private static String base;
    private static String path;
    private RSSService service;
    private static volatile RSSRepository instance = null;

    public static RSSRepository getInstance(String url) throws MalformedURLException {
        String base;
        String path;
        URL link = new URL(url);
        base = link.getProtocol() + "://" + link.getHost() + '/';
        path = link.getPath();

        if (instance == null)
            instance = new RSSRepository();

        RSSRepository.base = base;
        RSSRepository.path = path;

        return instance;
    }

    public RSSRepository initializeService() {
        this.service = RetrofitClient.getClient(RetrofitClient.Converter.SIMPLE_XML, base).create(RSSService.class);

        return instance;
    }

    public Call<RSSSite> getRSS() throws Exception {
        if (this.service != null)
            return this.service.getRSS(path);
        else
            throw new Exception("Service isn't initialized");
    }
}
