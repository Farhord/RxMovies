package com.example.rxmovies.screens.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.rxmovies.R;
import com.example.rxmovies.adapters.MovieAdapter;
import com.example.rxmovies.pojo.Movie;
import com.example.rxmovies.screens.detail.DetailActivity;
import com.example.rxmovies.screens.favourite.FavouriteActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Switch switchSort;
    private TextView textViewPopularity;
    private TextView textViewTopRated;
    private RecyclerView recyclerViewPosters;
    private ProgressBar progressBarLoading;
    private MovieAdapter movieAdapter;
    private MainViewModel viewModel;

    private int methodOfSort;
    private static int page = 1;
    private static String lang;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.itemMain:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.itemFavourite:
                Intent intentToFavourite = new Intent(this, FavouriteActivity.class);
                startActivity(intentToFavourite);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        switchSort = findViewById(R.id.switchSort);
        textViewPopularity = findViewById(R.id.textViewPopularity);
        textViewTopRated = findViewById(R.id.textViewTopRating);
        recyclerViewPosters = findViewById(R.id.recyclerViewPosters);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        lang = Locale.getDefault().getLanguage();
        movieAdapter = new MovieAdapter();
        movieAdapter.setMovies(new ArrayList<>());
        recyclerViewPosters.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        recyclerViewPosters.setAdapter(movieAdapter);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        switchSort.setChecked(true);
        viewModel.getMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                movieAdapter.setMovies(movies);
            }
        });
        setAllListeners();
        switchSort.setChecked(false);
        recyclerViewPosters.smoothScrollToPosition(0);
    }

    private int getColumnCount() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels / displayMetrics.density);
        return width / 185 > 2 ? width / 185 : 2;
    }

    public void setAllListeners() {
        switchSort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                page = 1;
                setFilmsBySort(b);
            }
        });

        movieAdapter.setOnPosterClickListener(new MovieAdapter.OnPosterClickListener() {
            @Override
            public void onPosterClick(int position) {
                Movie movie = movieAdapter.getMovies().get(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("id", movie.getId());
                startActivity(intent);
            }
        });

        movieAdapter.setOnReachEndListener(new MovieAdapter.OnReachEndListener() {
            @Override
            public void onReachEnd() {
                if (!viewModel.isLoading()) {
                    progressBarLoading.setVisibility(View.VISIBLE);
                    page++;
                    viewModel.loadData(lang, methodOfSort, page);
                    if (!viewModel.isLoading()) {
                        progressBarLoading.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    public void setFilmsBySort(boolean b) {
        if (b) {
            methodOfSort = viewModel.VOTE_AVERAGE;
            textViewTopRated.setTextColor(getResources().getColor(R.color.purple_200));
            textViewPopularity.setTextColor(getResources().getColor(R.color.white));
        } else {
            methodOfSort = viewModel.POPULARITY;
            textViewTopRated.setTextColor(getResources().getColor(R.color.white));
            textViewPopularity.setTextColor(getResources().getColor(R.color.purple_200));
        }
        viewModel.loadData(lang, methodOfSort, page);
    }

    public void onClickSetPopularity(View view) {
        if (methodOfSort == 1) {
            switchSort.setChecked(false);
        }
    }

    public void onClickSetTopRated(View view) {
        if (methodOfSort == 0) {
            switchSort.setChecked(true);
        }
    }

}