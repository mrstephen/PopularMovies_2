package com.droidbayapps.popularmovies_2.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by stephen on 1/26/2016.
 */
public interface FavoritesColumns {
    @DataType(DataType.Type.INTEGER) @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String NAME = "name";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String POSTER_URI = "poster_uri";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String PLOT_SUMMARY = "plot_summary";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String AVERAGE_RATING = "average_rating";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String RELEASE_DATE = "release_date";

    @DataType(DataType.Type.INTEGER)
    public static final String MOVIE_ID = "movie_id";
}
