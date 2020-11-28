package com.example.news;

import com.example.news.Models.Headlines;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface { //Turns HTTP API into a Java interface using Retrofit

    @GET("top-headlines")
    Call<Headlines> getHeadlines(
            @Query("country") String country,
            @Query("apiKey") String apiKey
    );

    @GET("everything")
    Call<Headlines> getQuery(
            @Query("q") String query,
            @Query("apiKey") String apiKey
    );
}
