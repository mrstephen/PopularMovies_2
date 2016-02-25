package com.droidbayapps.popularmovies_2;

import com.droidbayapps.popularmovies_2.model.MovieDiscovery;

/**
 * Created by stephen on 12/20/2015.
 */
public class PopularityComparator implements java.util.Comparator</*MovieData*/MovieDiscovery.MovieData> {
    @Override
    public int compare(/*MovieData*/MovieDiscovery.MovieData lhs, /*MovieData*/MovieDiscovery.MovieData rhs) {
        double lhValue = Double.parseDouble(lhs.getPopularity());
        double rhValue = Double.parseDouble(rhs.getPopularity());
        //Switching rhValue and lhValue to get descending order
        return Double.compare(/*lhs.getPopularity()*/rhValue, /*rhs.getPopularity()*/lhValue);
    }
}
