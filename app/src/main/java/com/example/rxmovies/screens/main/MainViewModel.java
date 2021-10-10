package com.example.rxmovies.screens.main;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.rxmovies.api.ApiFactory;
import com.example.rxmovies.api.ApiService;
import com.example.rxmovies.data.FavouriteMovie;
import com.example.rxmovies.data.MovieDatabase;
import com.example.rxmovies.pojo.Movie;
import com.example.rxmovies.pojo.MovieResponse;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel extends AndroidViewModel {

    private static final String API_KEY = "6bc1a55b28b09fe42158c4a99d3179ff";
    private static final String SORT_BY_POPULARITY = "popularity.desc";
    private static final String SORT_BY_VOTE_AVERAGE = "vote_average.desc";
    private static final int STANDARD_VOTE_COUNT_VALUE = 3200;
    public static final int POPULARITY = 0;
    public static final int VOTE_AVERAGE = 1;

    private static int minVoteCount;
    private String sortBy;
    private boolean isLoading = false;

    private static MovieDatabase database;
    private LiveData<List<Movie>> movies;
    private LiveData<List<FavouriteMovie>> favouriteMovies;
    private CompositeDisposable compositeDisposable;

    public MainViewModel(@NonNull Application application) {
        super(application);
        database = MovieDatabase.getInstance(application);
        movies = database.movieDao().getAllMovies();
        favouriteMovies = database.movieDao().getAllFavouriteMovies();
        minVoteCount = STANDARD_VOTE_COUNT_VALUE;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void loadData(String language, int methodOfSort, int page) {
        if (methodOfSort == 1) {
            sortBy = SORT_BY_VOTE_AVERAGE;
        } else sortBy = SORT_BY_POPULARITY;
        ApiFactory apiFactory = ApiFactory.getInstance();
        ApiService apiService = apiFactory.getApiService();
        compositeDisposable = new CompositeDisposable();
        Disposable disposable = apiService.getMovies(API_KEY, language, sortBy, page, minVoteCount)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        isLoading = true;
                    }
                })
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        isLoading = false;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MovieResponse>() {
                    @Override
                    public void accept(MovieResponse movieResponse) throws Exception {
                        if (page == 1) {
                            deleteAllMovies();
                        }
                        insertMovie(movieResponse.getMovies());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.i("xxx", throwable.getMessage());
                    }
                });
        compositeDisposable.add(disposable);
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }

    public void insertMovie(List<Movie> movies) {
        new InsertMovieTask().execute(movies);
    }

    private static class InsertMovieTask extends AsyncTask<List<Movie>, Void, Void> {
        @Override
        protected Void doInBackground(List<Movie>... lists) {
            if (lists != null && lists.length > 0) {
                database.movieDao().insertMovie(lists[0]);
            }
            return null;
        }
    }

    public void deleteAllMovies() {
        new DeleteAllMoviesTask().execute();
    }

    private static class DeleteAllMoviesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            database.movieDao().deleteAllMovies();
            return null;
        }
    }

    public LiveData<List<FavouriteMovie>> getFavouriteMovies() {
        return favouriteMovies;
    }

    @Override
    protected void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }
}
