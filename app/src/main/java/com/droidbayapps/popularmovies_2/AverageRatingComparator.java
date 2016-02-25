package com.droidbayapps.popularmovies_2;

import com.droidbayapps.popularmovies_2.model.MovieDiscovery;

/**
 * Created by stephen on 12/19/2015.
 */
public class AverageRatingComparator implements java.util.Comparator</*MovieData*/MovieDiscovery.MovieData> {
    @Override
    public int compare(/*MovieData*/MovieDiscovery.MovieData lhs, /*MovieData*/MovieDiscovery.MovieData rhs) {
        double leftRating = Double.parseDouble(lhs.getVote_average());
        double rightRating = Double.parseDouble(rhs.getVote_average());

        // comparing them backward because Collections.List sorts in ascending order
        return Double.compare(rightRating, leftRating);
    }
}
