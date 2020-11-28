package com.example.news;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient { //Controller class for the news API
    private static final String BASE_URL = "https://newsapi.org/v2/"; //Base URL of news API used
    private static ApiClient apiClient;
    private static Retrofit retrofit;

    private ApiClient(){ //Creates the Retrofit client
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
    }

    public static synchronized ApiClient getInstance(){
        if (apiClient==null){
            apiClient = new ApiClient();
        }
        return apiClient;
    }

    public ApiInterface getApi(){
        return retrofit.create(ApiInterface.class);
    }
}
