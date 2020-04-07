package edu.temple.bookshelf;

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
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookSelectorInterface {

    boolean dualPane;   //Boolean used to say if there are two fragments on the screen or not
    private ArrayList<Book> books;        //currently displayed books

    private static final String BOOKS_KEY = "_books";
    private static final String CURRENT_BOOK_KEY = "_currentBook";

    private BookListFragment listFragment;
    private BookDetailsFragment detailsFragment;

    private Book currentBook;
    private String booksUrl = "https://kamorris.com/lab/abp/booksearch.php?search=";

    RequestQueue requestQueue;
    EditText searchEditText;

    private String configTag;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);
        searchEditText = findViewById(R.id.searchText);

        // check if Activity has been loaded before
        if(savedInstanceState != null) {    // if so, set book data to previously loaded data

            books = savedInstanceState.getParcelableArrayList(BOOKS_KEY);
            currentBook = savedInstanceState.getParcelable(CURRENT_BOOK_KEY);

            listFragment = BookListFragment.newInstance(books);
            detailsFragment = BookDetailsFragment.newInstance(currentBook);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.list_fragment_container, listFragment)
                    .commit();

            //Determine if one or two fragments are visible (one if portrait mode on a smaller phone, two if in landscape mode or on a larger device)
            //If the BookDetailsFragment is visible (not null) we are in landscape mode or on a larger device
            dualPane = (findViewById(R.id.details_fragment_container) != null);

            //FragmentManager fragmentManager = getSupportFragmentManager();
            //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            //Check if our boolean is true (landscape or portrait) or false (small screen portrait)
            if(dualPane){   //If it is true, load book list and details fragments
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.details_fragment_container, detailsFragment)
                        .commit();
            } else {
                if(currentBook != null){
                    BookDetailsFragment portraitDetailsFragment = BookDetailsFragment.newInstance(currentBook);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.list_fragment_container, portraitDetailsFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }

//        } else {
//            listFragment = new BookListFragment();
//            detailsFragment = new BookDetailsFragment();
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.list_fragment_container, listFragment)
//                    .commit();
//
//            //Determine if one or two fragments are visible (one if portrait mode on a smaller phone, two if in landscape mode or on a larger device)
//            //If the BookDetailsFragment is visible (not null) we are in landscape mode or on a larger device
//            dualPane = (findViewById(R.id.details_fragment_container) != null);
//
//            //FragmentManager fragmentManager = getSupportFragmentManager();
//            //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            //Check if our boolean is true (landscape or portrait) or false (small screen portrait)
//            if(dualPane){   //If it is true, load book list and details fragments
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.details_fragment_container, detailsFragment)
//                        .commit();
//            }
        }

        findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Log.d("TESTTESTTEST", "onClick: search clicked");
                String searchUrl = booksUrl + searchEditText.getText().toString();
                searchBooks(searchUrl);
                Log.d("TESTTESTTEST", "onClick: display frag disaplyed?" +  (detailsFragment != null));
//                configTag = (String) findViewById(R.id.main_activity_view).getTag();
//                if (configTag.equals(getString(R.string.portrait_tag)) && (findViewById(R.id.details_fragment_container) != null)){
//                    getSupportFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.details_fragment_container, listFragment)
//                            .commit();
//                }
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
                            Log.d("TESTTESTTEST", "onResponse: getting books");
                            ArrayList<Book> searchBooks = new ArrayList<>();
                            if(response.length() > 0) {
                                if(books != null){
                                    books.clear();
                                }
                                for(int i = 0; i < response.length(); i++){
                                    searchBooks.add(new Book(response.getJSONObject(i)));
                                }
                                books = new ArrayList<>(searchBooks);
                                Log.d("TESTTESTTEST", "onResponse: got books:" + books);
                            }

                            if(listFragment != null){
                                listFragment.updateBookList(books);
                            } else {
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
            BookDetailsFragment portraitDetailsFragment = BookDetailsFragment.newInstance(book);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.list_fragment_container, portraitDetailsFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
