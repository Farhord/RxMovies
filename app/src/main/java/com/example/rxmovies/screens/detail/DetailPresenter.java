package com.example.rxmovies.screens.detail;

import android.os.AsyncTask;
import android.util.Log;


import com.example.rxmovies.api.ApiFactory;
import com.example.rxmovies.api.ApiService;
import com.example.rxmovies.data.FavouriteMovie;
import com.example.rxmovies.data.MovieDatabase;
import com.example.rxmovies.pojo.Movie;
import com.example.rxmovies.pojo.OneMovieResponse;
import com.example.rxmovies.pojo.ReviewResponse;
import com.example.rxmovies.pojo.TrailerResponse;

import java.util.concurrent.ExecutionException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DetailPresenter {

    private static final String API_KEY = "6bc1a55b28b09fe42158c4a99d3179ff";

    private static MovieDatabase database;
    private DetailView detailView;
    private CompositeDisposable compositeDisposable;

    public DetailPresenter(DetailView detailView) {
        this.detailView = detailView;
    }

    public void loadMovie(int movieId, String language) {
        ApiFactory apiFactory = ApiFactory.getInstance();
        ApiService apiService = apiFactory.getApiService();
        compositeDisposable = new CompositeDisposable();
        Disposable disposable = apiService.getMovieById(movieId, API_KEY, language)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<OneMovieResponse>() {
                    @Override
                    public void accept(OneMovieResponse oneMovieResponse) throws Exception {
                        Movie movie = new Movie();
                        movie.setId(oneMovieResponse.getId());
                        movie.setPosterPath(oneMovieResponse.getPosterPath());
                        movie.setTitle(oneMovieResponse.getTitle());
                        movie.setOriginalTitle(oneMovieResponse.getOriginalTitle());
                        movie.setVoteAverage(oneMovieResponse.getVoteAverage());
                        movie.setReleaseDate(oneMovieResponse.getReleaseDate());
                        movie.setOverview(oneMovieResponse.getOverview());
                        detailView.showData(movie);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        database = MovieDatabase.getInstance(detailView.getContext());
                        Movie movie = getMovieById(movieId);
                        detailView.showData(movie);
                    }
                });
        compositeDisposable.add(disposable);
    }

    public void loadTrailers(int movieId, String language) {
        ApiFactory apiFactory = ApiFactory.getInstance();
        ApiService apiService = apiFactory.getApiService();
        Disposable disposable = apiService.getTrailers(movieId, API_KEY, language)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<TrailerResponse>() {
                    @Override
                    public void accept(TrailerResponse trailerResponse) throws Exception {
                        detailView.showTrailers(trailerResponse.getTrailers());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                });
        compositeDisposable.add(disposable);
    }

    public void loadReviews(int movieId) {
        ApiFactory apiFactory = ApiFactory.getInstance();
        ApiService apiService = apiFactory.getApiService();
        Disposable disposable = apiService.getReviews(movieId, API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ReviewResponse>() {
                    @Override
                    public void accept(ReviewResponse reviewResponse) throws Exception {
                        detailView.showReviews(reviewResponse.getReviews());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.i("xxx", throwable.getMessage());
                    }
                });
        compositeDisposable.add(disposable);
    }

    public Movie getMovieById(int id) {
        try {
            return new GetMovieTask().execute(id).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class GetMovieTask extends AsyncTask<Integer, Void, Movie> {
        @Override
        protected Movie doInBackground(Integer... integers) {
            if (integers != null && integers.length > 0) {
                return database.movieDao().getMovieById(integers[0]);
            }
            return null;
        }
    }

    public FavouriteMovie getFavouriteMovieById(int id) {
        if (database == null) {
            database = MovieDatabase.getInstance(detailView.getContext());
        }
        try {
            return new GetFavouriteMovieTask().execute(id).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class GetFavouriteMovieTask extends AsyncTask<Integer, Void, FavouriteMovie> {
        @Override
        protected FavouriteMovie doInBackground(Integer... integers) {
            if (integers != null && integers.length > 0) {
                return database.movieDao().getFavouriteMovieById(integers[0]);
            }
            return null;
        }
    }

    public void insertFavouriteMovie(FavouriteMovie favouriteMovie) {
        new InsertFavouriteMovieTask().execute(favouriteMovie);
    }

    private static class InsertFavouriteMovieTask extends AsyncTask<FavouriteMovie, Void, Void> {
        @Override
        protected Void doInBackground(FavouriteMovie... favouriteMovies) {
            if (favouriteMovies != null && favouriteMovies.length > 0) {
                database.movieDao().insertFavouriteMovie(favouriteMovies[0]);
            }
            return null;
        }
    }

    public void deleteFavouriteMovie(FavouriteMovie favouriteMovie) {
        new DeleteFavouriteMovieTask().execute(favouriteMovie);
    }

    private static class DeleteFavouriteMovieTask extends AsyncTask<FavouriteMovie, Void, Void> {
        @Override
        protected Void doInBackground(FavouriteMovie... favouriteMovies) {
            if (favouriteMovies != null && favouriteMovies.length > 0) {
                database.movieDao().deleteFavouriteMovie(favouriteMovies[0]);
            }
            return null;
        }
    }


    public void disposeDisposable() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }
}
