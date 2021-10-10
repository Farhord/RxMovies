package com.example.rxmovies.api;

import com.example.rxmovies.pojo.MovieResponse;
import com.example.rxmovies.pojo.OneMovieResponse;
import com.example.rxmovies.pojo.ReviewResponse;
import com.example.rxmovies.pojo.TrailerResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("discover/movie")
    Observable<MovieResponse> getMovies(@Query("api_key") String apiKey, @Query("language") String language, @Query("sort_by") String sortBy, @Query("page") int page, @Query("vote_count.gte") int minVoteCount);

    @GET("movie/{id}")
    Observable<OneMovieResponse> getMovieById(@Path("id") int id, @Query("api_key") String apiKey, @Query("language") String language);

    @GET("movie/{id}/videos")
    Observable<TrailerResponse> getTrailers(@Path("id") int id, @Query("api_key") String apiKey, @Query("language") String language);

    @GET("movie/{id}/reviews")
    Observable<ReviewResponse> getReviews(@Path("id") int id, @Query("api_key") String apiKey);
}
