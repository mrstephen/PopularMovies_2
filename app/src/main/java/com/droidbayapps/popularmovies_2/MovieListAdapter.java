package com.droidbayapps.popularmovies_2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.droidbayapps.popularmovies_2.model.MovieDiscovery;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by stephen on 12/13/2015.
 */
public class MovieListAdapter extends BaseAdapter{
//    public MovieListAdapter(Context context, int resource, MovieData[] objects) {
//        super(context, resource, objects);
//    }
    Context mContext;
    //MovieData[] mMovieData;
    ArrayList</*MovieData*/MovieDiscovery.MovieData> mMovieList;
    LayoutInflater mInflater;

    public MovieListAdapter(Context c, ArrayList</*MovieData*/MovieDiscovery.MovieData> movieList/*MovieData[] movieData*/){
        mContext = c;
        //mMovieData = movieData;
        mMovieList = movieList;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        //return mMovieData.length;
        return mMovieList.size();
    }

    @Override
    public /*MovieData*/MovieDiscovery.MovieData getItem(int position) {
        return /*mMovieData[position]*/mMovieList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        ImageView imageView;

        if(null == convertView){
            imageView = (ImageView)mInflater.inflate(R.layout.movie_grid_image_view, null);
        }
        else{
            imageView = (ImageView)convertView;
        }

        //String poster
        Picasso.with(mContext)
                .load(this.getItem(position).getPoster_path())
                .placeholder(R.mipmap.movie_placeholder)
                .error(R.mipmap.load_error_image)
                .into(imageView);

        return imageView;
    }
}
