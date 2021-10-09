package com.example.rxmovies.api;

import com.example.rxmovies.pojo.MovieResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("discover/movie")
    Observable<MovieResponse> getMovies(@Query("api_key") String apiKey, @Query("language") String language, @Query("sort_by") String sortBy, @Query("page") int page, @Query("vote_count.gte") String minVoteCount);


}
