package edu.ucsb.ece.ece150.film;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

public class ListOfLists extends AppCompatActivity {
    ListView listView;
    EditText listNEdit;
    Button addButton;

    ArrayList<String> itemList;
    ArrayAdapter<String> adapter;

    Movie addableMovie;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_lists);

        listView = (ListView) findViewById(R.id.main_list);
        listNEdit = (EditText) findViewById(R.id.editText2);
        addButton = (Button) findViewById(R.id.button6);

        itemList = loadLists(getApplicationContext());
        adapter = new ArrayAdapter<String>(ListOfLists.this, android.R.layout.simple_list_item_1, itemList);
        listView.setAdapter(adapter);

        addButton.setOnClickListener(onButtonClick);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String listToRemove = itemList.get(position);
                adapter.remove(listToRemove);
                removeList(getApplicationContext(), itemList, listToRemove);
                Toast.makeText(getApplicationContext(), "List Deleted", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        final Intent intent = getIntent();
        try{
            addableMovie = intent.getExtras().getParcelable("MOVIE");
            listView.setOnItemClickListener(addToList);
        } catch(Exception e){
            listView.setOnItemClickListener(enterList);
        }
    }

    View.OnClickListener onButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String newListName = listNEdit.getText().toString();
            if(!newListName.equals("")) {
                itemList.add(newListName);
                Collections.sort(itemList);
                listNEdit.setText("");
                adapter.notifyDataSetChanged();

                saveLists(getApplicationContext(), itemList);
            }
        }
    };

    AdapterView.OnItemClickListener enterList = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String listName = itemList.get(position);
            Intent intent = new Intent(ListOfLists.this, ListViewDemo.class);
            intent.putExtra("ListName", listName);
            startActivity(intent);
        }
    };

    AdapterView.OnItemClickListener addToList = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String listName = itemList.get(position);
            ArrayList<Movie> movieList = Movie.loadMovieList(getApplicationContext(), listName);
            if(movieList.contains(addableMovie)){
                Toast.makeText(getApplicationContext(), "Movie already in list", Toast.LENGTH_SHORT).show();
            } else{
                movieList.add(addableMovie);
                Collections.sort(movieList);
                Movie.saveMovieList(getApplicationContext(), movieList, listName);
                Toast.makeText(getApplicationContext(), "Movie added to list", Toast.LENGTH_SHORT).show();
            }
        }
    };


    public void saveLists(Context context, ArrayList<String> lists){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int length = lists.size();
        sharedPreferences.edit().putInt("NumLists", length).apply();
        for(int i = 0; i < length; i++){
            String currList = lists.get(i);
            sharedPreferences.edit().putString("List" + i, currList).apply();
        }
        sharedPreferences.edit().commit();
    }

    public void removeList(Context context, ArrayList<String> lists, String listName){
        //Remove movies in list
        Movie.removeMovieList(context, listName);

        //Remove movieList from sharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int length = sharedPreferences.getInt("NumLists", 0);
        for (int i = 0; i < length; i++){
            sharedPreferences.edit().remove("List" + i).apply();
        }
        sharedPreferences.edit().remove("NumLists").apply();
        sharedPreferences.edit().commit();

        //Remove listName from movieList and resave movieList
        lists.remove(listName);
        saveLists(context, lists);
    }

    public static ArrayList<String> loadLists(Context context){
        ArrayList<String> lists = new ArrayList<String>();
        try{
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            int length = sharedPreferences.getInt("NumLists", 0);
            for (int i = 0; i < length; i++){
                String currList = sharedPreferences.getString("List" + i , null);
                lists.add(currList);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return lists;
    }

}

