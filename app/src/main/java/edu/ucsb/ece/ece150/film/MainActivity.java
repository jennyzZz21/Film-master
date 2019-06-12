package edu.ucsb.ece.ece150.film;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Locale;

/*
 * Whatever you do, remember: whatever gets the job done is a good first solution.
 * Then start to redo it, keeping the job done, but the solutions more and more elegant.
 *
 * Don't satisfy yourself with the first thing that comes out.
 * Dig through the documentation, read your error logs.
 */
public class MainActivity extends AppCompatActivity {
    TextView tvYear, tvCountry, tvLanguage, tvPlot;
    ImageView ivPoster;

    EditText edMName;

    Movie movie_obj;

    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edMName = findViewById(R.id.edMName);
        tvYear = findViewById(R.id.tvYear);
        tvCountry = findViewById(R.id.tvCountry);
        tvLanguage = findViewById(R.id.tvLanguage);
        tvPlot = findViewById(R.id.tvPlot);
        ivPoster = findViewById(R.id.ivPoster);

        Intent intent = getIntent();
        try {
            movie_obj = intent.getExtras().getParcelable("MOVIE");
            edMName.setText(movie_obj.getTitle());
            drawMovie(movie_obj);
        }catch(Exception e){
            edMName.setText("");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processNFCData(getIntent());
        }
    }

    public void search(View view) {
        final String mName = edMName.getText().toString();
        if (mName.isEmpty()){
            edMName.setError("please provide movie name");
            return;
        }

        // OMDB API Key: 16317712
        final String url = "http://www.omdbapi.com/?t=" + mName + "&plot=full&apikey=16317712";

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.GET, url,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        /*tvYear.setText(response);
                        Log.d("MOVIE", response);*/
                        try {
                            JSONObject movie = new JSONObject(response);
                            String result = movie.getString("Response");

                            if (result.equals("True")){
                                Toast.makeText(MainActivity.this, "Found", Toast.LENGTH_SHORT).show();
                                String title = movie.getString("Title");
                                String year = movie.getString("Year");
                                String country = movie.getString("Country");
                                String language = movie.getString("Language");
                                String plot = movie.getString("Plot");
                                if (plot.length() > 400){plot = plot.substring(0,400) + "...";}
                                String posterUrl = movie.getString("Poster");
                                movie_obj = new Movie(title, year, language, country, plot, posterUrl);
                                drawMovie(movie_obj);
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Movie not found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
        queue.add(request);
    }

    public void drawMovie(Movie movie){
        tvYear.setText("Year: " + movie.getYear());
        tvCountry.setText("Country: " + movie.getCountry());
        tvLanguage.setText("Language: " + movie.getLanguage());
        tvPlot.setText("Plot: " + movie.getPlot());

        String posterUrl = movie.getUrl();
        if (posterUrl.equals("N/A")){ }
        else{
            Picasso.get().load(posterUrl).into(ivPoster);
        }
    }

    public void save(View view) {
        if(movie_obj == null){
            Toast.makeText(getApplicationContext(), "No Movie Selected", Toast.LENGTH_SHORT).show();
        }else{
            Intent intent = new Intent(MainActivity.this, ListOfLists.class);
            intent.putExtra("MOVIE", movie_obj);
            startActivity(intent);
        }
    }

    public void openLists(View view) {
        Intent intent = new Intent(MainActivity.this, ListOfLists.class);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            processNFCData(intent);
        }
    }

    private void processNFCData(Intent inputIntent) {
        Parcelable[] rawMessages =
                inputIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMessages != null && rawMessages.length > 0) {
            NdefMessage[] messages = new NdefMessage[rawMessages.length];
            for (int i = 0; i < rawMessages.length; i++) {
                messages[i] = (NdefMessage) rawMessages[i];
            }
            // only one message sent during the beam
            NdefMessage msg = (NdefMessage) rawMessages[0];
            // record 0 contains the MIME type, record 1 is the AAR, if present
            String base = new String(msg.getRecords()[0].getPayload());
            String str = base;
//            String str = String.format(Locale.getDefault(), "Message entries=%d. Base message is %s", rawMessages.length, base);
            edMName.setText(str);
            search(edMName);
//            Toast.makeText(getApplicationContext(), "Received " +str, Toast.LENGTH_SHORT).show();
        }
    }


    /* **************************************************************
        This will create the NFC Adapter, if available,
        and setup the Callback listener when create message is needed.
     */
    public void share(View view) {
        // Check for available NFC Adapter
        if (mNfcAdapter == null) {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }
        if (mNfcAdapter == null || !mNfcAdapter.isEnabled()) {
            mNfcAdapter = null;
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            return;
        }

        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(onNfcCreateCallback, this);
    }

    private NfcAdapter.CreateNdefMessageCallback onNfcCreateCallback = new NfcAdapter.CreateNdefMessageCallback() {
        @Override
        public NdefMessage createNdefMessage(NfcEvent inputNfcEvent) {
            return createMessage();
        }
    };

    private NdefMessage createMessage() {
        String text = (movie_obj.getTitle());
        NdefMessage msg = new NdefMessage(
                new NdefRecord[]{NdefRecord.createMime(
                        "application/com.example.nfc_demo.mimetype", text.getBytes())
                });
        return msg;
    }
}
