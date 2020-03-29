package edu.temple.bookshelf;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookSelectorInterface {

    boolean dualPane;   //Boolean used to say if there are two fragments on the screen or not
    private ArrayList<HashMap<String, String>> books;

    private static final String LIST_FRAGMENT_KEY = "_listFragment";
    private static final String DETAILS_FRAGMENT_KEY = "_detailsFragment";
    private static final String TITLE_KEY = "_title";
    private static final String AUTHOR_KEY = "_author";

    private BookListFragment listFragment;
    private BookDetailsFragment detailsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_main);

        books = new ArrayList<>();
        String[] titles = getResources().getStringArray(R.array.book_titles);
        String[] authors = getResources().getStringArray(R.array.book_authors);
        for (int i = 0; i < getResources().getStringArray(R.array.book_titles).length; i++){
            HashMap<String,String> book = new HashMap<>();
            book.put(TITLE_KEY, titles[i]);
            book.put(AUTHOR_KEY, authors[i]);
            books.add(book);
        }
        listFragment = BookListFragment.newInstance(books);
        detailsFragment = BookDetailsFragment.newInstance(books.get(0));

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.list_fragment_container, listFragment, LIST_FRAGMENT_KEY)
                .commit();

        //Determine if one or two fragments are visible (one if portrait mode on a smaller phone, two if in landscape mode or on a larger device)
        //If the BookListFragment is visible (not null) we are in landscape mode or on a larger device
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
    public void selectBook(HashMap<String, String> book) {
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
}
