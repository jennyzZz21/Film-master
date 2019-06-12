package edu.ucsb.ece.ece150.film;

import android.app.ListActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.os.Bundle;
import android.view.View;
import android.os.Parcel;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class ListViewDemo extends ListActivity {
    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    CustomAdapter adapter;
    ArrayList<Movie> movieList = new ArrayList<Movie>();
    String listName;
    TextView listTitle;
    ListView listView;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_save);
        listTitle = (TextView) findViewById(R.id.textView);
        ListView listView = getListView();

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movieToRemove = movieList.get(position);
                adapter.remove(movieToRemove);
                Movie.removeMovie(getApplicationContext(), movieList, movieToRemove, listName);
                Toast.makeText(getApplicationContext(), "Movie Deleted", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        // Getting the intent passed from MainActivity
        final Intent intent = getIntent();
        try {
            listName = intent.getExtras().getString("ListName");
            listTitle.setText(listName);
            movieList = Movie.loadMovieList(getApplicationContext(), listName);
        } catch(Exception e){
            e.printStackTrace();
        }


        adapter = new CustomAdapter(this, movieList);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
        super.onListItemClick(l, v, position, id);
        Movie currMovie = movieList.get(position);

        Intent intent = new Intent(ListViewDemo.this, MainActivity.class);
        intent.putExtra("MOVIE", currMovie);
        startActivity(intent);
    }

}
