package com.droidbayapps.popularmovies_2;

import com.droidbayapps.popularmovies_2.model.MovieDiscovery;

/**
 * Created by stephen on 2/20/2016.
 */
public interface MovieSelectedCallback {
    void onMovieSelected(MovieDiscovery.MovieData movieData);
}
