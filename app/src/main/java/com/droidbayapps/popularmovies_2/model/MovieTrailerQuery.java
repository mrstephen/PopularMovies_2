package com.droidbayapps.popularmovies_2.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by stephen on 2/5/2016.
 */
public class MovieTrailerQuery {
    List<MovieTrailer> results;


    public List<MovieTrailer> getResults() {
        return results;
    }

    public void setResults(List<MovieTrailer> results) {
        this.results = results;
    }

    public static class MovieTrailer implements Parcelable{
        String id;
        String key;
        String name;

        protected MovieTrailer(Parcel in) {
            id = in.readString();
            key = in.readString();
            name = in.readString();
        }

        public static final Creator<MovieTrailer> CREATOR = new Creator<MovieTrailer>() {
            @Override
            public MovieTrailer createFromParcel(Parcel in) {
                return new MovieTrailer(in);
            }

            @Override
            public MovieTrailer[] newArray(int size) {
                return new MovieTrailer[size];
            }
        };

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(key);
            dest.writeString(name);
        }
    }
}