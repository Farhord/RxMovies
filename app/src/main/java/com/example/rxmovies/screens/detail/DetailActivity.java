package com.example.rxmovies.screens.detail;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rxmovies.R;
import com.example.rxmovies.adapters.ReviewAdapter;
import com.example.rxmovies.adapters.TrailerAdapter;
import com.example.rxmovies.data.FavouriteMovie;
import com.example.rxmovies.pojo.Movie;
import com.example.rxmovies.pojo.Review;
import com.example.rxmovies.pojo.Trailer;
import com.example.rxmovies.screens.favourite.FavouriteActivity;
import com.example.rxmovies.screens.main.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity implements DetailView {


    private static final String BASE_VIDEO_URL = "https://www.youtube.com/watch?v=";
    private static final String BASE_POSTER_URL = "https://image.tmdb.org/t/p/";
    private static final String BIG_POSTER_SIZE = "w780";

    private ScrollView scrollViewLandscape;
    private ImageView imageViewPoster;
    private TextView textViewTitle;
    private TextView textViewOriginalTitle;
    private TextView textViewRating;
    private TextView textViewReleaseDate;
    private TextView textViewDescription;
    private ImageView imageViewFavouriteStar;
    private RecyclerView recyclerViewTrailers;
    private RecyclerView recyclerViewReviews;

    private DetailPresenter presenter;
    private TrailerAdapter trailerAdapter;
    private ReviewAdapter reviewAdapter;
    private FavouriteMovie favouriteMovie;

    private int movieId;
    private String lang;

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
        setContentView(R.layout.activity_detail);
        scrollViewLandscape = findViewById(R.id.scrollViewLandscape);
        imageViewPoster = findViewById(R.id.imageViewPoster);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewOriginalTitle = findViewById(R.id.textViewOriginalTitle);
        textViewRating = findViewById(R.id.textViewRating);
        textViewReleaseDate = findViewById(R.id.textViewReleaseDate);
        textViewDescription = findViewById(R.id.textViewDescription);
        imageViewFavouriteStar = findViewById(R.id.imageViewFavouriteStar);
        presenter = new DetailPresenter(this);
        lang = Locale.getDefault().getLanguage();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            movieId = intent.getIntExtra("id", -1);
        } else finish();
        presenter.loadMovie(movieId, lang);
        checkFavouriteMovie();
        setTrailers();
        setReviews();
        setOnTrailerClickListener();
        scrollViewLandscape.smoothScrollTo(0, 0);
    }

    private void checkFavouriteMovie() {
        favouriteMovie = presenter.getFavouriteMovieById(movieId);
        if (favouriteMovie == null) {
            imageViewFavouriteStar.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_off));
        } else
            imageViewFavouriteStar.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_on));
    }

    public void setTrailers() {
        recyclerViewTrailers = findViewById(R.id.recyclerViewTrailers);
        recyclerViewTrailers.setLayoutManager(new LinearLayoutManager(this));
        trailerAdapter = new TrailerAdapter();
        trailerAdapter.setTrailers(new ArrayList<>());
        recyclerViewTrailers.setAdapter(trailerAdapter);
        presenter.loadTrailers(movieId, lang);
    }

    public void setReviews() {
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter();
        reviewAdapter.setReviews(new ArrayList<>());
        recyclerViewReviews.setAdapter(reviewAdapter);
        presenter.loadReviews(movieId);
    }

    private void setOnTrailerClickListener() {
        trailerAdapter.setOnTrailerClickListener(new TrailerAdapter.OnTrailerClickListener() {
            @Override
            public void onTrailerClick(String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_VIDEO_URL + url));
                startActivity(intent);
            }
        });
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showData(Movie movie) {
        Picasso.get().load(BASE_POSTER_URL + BIG_POSTER_SIZE + movie.getPosterPath()).placeholder(R.drawable.internetlost).into(imageViewPoster);
        textViewTitle.setText(movie.getTitle());
        textViewOriginalTitle.setText(movie.getOriginalTitle());
        textViewRating.setText(Double.toString(movie.getVoteAverage()));
        textViewReleaseDate.setText(movie.getReleaseDate());
        textViewDescription.setText(movie.getOverview());
    }

    @Override
    public void showTrailers(List<Trailer> trailers) {
        trailerAdapter.setTrailers(trailers);
    }

    @Override
    public void showReviews(List<Review> reviews) {
        reviewAdapter.setReviews(reviews);
    }

    public void onClickChangeToFavourite(View view) {
        favouriteMovie = presenter.getFavouriteMovieById(movieId);
        if (favouriteMovie == null) {
            presenter.insertFavouriteMovie(new FavouriteMovie(presenter.getMovieById(movieId)));
            Toast.makeText(this, R.string.add_to_favourite, Toast.LENGTH_SHORT).show();
        } else {
            presenter.deleteFavouriteMovie(favouriteMovie);
            Toast.makeText(this, R.string.remove_from_favourite, Toast.LENGTH_SHORT).show();
        }
        checkFavouriteMovie();
    }

    @Override
    protected void onDestroy() {
        presenter.disposeDisposable();
        super.onDestroy();
    }

}