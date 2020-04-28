package edu.temple.bookshelf;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookSelectorInterface, BookDetailsFragment.PlayStatusInterface {

    boolean dualPane;   //Boolean used to say if there are two fragments on the screen or not
    private ArrayList<Book> books;  //current list of books returned from search

    private static final String BOOKS_KEY = "_books";
    private static final String CURRENT_BOOK_KEY = "_currentBook";
    private static final String PLAY_KEY = "_playing";
    private static final String PAUSE_KEY = "_paused";
    private static final String NOW_PLAYING_KEY = "_nowPlaying";
    private static final String PROGRESS_KEY = "_progress";
    private static final String LAST_PROGRESS_KEY = "_lastProgress";
    private static final String BOUND_KEY = "_bound";

    private BookListFragment listFragment;
    private BookDetailsFragment detailsFragment;
    private BookDetailsFragment portraitDetailsFragment;

    private Book currentBook;       //current selected book for display frag
    private String booksUrl = "https://kamorris.com/lab/abp/booksearch.php?search=";
    private int nowPlaying;         //current playing book ID
    private volatile int currentProgress = 0;
    private int lastProgress = 0;
    private boolean playing = false;
    private boolean paused = false;

    RequestQueue requestQueue;
    EditText searchEditText;
    AudiobookService.MediaControlBinder mediaControlBinder;
    ServiceConnection serviceConnection;
    boolean bound = false;

    private String configTag;       //tag used to check which screen configuration is currently being used


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);
        searchEditText = findViewById(R.id.searchText);

        // check if Activity has been loaded before
        if(savedInstanceState != null) {    // if so, set book data to previously loaded data

            //Log.d("TESTTESTTEST", "onCreate: savedInstSt");
            books = savedInstanceState.getParcelableArrayList(BOOKS_KEY);
            currentBook = savedInstanceState.getParcelable(CURRENT_BOOK_KEY);
            nowPlaying = savedInstanceState.getInt(NOW_PLAYING_KEY);
            currentProgress = savedInstanceState.getInt(PROGRESS_KEY);
            lastProgress = savedInstanceState.getInt(LAST_PROGRESS_KEY);
            playing = savedInstanceState.getBoolean(PLAY_KEY);
            paused = savedInstanceState.getBoolean(PAUSE_KEY);
            bound = savedInstanceState.getBoolean(BOUND_KEY);

            listFragment = BookListFragment.newInstance(books);
            detailsFragment = BookDetailsFragment.newInstance(currentBook);

            //load book list frag no matter if we are in portrait or landscape/tablet
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.list_fragment_container, listFragment)
                    .commit();

            //If the BookDetailsFragment is visible (not null) we are in landscape mode or on a larger device
            dualPane = (findViewById(R.id.details_fragment_container) != null);

            //Check if our dualPane is true (landscape or portrait) or false (small screen portrait)
            if(dualPane){   //If it is true, load book details fragments
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.details_fragment_container, detailsFragment)
                        .commit();
            } else {        //False, therefore we are in portrait mode
                if(currentBook != null){    // check if a book from the list is currently selected
                    // if a book is selected add the display fragment of that book to the back stack
                    portraitDetailsFragment = BookDetailsFragment.newInstance(currentBook);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.list_fragment_container, portraitDetailsFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        }

        if(!bound){
            acquireConnection();
        }

        acquireMainConnection();

        findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //Log.d("TESTTESTTEST", "onClick: search clicked");
                String searchUrl = booksUrl + searchEditText.getText().toString();      //search url + text grabbed from search bar
                searchBooks(searchUrl);     //call searchBook function and pass it the search url
                currentBook = null;         //after a search is completed clear the selected book used by display frag
                //Log.d("TESTTESTTEST", "onClick: number of fragments on backstack" +  getSupportFragmentManager().getBackStackEntryCount());
                configTag = (String) findViewById(R.id.main_activity_view).getTag();    //get the config of our current screen
                // check if the screen is in portrait mode and if the display frag is currently displayed after a new search
                if (configTag.equals(getString(R.string.portrait_tag)) && (getSupportFragmentManager().getBackStackEntryCount() > 0)){
                    getSupportFragmentManager().popBackStack();     //since we are in portrait and display frag is displayed pop it from the back stack
                }
                // now check if we are in a landscape or tablet configuration
                if (configTag.equals(getString(R.string.landscape_tag)) || configTag.equals(getString(R.string.tablet_tag))){
                    // if so, replace the display frag with the previously selected book with an empty display frag
                    detailsFragment = BookDetailsFragment.newInstance(currentBook);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.details_fragment_container, detailsFragment)
                            .commit();
                }
            }
        });
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(BOOKS_KEY, books);
        outState.putParcelable(CURRENT_BOOK_KEY, currentBook);
        outState.putInt(NOW_PLAYING_KEY, nowPlaying);
        outState.putInt(PROGRESS_KEY, currentProgress);
        outState.putInt(LAST_PROGRESS_KEY, lastProgress);
        outState.putBoolean(PLAY_KEY, playing);
        outState.putBoolean(PAUSE_KEY, paused);
        outState.putBoolean(BOUND_KEY, bound);
    }

    @Override
    public void onRestart(){
        super.onRestart();
        if(!bound){
            acquireConnection();
        }
        acquireMainConnection();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        books = savedInstanceState.getParcelableArrayList(BOOKS_KEY);
        currentBook = savedInstanceState.getParcelable(CURRENT_BOOK_KEY);

    }

    public void searchBooks(String searchURL){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(searchURL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response){
                        try {
                            ArrayList<Book> searchBooks = new ArrayList<>();
                            if(response.length() > 0) {
                                if(books != null) {     // if there are currently books in the arrayList
                                    books.clear();      // clear the list
                                }
                                for(int i = 0; i < response.length(); i++){ // iterate through the returned json array
                                    searchBooks.add(new Book(response.getJSONObject(i))); // and create a Book object from each jsonObject then add it to an arrayList
                                }
                            }
                            if(books != null) { // if there are books in the list
                                books = new ArrayList<>(searchBooks);
                                listFragment.updateBookList(books); // update the adapter with the new books returned by the search
                            } else {            // otherwise the list fragment hadn't been create yet so create one
                                books = new ArrayList<>(searchBooks);
                                listFragment = BookListFragment.newInstance(books);
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.list_fragment_container, listFragment)
                                        .commit();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void  onErrorResponse(VolleyError error){
                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
        requestQueue.add(jsonArrayRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isChangingConfigurations()){
            if(serviceConnection != null){
                getApplicationContext().unbindService(serviceConnection);
            }
            return;
        }
        if(playing){
            stop();
        }
        if(bound){
            bound = false;
            getApplicationContext().unbindService(serviceConnection);
            getApplicationContext().stopService(new Intent(getApplicationContext(), AudiobookService.class));
        }
    }

        /**
         * Method implemented from BookSelectorInterface used for inter-fragment communication
         */
    @Override
    public void selectBook(Book book) {
        if(dualPane){
            //Call BookDetailsFragment method displayBook with the data parameter which is the book string title provided by BookListFragment
            currentBook = book;
            detailsFragment.displayBook(book);
        } else {
            currentBook = book;
            portraitDetailsFragment = BookDetailsFragment.newInstance(book);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.list_fragment_container, portraitDetailsFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void play() {
        nowPlaying = currentBook.getId();
        playing = true;
        if(dualPane) {
            detailsFragment.updateButtons();
        } else {
            portraitDetailsFragment.updateButtons();
        }
        mediaControlBinder.play(nowPlaying);
    }

    @Override
    public void pause() {
        paused = true;
        if(dualPane) {
            detailsFragment.updateButtons();
        } else {
            portraitDetailsFragment.updateButtons();
        }
        mediaControlBinder.pause();
    }

    @Override
    public void stop() {
        mediaControlBinder.stop();
        nowPlaying = -1;
        currentProgress = 0;
        lastProgress = 0;
        playing = false;
        paused = false;
        if(dualPane) {
            detailsFragment.updateButtons();
        } else {
            portraitDetailsFragment.updateButtons();
        }
    }

    @Override
    public void unpause() {
        paused = false;
        if(dualPane) {
            detailsFragment.updateButtons();
        } else {
            portraitDetailsFragment.updateButtons();
        }
        mediaControlBinder.pause();
    }

    @Override
    public boolean getPlayStatus() {
        return this.playing;
    }

    @Override
    public boolean getPauseStatus() {
        return this.paused;
    }

    @Override
    public int getProgress(int id) {
        return this.currentProgress;
    }

    @Override
    public void seekProgress(int progress) {
        currentProgress  = progress;
        lastProgress = progress;
        mediaControlBinder.seekTo(progress);
    }

    @Override
    public int getNowPlayingId(){
        if(!playing){
            return -1;
        } else {
            return nowPlaying;
        }
    }

    @Override
    public Book getNowPlaying(){
        return currentBook;
    }

    private void acquireMainConnection() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mediaControlBinder =(AudiobookService.MediaControlBinder) service;
                mediaControlBinder.setProgressHandler(new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        int newProgress = msg.what;
                        int duration = currentBook.getDuration();

                        if(nowPlaying <= 0 || currentProgress < 0){
                            return false;
                        }
                        if(newProgress < (lastProgress - 1)){
                            if(newProgress == 0){
                                stop();
                                return false;
                            }
                        }
                        if (paused) {
                            mediaControlBinder.pause();
                            return false;
                        }
                        if(newProgress > duration){
                            stop();
                            return false;
                        }
                        currentProgress = newProgress;
                        if(dualPane){
                            detailsFragment.updateButtons();
                        } else {
                            portraitDetailsFragment.updateButtons();
                        }

                        if(currentProgress >= duration){
                            stop();
                        } else {
                            lastProgress = newProgress;
                        }

                        return false;
                    }
                }));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        Intent intent = new Intent(getApplicationContext(), AudiobookService.class);
        getApplicationContext().bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void acquireConnection(){
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                bound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        Intent intent = new Intent(getApplicationContext(), AudiobookService.class);
        getApplicationContext().bindService(intent, connection, BIND_AUTO_CREATE);
    }
}
