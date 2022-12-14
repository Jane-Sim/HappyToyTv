package com.example.seyoung.happytoytv.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Amal Krishnan on 06-03-2017.
 */

public interface RetrofitInterface {

    @GET("maps/api/directions/json?")
    Call<DirectionsResponse> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("key") String key
    );

    @GET("/maps/api/geocode/json?")
    Call<GeocodeResponse> getGecodeData(
            @Query("address") String address,
            @Query("key") String key
    );

    @GET("/maps/api/geocode/json?")
    Call<GeocodeResponse> getRevGecodeData(
            @Query("latlng") String latlng,
            @Query("key") String key
    );


}

