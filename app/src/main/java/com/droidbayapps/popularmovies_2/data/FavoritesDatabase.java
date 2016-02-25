package com.droidbayapps.popularmovies_2.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by stephen on 1/27/2016.
 */
@Database(version = FavoritesDatabase.VERSION)
public class FavoritesDatabase {
    private FavoritesDatabase(){}

    public static final int VERSION = 1;

    @Table(FavoritesColumns.class) public static final String FAVORITES = "favorites";
}
