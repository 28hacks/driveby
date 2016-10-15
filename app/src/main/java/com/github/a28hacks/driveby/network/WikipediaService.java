package com.github.a28hacks.driveby.network;

import com.github.a28hacks.driveby.model.wiki_api.WikipediaResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WikipediaService {
    @GET("api.php?action=query&format=json&list=geosearch&gsprop=type")
    Call<WikipediaResult> getItemForLocation(
            @Query("gsradius") int radius,
            @Query("gscoord") String formatedCoords
    );


    @GET("api.php?action=query&format=json&prop=extracts&exintro=&explaintext=&exlimit=max")
    Call<WikipediaResult> getExtractText(
            @Query("pageids") String pageIds
    );




}
