package com.helenpaulini.flixster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.helenpaulini.flixster.adapters.MovieAdapter;
import com.helenpaulini.flixster.databinding.ActivityMainBinding;
import com.helenpaulini.flixster.databinding.ActivityMovieDetailsBinding;
import com.helenpaulini.flixster.models.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class MovieDetailsActivity extends AppCompatActivity {

    public static final String TAG = "MovieDetailsActivity";

    Movie movie;
    TextView tvTitle;
    TextView tvOverview;
    RatingBar rbVoteAverage;
    List<Movie> movies;
    ImageView thumbnail;
    String videoURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_movie_details);

        // simple_activity.xml -> SimpleActivityBinding
        ActivityMovieDetailsBinding binding = ActivityMovieDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // unwrap the movie passed in via intent, using its simple name as a key
        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        // resolve the view objects
        tvTitle = (TextView) binding.tvTitle;
        tvOverview = (TextView) binding.tvOverview;
        rbVoteAverage = (RatingBar) binding.rbVoteAverage;
        thumbnail = (ImageView) binding.thumbnail;

        // set the title and overview
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());

        // vote average is 0..10, convert to 0..5 by dividing by 2
        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage = voteAverage > 0 ? voteAverage / 2.0f : voteAverage);

        String imageURL = movie.getBackdropPath();
        Glide.with(this).load(imageURL).placeholder(R.drawable.flicks_backdrop_placeholder).into(thumbnail);

        // create the adapter
        final MovieAdapter movieAdapter = new MovieAdapter(this, movies);

        videoURL = "https://api.themoviedb.org/3/movie/"+movie.getId()+"/videos?api_key=a07e22bc18f5cb106bfe4cc1f83ad8ed&language=en-US";
        movies = new ArrayList<>();

        AsyncHttpClient client2 = new AsyncHttpClient();
        client2.get(videoURL, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess");
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray results = jsonObject.getJSONArray("results");
                    final String youtubeKey = results.getJSONObject(0).getString("key");

                    thumbnail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MovieDetailsActivity.this, MovieTrailerActivity.class);
                            intent.putExtra("youtubeKey", youtubeKey);
                            startActivity(intent);
                        }
                    });


                } catch (JSONException e) {
                    Log.e(TAG, "Hit json exception", e);
                }
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "onFailure");
            }
        });


    }
}