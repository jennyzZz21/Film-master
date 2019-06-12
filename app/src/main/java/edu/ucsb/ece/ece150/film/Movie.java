package edu.ucsb.ece.ece150.film;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;

public class Movie implements Parcelable, Comparable {
    private String title;
    private String year;
    private String language;
    private String country;
    private String plot;
    private String url;

    public void setTitle (String t){ this.title = t; }
    public void setYear (String y){ this.year = y; }
    public void setLanguage (String l){ this.language = l; }
    public void setCountry (String c){ this.country = c; }
    public void setUrl (String u){ this.url = u; }

    public String getTitle (){ return this.title;}
    public String getYear (){ return this.year;}
    public String getLanguage (){ return this.language;}
    public String getCountry (){ return this.country;}
    public String getPlot (){ return this.plot;}
    public String getUrl (){ return this.url;}

    public Movie(String title, String year, String language, String country, String plot, String url){
        this.title = title;
        this.year = year;
        this.language = language;
        this.country = country;
        this.plot = plot;
        this.url = url;
    }

    public Movie(Parcel in){
        String[] data = new String[6];
        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.title = data[0];
        this.year = data[1];
        this.language = data[2];
        this.country = data[3];
        this.plot = data[4];
        this.url = data[5];
    }

    @Override
    public int compareTo(Object o){
        Movie compMovie = (Movie) o;
        return this.title.compareTo(compMovie.title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Movie movie = (Movie) o;
        return title.equals(movie.title) &&
                year.equals(movie.year) &&
                language.equals(movie.language) &&
                country.equals(movie.country) &&
                plot.equals(movie.plot);
    }

    @Override
    public int hashCode(){
        String hashStr = title + year + language + country + plot;
        return hashStr.hashCode();
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.title, this.year, this.language, this.country,
                this.plot, this.url});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public static void removeMovie(Context context, ArrayList<Movie> movies, Movie removingMovie, String listName){
        //Remove movie from sharedPreferences
        removeMovieList(context, listName);

        //Remove movie from movies and resave movies
        movies.remove(removingMovie);
        saveMovieList(context, movies, listName);
    }


    public static void saveMovieList(Context context, ArrayList<Movie> movies, String listName){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int length = movies.size();
        sharedPreferences.edit().putInt(listName + "Length", length).apply();
        for(int i = 0; i < length; i++){
            Movie currMovie = movies.get(i);
            String movieStr = new Gson().toJson(currMovie);
            sharedPreferences.edit().putString(listName + i, movieStr).apply();
        }
        sharedPreferences.edit().commit();
    }

    public static void removeMovieList(Context context, String listName){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int length = sharedPreferences.getInt(listName + "Length", 0);
        for (int i = 0; i < length; i++){
            sharedPreferences.edit().remove(listName + i).apply();
        }
        sharedPreferences.edit().remove(listName + "Length").apply();
        sharedPreferences.edit().commit();
    }

    public static ArrayList<Movie> loadMovieList(Context context, String listName){
        ArrayList<Movie> movies = new ArrayList<Movie>();
        try{
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            int length = sharedPreferences.getInt(listName + "Length", 0);
            for (int i = 0; i < length; i++){
                String json = sharedPreferences.getString(listName + i , null);
                Movie currMovie = new Gson().fromJson(json, Movie.class);
                movies.add(currMovie);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return movies;
    }



}
