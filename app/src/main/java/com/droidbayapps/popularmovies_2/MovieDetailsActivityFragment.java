package com.droidbayapps.popularmovies_2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.droidbayapps.popularmovies_2.data.FavoritesColumns;
import com.droidbayapps.popularmovies_2.data.FavoritesProvider;
import com.droidbayapps.popularmovies_2.model.MovieDiscovery;
import com.droidbayapps.popularmovies_2.model.MovieReviewQuery;
import com.droidbayapps.popularmovies_2.model.MovieTrailerQuery;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsActivityFragment extends Fragment implements  View.OnClickListener{

    MovieDiscovery.MovieData mMovieData = null;
    View mRootView = null;
    private final String LOG_TAG = MovieDetailsActivityFragment.class.toString();
    private ListView mListView;
    private MovieDetailsAdapter mAdapter;
    private Menu menu;

    public MovieDetailsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movie_details_fragment, menu);

        this.menu = menu;
    }

    private Intent createShareTrailerIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.youtube_base_url, mMovieData.getTrailers().get(0).getKey()));
        return shareIntent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_movie_details, container, false);
        mListView = (ListView)mRootView.findViewById(R.id.detailsList);

        if(null == savedInstanceState) {
            // Not restoring a saved state, so get the arguments and extract the movie data
            Bundle args = getArguments();
            if(args != null)
                mMovieData = args.getParcelable(MovieDiscovery.MovieData.PARCELABLE_KEY);

            // If there was no movie data, set the title text to indicate that
            // the user should pick a movie
            if(null == mMovieData){
                TextView textView = (TextView)mRootView.findViewById(R.id.movieTitleTextView);

                if(textView != null)
                    textView.setText(getString(R.string.no_movie_selected_text));
            }
        }
        else{
            // Restoring from a saved instance. Get the movie data from the saved instance state
            mMovieData = savedInstanceState.getParcelable(MovieDiscovery.MovieData.PARCELABLE_KEY);
        }

        // Create the adapter and bind it to the list view
        mAdapter = new MovieDetailsAdapter();
        mListView.setAdapter(mAdapter);

        return mRootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the movie data
        outState.putParcelable(MovieDiscovery.MovieData.PARCELABLE_KEY, mMovieData);
    }

    @Override
    public void onStart() {
        super.onStart();

        loadMovieDetails();
    }

    private void loadMovieDetails() {
        if(mMovieData != null && mRootView != null){

            // We've got movie data.
            // Set the title text
            TextView textView = (TextView) mRootView.findViewById(R.id.movieTitleTextView);
            textView.setText(mMovieData.getOriginal_title());

            // If we haven't retrieved the reviews and trailers yet, go ahead and do that
            if(mMovieData.getReviews() == null || mMovieData.getTrailers() == null) {
                FetchTrailersAndReviewsTask task = new FetchTrailersAndReviewsTask();
                task.execute();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void updateOptionsMenu(){
        // This method diables the sharing option if there are no trailers for the movie (or
        // if I was not able to retrieve them from TheMovieDB)
        if(menu != null && getActivity() != null) {
            MenuItem item = menu.findItem(R.id.action_share);

            if(mMovieData != null && mMovieData.getTrailers() != null && mMovieData.getTrailers().size() > 0){
                // Get the provider and hold onto it to set/change the share intent.
                ShareActionProvider mShareActionProvider =
                        (ShareActionProvider) MenuItemCompat.getActionProvider(item);

                // Attach an intent to this ShareActionProvider.  You can update this at any time,
                // like when the user selects a new piece of data they might like to share.
                if (mShareActionProvider != null && getActivity() != null) {
                    mShareActionProvider.setShareIntent(createShareTrailerIntent());
                }
            }
            else{
                item.setVisible(false);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.favorite:
                CheckBox checkBox = (CheckBox)v;
                if(checkBox.isChecked()) {
                    // First, let's query for this movie and see if it's already a Favorite
                    Uri targetUri = FavoritesProvider.Favorites.withId(mMovieData.getId());
                    Cursor c = getActivity().getContentResolver().query(targetUri, null, null, null, null);

                    if(c != null && c.moveToFirst()){
                        // This movie is already a Favorite. Shouldn't get to this code, but if we do,
                        // don't add it to the Favorites table again.
                        Toast.makeText(getActivity(), "This movie is already in the Favorites list", Toast.LENGTH_SHORT).show();
                        c.close();
                    }
                    else {
                        // First, save the image to the external storage
                        Uri imageUri = saveImageToInternalStorage();

                        // Add it to the Favorites table
                        ContentValues values = new ContentValues();
                        values.put(FavoritesColumns.NAME, mMovieData.getOriginal_title());
                        values.put(FavoritesColumns.POSTER_URI, imageUri != null ? imageUri.toString() : mMovieData.getPoster_path());
                        values.put(FavoritesColumns.PLOT_SUMMARY, mMovieData.getOverview());
                        values.put(FavoritesColumns.AVERAGE_RATING, mMovieData.getVote_average());
                        values.put(FavoritesColumns.RELEASE_DATE, mMovieData.getRelease_date());
                        values.put(FavoritesColumns.MOVIE_ID, mMovieData.getId());
                        Uri insertResultUri = getActivity().getContentResolver().insert(FavoritesProvider.Favorites.CONTENT_URI, values);

                        // Lastly, make sure the row was added as expected
                        if (insertResultUri != null)
                            Toast.makeText(getActivity(), "Added to Favorites", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), "An error occurred. Unable to add to favorites!", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    // Remove this movie from the favorites table
                    int rowsDeleted = getActivity().getContentResolver().delete(FavoritesProvider.Favorites.withId(mMovieData.getId()), null, null);
                    File imageFile = new File(getActivity().getFilesDir(), mMovieData.getId()+".png");

                    // Delete the image file from local storage if it exists (which it should)
                    if(imageFile.exists())
                        imageFile.delete();

                    // If we successfully deleted a row, tell the user it was removed from favorites
                    if(1 == rowsDeleted)
                        Toast.makeText(getActivity(), "Removed from Favorites", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), "An error occurred. Unable to remove from favorites!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private Uri saveImageToInternalStorage() {
        Uri imageUri = null;

        // Get the image as a bitmap that we can stream to a file
        ImageView imageView = (ImageView) mRootView.findViewById(R.id.moviePoster);
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        File imageFile = new File(getActivity().getFilesDir(), mMovieData.getId()+".png");

        // Encode the file as a PNG image.
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            /* 100 to keep full quality of the image */

            imageUri = Uri.fromFile(imageFile);

            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageUri;
    }

    private class MovieDetailsAdapter extends BaseAdapter{
        private static final int NUM_VIEW_TYPES = 5;
        private static final int VIEW_TYPE_DETAILS = 0;
        private static final int VIEW_TYPE_TRAILER_HEADING = 1;
        private static final int VIEW_TYPE_TRAILER = 2;
        private static final int VIEW_TYPE_REVIEW_HEADING = 3;
        private static final int VIEW_TYPE_REVIEW = 4;

        private LayoutInflater mInflater;
        private int mNumTrailers;
        private int mNumReviews;
        private int mTrailerHeadingPosition;
        private int mFirstTrailerPosition;
        private int mReviewHeadingPosition;
        private int mFirstReviewPosition;

        public MovieDetailsAdapter() {
            mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            mNumTrailers = mNumReviews = mTrailerHeadingPosition = mReviewHeadingPosition = mFirstTrailerPosition = mFirstReviewPosition = 0;

            if(mMovieData != null) {
                // To get the count, determine how many trailers and reviews there are. Also,
                // determine the starting position of the reviews and trailers (for later use)
                mNumTrailers = mMovieData.getTrailers() != null ? mMovieData.getTrailers().size() : 0;
                int TrailerHeading = mNumTrailers > 0 ? 1 : 0;
                mTrailerHeadingPosition = mNumTrailers > 0 ? 1 : 0;
                mFirstTrailerPosition = mNumTrailers > 0 ? 2 : 0;
                mNumReviews = mMovieData.getReviews() != null ? mMovieData.getReviews().size() : 0;
                if (mNumReviews > 0) {
                    if (mNumTrailers > 0)
                        mReviewHeadingPosition = mFirstTrailerPosition + mNumTrailers;
                    else
                        mFirstReviewPosition = 1;

                    mFirstReviewPosition = mReviewHeadingPosition + 1;
                }
                int reviewHeading = mReviewHeadingPosition > 0 ? 1 : 0;

                // Total number of items in the list is:
                // The "details" item (poster, rating, release date, favorite)
                // + the heading item for the trailers (if any trailers were retrieved)
                // + the number of trailers retrieved from TheMovieDB
                // + the heading item for the reviews (if any reviews were retrieved)
                // + the number of reviews retrieved from TheMovieDB
                return 1/*details*/ + TrailerHeading + mNumTrailers + reviewHeading + mNumReviews;
            }
            else return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            int type = getItemViewType(position);
            if (convertView == null) {
                holder = new ViewHolder();
                switch (type) {
                    case VIEW_TYPE_DETAILS:
                        convertView = mInflater.inflate(R.layout.details_details_layout, mListView, false);
                        holder.releaseDateTextView = (TextView)convertView.findViewById(R.id.releaseDateTextView);
                        holder.ratingTextView = (TextView)convertView.findViewById(R.id.ratingTextView);
                        holder.moviePosterView = (ImageView)convertView.findViewById(R.id.moviePoster);
                        holder.favoriteCheckBox = (CheckBox)convertView.findViewById(R.id.favorite);
                        holder.plotSynopsisTextView = (TextView)convertView.findViewById(R.id.plotSynopsisTextView);

                        break;
                    case VIEW_TYPE_TRAILER_HEADING:
                        convertView = mInflater.inflate(R.layout.details_list_header_item, mListView, false);
                        holder.sectionHeadingTextView = (TextView)convertView.findViewById(R.id.sectionHeading);
                        break;
                    case VIEW_TYPE_TRAILER:
                        convertView = mInflater.inflate(R.layout.details_trailer_item, mListView, false);
                        holder.trailerNameTextView = (TextView)convertView.findViewById(R.id.trailerName);
                        convertView.setTag(R.integer.trailer_id_tag_key, mMovieData.getTrailers().get(position - mFirstTrailerPosition).getKey());
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String key = (String)v.getTag(R.integer.trailer_id_tag_key);
                                if(key != null){
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+key)));
                                }
                            }
                        });
                        break;
                    case VIEW_TYPE_REVIEW_HEADING:
                        convertView = mInflater.inflate(R.layout.details_list_header_item, mListView, false);
                        holder.sectionHeadingTextView = (TextView)convertView.findViewById(R.id.sectionHeading);
                        break;
                    case VIEW_TYPE_REVIEW:
                        convertView = mInflater.inflate(R.layout.details_review_layout, mListView, false);
                        holder.reviewTextView = (TextView)convertView.findViewById(R.id.reviewText);
                        holder.reviewerNameTextView = (TextView)convertView.findViewById(R.id.reviewerName);
                        break;
                }

                if(convertView != null)
                    convertView.setTag(R.integer.view_holder_tag_key, holder);
            } else {
                holder = (ViewHolder)convertView.getTag(R.integer.view_holder_tag_key);
            }

            switch (type){
                case VIEW_TYPE_DETAILS:
                    String releaseDate = "????";
                    try {
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format_string));
                        calendar.setTime(sdf.parse(mMovieData.getRelease_date()));
                        releaseDate = String.format("%d", calendar.get(Calendar.YEAR));
                    }
                    catch(ParseException e){
                        e.printStackTrace();
                    }

                    holder.releaseDateTextView.setText(releaseDate);
                    holder.ratingTextView.setText(mMovieData.getVote_average());
                    Picasso.with(getActivity()).load(mMovieData.getPoster_path()).into(holder.moviePosterView);
                    Cursor c = getActivity().getContentResolver().query(FavoritesProvider.Favorites.withId(mMovieData.getId()), null, null, null, null);

                    if (c != null && c.moveToFirst()){
                        // This is a favorite
                        holder.favoriteCheckBox.setChecked(true);
                        c.close();
                    }
                    holder.favoriteCheckBox.setOnClickListener(MovieDetailsActivityFragment.this);

                    holder.plotSynopsisTextView.setText(mMovieData.getOverview());
                    break;
                case VIEW_TYPE_TRAILER_HEADING:
                    holder.sectionHeadingTextView.setText(getString(R.string.trailers_heading_text));
                    break;
                case VIEW_TYPE_TRAILER:
                    MovieTrailerQuery.MovieTrailer trailer = mMovieData.getTrailers().get(position - mFirstTrailerPosition);
                    if(trailer != null)
                        holder.trailerNameTextView.setText(trailer.getName());
                    else
                        holder.trailerNameTextView.setText(getString(R.string.no_trailer_text));
                    break;
                case VIEW_TYPE_REVIEW_HEADING:
                    holder.sectionHeadingTextView.setText(getString(R.string.reviews_heading_text));
                    break;
                case VIEW_TYPE_REVIEW:
                    int reviewIndex = position - mFirstReviewPosition;//position - 1 - NumTrailers;
                    MovieReviewQuery.MovieReview review = mMovieData.getReviews().get(reviewIndex);
                    holder.reviewTextView.setText(getString(R.string.review_text_format_string, review.getContent()));
                    holder.reviewerNameTextView.setText(getString(R.string.review_author_format_string, review.getAuthor()));
                    break;
            }

            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return NUM_VIEW_TYPES;
        }

        @Override
        public int getItemViewType(int position) {
            if(0 == position)
                return VIEW_TYPE_DETAILS;
            else {
                int viewType = -1;
                if(mNumTrailers > 0){
                    if(mTrailerHeadingPosition == position)
                        viewType = VIEW_TYPE_TRAILER_HEADING;
                    else if(position >= mFirstTrailerPosition && position < (mFirstTrailerPosition + mNumTrailers))
                        viewType = VIEW_TYPE_TRAILER;
                }

                if(viewType < 0){
                    if(mNumReviews > 0) {
                        if(mReviewHeadingPosition == position)
                            viewType = VIEW_TYPE_REVIEW_HEADING;
                        else if(position >= mFirstReviewPosition && position < (mFirstReviewPosition + mNumReviews))
                            viewType = VIEW_TYPE_REVIEW;
                        else
                            throw new IndexOutOfBoundsException();
                    }
                    else
                        throw new IndexOutOfBoundsException();
                }

                return viewType;
            }
        }

    }

    public static class ViewHolder{
        // Not sure if this is proper form or not, but I'm just using one type of ViewHolder
        // that has a reference for any view that any layout in the list will need
        public TextView releaseDateTextView;
        public TextView ratingTextView;
        public CheckBox favoriteCheckBox;
        public ImageView moviePosterView;
        public TextView plotSynopsisTextView;

        public TextView trailerNameTextView;

        public TextView reviewTextView;
        public TextView reviewerNameTextView;

        public TextView sectionHeadingTextView;
    }

    private class FetchTrailersAndReviewsTask extends AsyncTask<Void, Void, Void>{
        List<MovieTrailerQuery.MovieTrailer> mTrailers;
        List<MovieReviewQuery.MovieReview> mReviews;

        @Override
        protected Void doInBackground(Void... params) {
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(getString(R.string.themoviedb_base_uri))
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                MovieDBService.MovieDBAPI mdbapi = retrofit.create(MovieDBService.MovieDBAPI.class);
                Call<MovieTrailerQuery> trailerCall = mdbapi.getTrailers(Integer.toString(mMovieData.getId()), getString(R.string.api_key));
                Call<MovieReviewQuery> reviewCall = mdbapi.getReviews(Integer.toString(mMovieData.getId()), getString(R.string.api_key));
                try {
                    // Get the movie trailers
                    MovieTrailerQuery trailerInfo = trailerCall.execute().body();
                    mTrailers = trailerInfo.getResults();

                    // Get the reviews
                    MovieReviewQuery reviewInfo = reviewCall.execute().body();
                    mReviews = reviewInfo.getResults();

                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(mMovieData != null){
                if(mTrailers != null)
                    mMovieData.setTrailers(mTrailers);
                if(mReviews != null)
                    mMovieData.setReviews(mReviews);

                mAdapter.notifyDataSetChanged();

                updateOptionsMenu();
            }
        }
    }
}
