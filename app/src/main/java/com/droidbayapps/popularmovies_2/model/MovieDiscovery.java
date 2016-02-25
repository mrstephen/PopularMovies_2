package com.droidbayapps.popularmovies_2.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by stephen on 2/3/2016.
 */
public class MovieDiscovery {
    public List<MovieData> getResults(){
        return results;
    }
    public void setResults(List<MovieData> results){
        this.results = results;
    }

    private List<MovieData> results;

    public static class MovieData implements Parcelable{
        public final static String PARCELABLE_KEY = MovieData.class.toString();

        String poster_path;
        String overview;
        String release_date;
        int id;
        String original_title;
        String popularity;
        String vote_average;

        List<MovieTrailerQuery.MovieTrailer> mTrailers = null;
        List<MovieReviewQuery.MovieReview> mReviews = null;

        public MovieData(String title,
                         String posterUri,
                         String plotSynopsis,
                         String averageRating,
                         String releaseDate,
                         int movieID,
                         String popularity){
            original_title = title;
            poster_path = posterUri;
            overview = plotSynopsis;
            vote_average = averageRating;
            release_date = releaseDate;
            id = movieID;
            this.popularity = popularity;
        }

        protected MovieData(Parcel in) {
            poster_path = in.readString();
            overview = in.readString();
            release_date = in.readString();
            id = in.readInt();
            original_title = in.readString();
            popularity = in.readString();
            vote_average = in.readString();

            int NumTrailers = in.readInt();

            if(NumTrailers > 0){
                in.readList(mTrailers, MovieTrailerQuery.MovieTrailer.class.getClassLoader());
            }

            int NumReviews = in.readInt();

            if(NumReviews > 0){
                in.readList(mReviews, MovieReviewQuery.MovieReview.class.getClassLoader());
            }
        }

        public static final Creator<MovieData> CREATOR = new Creator<MovieData>() {
            @Override
            public MovieData createFromParcel(Parcel in) {
                return new MovieData(in);
            }

            @Override
            public MovieData[] newArray(int size) {
                return new MovieData[size];
            }
        };

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getOriginal_title() {
            return original_title;
        }

        public void setOriginal_title(String original_title) {
            this.original_title = original_title;
        }

        public String getOverview() {
            return overview;
        }

        public void setOverview(String overview) {
            this.overview = overview;
        }

        public String getPopularity() {
            return popularity;
        }

        public void setPopularity(String popularity) {
            this.popularity = popularity;
        }

        public String getRelease_date() {
            return release_date;
        }

        public void setRelease_date(String release_date) {
            this.release_date = release_date;
        }

        public String getVote_average() {
            return vote_average;
        }

        public void setVote_average(String vote_average) {
            this.vote_average = vote_average;
        }

        public String getPoster_path(){
            return poster_path;
        }

        public void setPoster_path(String poster_path){
            this.poster_path = poster_path;
        }

        public List<MovieTrailerQuery.MovieTrailer> getTrailers() {
            return mTrailers;
        }

        public void setTrailers(List<MovieTrailerQuery.MovieTrailer> mTrailers) {
            this.mTrailers = mTrailers;
        }

        public List<MovieReviewQuery.MovieReview> getReviews() {
            return mReviews;
        }

        public void setReviews(List<MovieReviewQuery.MovieReview> mReviews) {
            this.mReviews = mReviews;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(poster_path);
            dest.writeString(overview);
            dest.writeString(release_date);
            dest.writeInt(id);
            dest.writeString(original_title);
            dest.writeString(popularity);
            dest.writeString(vote_average);

            // Save the trailers
            int NumTrailers = mTrailers != null ? mTrailers.size() : 0;

            dest.writeInt(NumTrailers);

            if(NumTrailers > 0)
                dest.writeList(mTrailers);

            // Save the reviews
            int NumReviews = mReviews != null ? mReviews.size() : 0;

            dest.writeInt(NumReviews);

            if(NumReviews > 0)
                dest.writeList(mReviews);
        }
    }
}
