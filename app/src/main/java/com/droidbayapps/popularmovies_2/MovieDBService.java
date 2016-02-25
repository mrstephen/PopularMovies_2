package com.droidbayapps.popularmovies_2;

import com.droidbayapps.popularmovies_2.model.MovieDiscovery;
import com.droidbayapps.popularmovies_2.model.MovieReviewQuery;
import com.droidbayapps.popularmovies_2.model.MovieTrailerQuery;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by stephen on 2/2/2016.
 */
public class MovieDBService {
    public interface MovieDBAPI{
        @GET("3/discover/movie")
        Call<MovieDiscovery> getMovies(
                @Query("api_key") String apiKey,
                @Query("sort_by") String sortBy
        );

        @GET("3/movie/{id}/videos")
        Call<MovieTrailerQuery> getTrailers(
                @Path("id") String id,
                @Query("api_key") String apiKey
        );

        @GET("3/movie/{id}/reviews")
        Call<MovieReviewQuery> getReviews(
          @Path("id") String id,
          @Query("api_key") String apiKey
        );
    }
}
