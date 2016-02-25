package com.droidbayapps.popularmovies_2.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by stephen on 1/27/2016.
 */

@ContentProvider(authority = FavoritesProvider.AUTHORITY, database = FavoritesDatabase.class)
public final class FavoritesProvider {
    public static final String AUTHORITY = "com.droidbayapps.popularmovies_2.FavoritesProvider";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // table path(s)? Putting this in for now because that's what they did in the webcast
    // "we're going to use these to build our Uri's"
    interface Path{
        String FAVORITES = "favorites";
    }

    private static Uri buildUri(String ... paths){
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for(String path : paths){
            builder.appendPath(path);
        }

        return builder.build();
    }

    @TableEndpoint(table = FavoritesDatabase.FAVORITES) public static class Favorites{
        @ContentUri(
                path = Path.FAVORITES,
                type = "vnd.android.cusor.dir/favorites",
                defaultSort = FavoritesColumns._ID + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.FAVORITES);

        @InexactContentUri(
                name = "MOVIE_ID",
                path = Path.FAVORITES + "/#",
                type = "vnd.android.cursor.item/favorite_movie",
                whereColumn = FavoritesColumns.MOVIE_ID,
                pathSegment = 1)
        public static final Uri withId(long id){
            return buildUri(Path.FAVORITES, String.valueOf(id));
        }
    }

}
