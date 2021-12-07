package com.example.rss_reader.networking.clients;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class RetrofitClient {
    public enum Converter {
        SIMPLE_XML, SCALARS
    }

    private static Retrofit retrofit = null;

    public static Retrofit getClient(Converter converter, String url) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        switch (converter) {
            case SIMPLE_XML:
                retrofit = new Retrofit.Builder()
                        .baseUrl(url)
                        .addConverterFactory(SimpleXmlConverterFactory.create())
                        .client(client)
                        .build();
                break;
            case SCALARS:
                retrofit = new Retrofit.Builder()
                        .baseUrl(url)
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .client(client)
                        .build();
        }

        return retrofit;
    }

}
