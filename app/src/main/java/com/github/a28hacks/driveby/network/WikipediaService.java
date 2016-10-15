package com.github.a28hacks.driveby.network;

import com.github.a28hacks.driveby.model.WikipediaResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WikipediaService {
    @GET("api.php?action=query&format=json&list=geosearch&prop=extracts&exintro=&explaintext=")
    Call<WikipediaResult> getItemForLocation(
            @Query("gsradius") int radius,
            @Query("gscoord") String formatedCoords
    );


    @GET("api.php?action=query&format=json&prop=extracts&exintro=&explaintext=&exlimit=max")
    Call<WikipediaResult> getExtractText(
            @Query("pageids") String pageIds
    );



}
