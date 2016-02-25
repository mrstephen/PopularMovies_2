package com.droidbayapps.popularmovies_2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.droidbayapps.popularmovies_2.model.MovieDiscovery;

public class MovieGridActivity extends AppCompatActivity implements MovieSelectedCallback{
    boolean mTwoPane = false;

    private static final String DETAILS_FRAGMENT_TAG = "details";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(findViewById(R.id.movie_details_container) != null){
            mTwoPane = true;

            if(savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.movie_details_container, new MovieDetailsActivityFragment(), DETAILS_FRAGMENT_TAG)
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_list, menu);

        return true;
    }

    @Override
    public void onMovieSelected(MovieDiscovery.MovieData movieData) {
        Bundle b = new Bundle();
        b.putParcelable(MovieDiscovery.MovieData.PARCELABLE_KEY, movieData);

        if (!mTwoPane) {
            Intent intent = new Intent(this, MovieDetailsActivity.class);
            intent.putExtras(b);
            startActivity(intent);
        }
        else{
            MovieDetailsActivityFragment oldFragment = (MovieDetailsActivityFragment)getSupportFragmentManager().findFragmentByTag(DETAILS_FRAGMENT_TAG);
            MovieDetailsActivityFragment newFragment = new MovieDetailsActivityFragment();

            newFragment.setArguments(b);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            if(oldFragment != null)
                fragmentTransaction.remove(oldFragment);
            fragmentTransaction.add(R.id.movie_details_container, newFragment, DETAILS_FRAGMENT_TAG)
                    .commit();
        }
    }
}
