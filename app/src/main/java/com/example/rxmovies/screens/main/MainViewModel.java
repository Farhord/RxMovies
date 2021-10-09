package com.example.rxmovies.screens.main;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.rxmovies.api.ApiFactory;
import com.example.rxmovies.api.ApiService;
import com.example.rxmovies.data.MovieDatabase;
import com.example.rxmovies.pojo.Movie;
import com.example.rxmovies.pojo.MovieResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel extends AndroidViewModel {

    private static final String API_KEY = "6bc1a55b28b09fe42158c4a99d3179ff";
    private static final String SORT_BY_POPULARITY = "popularity.desc";
    private static final String SORT_BY_VOTE_AVERAGE = "vote_average.desc";
    private static final String MIN_VOTE_COUNT_VALUE = "3200";

    private String sortBy;
    private boolean isLoading = false;

    public boolean isLoading() {
        return isLoading;
    }

    public static final int POPULARITY = 0;
    public static final int VOTE_AVERAGE = 1;

    private static MovieDatabase database;
    private LiveData<List<Movie>> movies;
    private MutableLiveData<Throwable> errors;

    private CompositeDisposable compositeDisposable;


    public MainViewModel(@NonNull Application application) {
        super(application);
        database = MovieDatabase.getInstance(application);
        movies = database.movieDao().getAllMovies();
        errors = new MutableLiveData<>();
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }

    public MutableLiveData<Throwable> getErrors() {
        return errors;
    }

    public void clearErrors() {
        errors.setValue(null);
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
            database.movieDao().getAllMovies();
            return null;
        }
    }

    public void deleteMovie(Movie movie) {
        new DeleteMovieTask().execute(movie);
    }

    private static class DeleteMovieTask extends AsyncTask<Movie, Void, Void> {
        @Override
        protected Void doInBackground(Movie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().deleteMovie(movies[0]);
            }
                return null;
        }
    }

    public void loadData(String language, int methodOfSort, int page) {
        if (methodOfSort == 1) {
            sortBy = SORT_BY_VOTE_AVERAGE;
        } else sortBy = SORT_BY_POPULARITY;
        ApiFactory apiFactory = ApiFactory.getInstance();
        ApiService apiService = apiFactory.getApiService();
        compositeDisposable = new CompositeDisposable();
        Disposable disposable = apiService.getMovies(API_KEY, language, sortBy, page, MIN_VOTE_COUNT_VALUE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MovieResponse>() {
                    @Override
                    public void accept(MovieResponse movieResponse) throws Exception {
                        deleteAllMovies();
                        insertMovie(movieResponse.getMovies());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        errors.setValue(throwable);
                        Log.i("xxx", throwable.getMessage());
                    }
                });
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }
}
