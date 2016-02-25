package com.droidbayapps.popularmovies_2.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by stephen on 2/7/2016.
 */
public class MovieReviewQuery {
    List<MovieReview> results;


    public List<MovieReview> getResults() {
        return results;
    }

    public void setResults(List<MovieReview> results) {
        this.results = results;
    }

    public static class MovieReview implements Parcelable{
        String author;
        String content;

        protected MovieReview(Parcel in) {
            author = in.readString();
            content = in.readString();
        }

        public static final Creator<MovieReview> CREATOR = new Creator<MovieReview>() {
            @Override
            public MovieReview createFromParcel(Parcel in) {
                return new MovieReview(in);
            }

            @Override
            public MovieReview[] newArray(int size) {
                return new MovieReview[size];
            }
        };

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(author);
            dest.writeString(content);
        }
    }
}
