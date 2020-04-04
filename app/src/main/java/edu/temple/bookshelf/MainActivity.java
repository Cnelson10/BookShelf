package edu.temple.bookshelf;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    //private ArrayList<HashMap<String, String>> books;
    private ArrayList<Book> books;        //currently displayed books
    private ArrayList<Book> allBooks;    //all books

    private static final String LIST_FRAGMENT_KEY = "_listFragment";
    private static final String DETAILS_FRAGMENT_KEY = "_detailsFragment";
    private static final String TITLE_KEY = "_title";
    private static final String AUTHOR_KEY = "_author";
    private static final String BOOKS_KEY = "_books";
    private static final String ALL_BOOKS_KEY = "_allBooks";
    private static final String CURRENT_BOOK_KEY = "_currentBook";

    private BookListFragment listFragment;
    private BookDetailsFragment detailsFragment;

    private int currentBookIndex = -1;
    private String booksUrl = "https://kamorris.com/lab/abp/booksearch.php?search=";

    RequestQueue requestQueue;
    EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

//        books = new ArrayList<>();
//        String[] titles = getResources().getStringArray(R.array.book_titles);
//        String[] authors = getResources().getStringArray(R.array.book_authors);
//        for (int i = 0; i < getResources().getStringArray(R.array.book_titles).length; i++){
//            HashMap<String,String> book = new HashMap<>();
//            book.put(TITLE_KEY, titles[i]);
//            book.put(AUTHOR_KEY, authors[i]);
//            books.add(book);
//        }
        searchEditText = findViewById(R.id.searchText);
        // check if Activity has been loaded before
        if(savedInstanceState != null) {    // if so, set book data to previously loaded data

            books = savedInstanceState.getParcelableArrayList(BOOKS_KEY);
            currentBookIndex = savedInstanceState.getInt(CURRENT_BOOK_KEY);

        }

        findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String searchUrl = booksUrl + searchEditText.getText().toString();
                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(searchUrl,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response){
                                try {
                                    ArrayList<Book> searchBooks = new ArrayList<>();
                                    if(response.length() > 0) {
                                        books.clear();
                                        for(int i = 0; i < response.length(); i++){
                                            searchBooks.add(new Book(response.getJSONObject(i)));
                                        }
                                        books = new ArrayList<>(searchBooks);
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
            }
        });
        listFragment = BookListFragment.newInstance(books);
        detailsFragment = BookDetailsFragment.newInstance(books.get(0));

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.list_fragment_container, listFragment, LIST_FRAGMENT_KEY)
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
                    .add(R.id.details_fragment_container, detailsFragment, DETAILS_FRAGMENT_KEY)
                    .commit();
        }
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
    }

    /**
     * Method implemented from BookSelectorInterface used for inter-fragment communication
     */
    @Override
    public void selectBook(Book book) {
        if(dualPane){
            //Call BookDetailsFragment method displayBook with the data parameter which is the book string title provided by BookListFragment
            detailsFragment.displayBook(book);
        } else {
            BookDetailsFragment portraitDetailsFragment = BookDetailsFragment.newInstance(book);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.list_fragment_container, portraitDetailsFragment, LIST_FRAGMENT_KEY)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void downloadAllBooks(){
        this.downloadBooks("");
    }

    private void downloadBooks(final String search) {
        new Thread(){
            public void run() {
                try {

                    ArrayList<Book> searchBooks = new ArrayList<>();
                    URL searchURL = new URL(booksUrl + search);
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(searchURL.openStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String temp;
                    while ((temp = bufferedReader.readLine()) != null){
                        stringBuilder.append(temp);
                    }
                    bufferedReader.close();
                    try {

                        JSONArray booksArray = new JSONArray(stringBuilder.toString());
                        if(booksArray.length() > 0) {
                            books.clear();
                            for(int i = 0; i < booksArray.length(); i++){
                                searchBooks.add(new Book(booksArray.getJSONObject(i)));
                            }
                            books = new ArrayList<>(searchBooks);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
