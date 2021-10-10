package com.example.rxmovies.data;

import androidx.room.Entity;
import androidx.room.Ignore;

import com.example.rxmovies.pojo.Movie;

@Entity(tableName = "favourite_movie")
public class FavouriteMovie extends Movie {
    public FavouriteMovie(int autoId, boolean adult, String backdropPath, int id, String originalLanguage, String originalTitle, String overview, double popularity, String posterPath, String releaseDate, String title, boolean video, double voteAverage, int voteCount) {
        super(autoId, adult, backdropPath, id, originalLanguage, originalTitle, overview, popularity, posterPath, releaseDate, title, video, voteAverage, voteCount);
    }

    @Ignore
    public FavouriteMovie (Movie movie) {
        super(movie.getAutoId(), movie.isAdult(), movie.getBackdropPath(), movie.getId(), movie.getOriginalLanguage(), movie.getOriginalTitle(), movie.getOverview(), movie.getPopularity(), movie.getPosterPath(), movie.getReleaseDate(), movie.getTitle(), movie.isVideo(), movie.getVoteAverage(), movie.getVoteCount());
    }

}
