package com.example.rxmovies.screens.detail;

import android.content.Context;

import com.example.rxmovies.pojo.Movie;
import com.example.rxmovies.pojo.Review;
import com.example.rxmovies.pojo.Trailer;

import java.util.List;

public interface DetailView {

    Context getContext();

    void showData(Movie movie);

    void showTrailers(List<Trailer> trailers);

    void showReviews(List<Review> reviews);
}
