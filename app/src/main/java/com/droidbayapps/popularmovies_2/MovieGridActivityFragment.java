package com.droidbayapps.popularmovies_2;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.droidbayapps.popularmovies_2.data.FavoritesColumns;
import com.droidbayapps.popularmovies_2.data.FavoritesProvider;
import com.droidbayapps.popularmovies_2.model.MovieDiscovery;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieGridActivityFragment extends Fragment {

    private GridView mGridView = null;
    private MovieListAdapter mAdapter = null;
    //private MovieData [] mMovieData = null;
    private ArrayList</*MovieData*/MovieDiscovery.MovieData> mMovieList = null;
    private ArrayList</*MovieData*/MovieDiscovery.MovieData> mFromMovieDB = null;
    private static final String MOVIE_KEY = "movie_data_array";
    //private Call<MovieDiscovery> call;
    private MovieDiscovery discovery;
    //private List<MovieDiscovery.MovieData> movieData;

    public MovieGridActivityFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null && savedInstanceState.containsKey(MOVIE_KEY)){
            /*mMovieList*/mFromMovieDB = (ArrayList</*MovieData*/MovieDiscovery.MovieData>)savedInstanceState.get(MOVIE_KEY);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movie_grid_fragment, menu);

        if(getActivity() == null)
            return;

        MenuItem item = menu.findItem(R.id.sortSpinner);
        final Spinner s = (Spinner) MenuItemCompat.getActionView(item);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.sort_method_array, R.layout.sort_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(R.layout.sort_spinner_dropdown_item);
        s.setAdapter(spinnerAdapter);

        // Had to post the following code as a runnable for this reason: When the spinner layout is created, it
        // automatically sets the selected item to 0. I researched a solution, but it seems that there
        // is no way to prevent this. And if that happens after the OnItemSelectedListener
        // has been set, then it will overwrite the user-selected sort option force it
        // to "Most popular".
        // Posting this as a runnable allows the OnItemSelectedListener to be set after this occurs.
        s.post(new Runnable() {
            @Override
            public void run() {
                if(getActivity() == null)
                    return;

                // Load the sort preference and set the appropriate value in the spinner
                SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE);
                String sortPreference = preferences.getString(getString(R.string.sort_preference_key), getString(R.string.sort_popularity_desc));

                String[] values = getResources().getStringArray(R.array.sort_query_values);

                // Find the corresponding item in the spinner and select it
                if (values != null) {
                    int sortMethodIndex = -1;
                    for (int idx = 0; sortMethodIndex < 0 && idx < values.length; idx++) {
                        if (values[idx].equals(sortPreference)) {
                            sortMethodIndex = idx;
                        }
                    }

                    if (sortMethodIndex >= 0) {
                        s.setSelection(sortMethodIndex, false);
                    }
                }

                // Set the OnItemSelectedListener to respond to user selection
                s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String[] values = getResources().getStringArray(R.array.sort_query_values);

                        // Save the new sort option in the SharedPreferences
                        String sortMethod = values[position];
                        SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE);
                        preferences.edit().putString(getString(R.string.sort_preference_key), sortMethod).apply();

                        refreshMovieList();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_movie_grid, container, false);

        mGridView = (GridView)rootView.findViewById(R.id.movieThumbnailGridView);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (/*mMovieData*/mMovieList != null && /*mMovieData.length*/mMovieList.size() > position) {
                    if (mMovieList.get(position).getOriginal_title().isEmpty()) {
                        Toast toast = Toast.makeText(getActivity(), getString(R.string.no_movie_error_msg), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    } else {
                        MovieSelectedCallback callback = (MovieSelectedCallback)getActivity();
                        if(callback != null)
                            callback.onMovieSelected(mMovieList.get(position));

                    }
                }
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        refreshMovieList();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mFromMovieDB != null)
            outState.putParcelableArrayList(MOVIE_KEY, mFromMovieDB);
    }

    private void refreshMovieList() {
        SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE);
        final String sortPreference = preferences.getString(getActivity().getString(R.string.sort_preference_key), getString(R.string.sort_popularity_desc));

        if(null == mMovieList)
            mMovieList = new ArrayList<>();

        mMovieList.clear();

        if(sortPreference.equals(getString(R.string.sort_favorites))){
            // Get the list of movies from the favorites db
            Cursor c = getActivity().getContentResolver().query(FavoritesProvider.Favorites.CONTENT_URI, null, null, null, FavoritesColumns._ID+" ASC");

            if(c != null && c.moveToFirst()){

                do {
                    mMovieList.add(new /*MovieData*/MovieDiscovery.MovieData(c.getString(c.getColumnIndex(FavoritesColumns.NAME)),
                            c.getString(c.getColumnIndex(FavoritesColumns.POSTER_URI)),
                            c.getString(c.getColumnIndex(FavoritesColumns.PLOT_SUMMARY)),
                            c.getString(c.getColumnIndex(FavoritesColumns.AVERAGE_RATING)),
                            c.getString(c.getColumnIndex(FavoritesColumns.RELEASE_DATE)),
                            c.getInt(c.getColumnIndex(FavoritesColumns.MOVIE_ID)),
                            "1"
                    ));
                } while(c.moveToNext());

                c.close();
            }

            updateAdapter();
        }
        else {
            // Get the movies from TheMovieDB if we haven't already
            if(null == mFromMovieDB) {
                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("http://api.themoviedb.org")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    MovieDBService.MovieDBAPI mdbapi = retrofit.create(MovieDBService.MovieDBAPI.class);
                    Call<MovieDiscovery> call = mdbapi.getMovies(getString(R.string.api_key), getString(R.string.sort_popularity_desc));
                    call.enqueue(new Callback<MovieDiscovery>() {
                        @Override
                        public void onResponse(Response<MovieDiscovery> response) {
                            if(getActivity() == null)
                                return;

                            try{
                                discovery = response.body();
                                mFromMovieDB = new ArrayList<>(discovery.getResults());


                                for(MovieDiscovery.MovieData r : mFromMovieDB){
                                    r.setPoster_path(getString(R.string.themoviedb_poster_base_uri) + r.getPoster_path());
                                }
                                mMovieList.addAll(mFromMovieDB);

                                SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE);
                                String sortPreference = preferences.getString((getString(R.string.sort_preference_key)), getString(R.string.sort_popularity_desc));

                                // The list is sorted by popularity by default, so if "by popularity" is the choice, do nothing.
                                // If it's sorted by rating, then re-sort the list
                                if(sortPreference.compareTo(getString(R.string.sort_rating_desc)) == 0){
                                    Collections.sort(mMovieList, new AverageRatingComparator());
                                }

                                updateAdapter();
                            }
                            catch (NullPointerException e){
                                Toast.makeText(getActivity(), "We couldn't download the movie data.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Toast toast = Toast.makeText(getActivity(), getString(R.string.no_movie_error_msg), Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            updateAdapter();

                        }
                    });

                } else {
                    Toast.makeText(getActivity(), getString(R.string.notifyNoInternetPermission), Toast.LENGTH_LONG).show();
                    updateAdapter();
                }
            }
            else{
                // Re-sort the movie list based on the selection
                //mMovieList.clear();
                mMovieList.addAll(mFromMovieDB);

                if(sortPreference.equals(getString(R.string.sort_popularity_desc)))
                    Collections.sort(mMovieList, new PopularityComparator());
                else if(sortPreference.equals(getString(R.string.sort_rating_desc)))
                    Collections.sort(mMovieList, new AverageRatingComparator());

                updateAdapter();
            }
        }

    }

    private void updateAdapter(){
        if(null == mAdapter) {
            if(mMovieList != null) {
                mAdapter = new MovieListAdapter(getActivity(), mMovieList);
                mGridView.setAdapter(mAdapter);
            }
        }
        else
            mAdapter.notifyDataSetChanged();
    }

}
