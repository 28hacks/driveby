package com.github.a28hacks.driveby.network;

import com.github.a28hacks.driveby.model.WikipediaResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WikipediaService {
    @GET("api.php?action=query&format=json&list=geosearch")
    Call<WikipediaResult> getItemForLocation(
            @Query("gsradius") int radius,
            @Query("gscoord") String formatedCoords
    );

}
