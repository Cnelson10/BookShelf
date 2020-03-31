package edu.temple.bookshelf;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;


/**
 * A fragment to display the title of the book clicked on in the BookDetailsFragment
 */
public class BookDetailsFragment extends Fragment {

    private static final String BOOK_KEY = "_bookId";
    private static final String TITLE_KEY = "_title";
    private static final String AUTHOR_KEY = "_author";

    private HashMap<String, String> book;
    //private Book book;
    Context parent;

    TextView title_text;
    TextView author_text;

    public BookDetailsFragment() {
        // Required empty public constructor
    }

    public static BookDetailsFragment newInstance(HashMap<String, String> book){
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(BOOK_KEY, book);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        parent = context;
        super.onAttach(parent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = (HashMap<String,String>) getArguments().getSerializable(BOOK_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_book_details, container, false);
        title_text = rootView.findViewById(R.id.book_details_title);
        author_text = rootView.findViewById(R.id.book_details_author);
        title_text.setText(book.get(TITLE_KEY));
        author_text.setText(book.get(AUTHOR_KEY));
        return rootView;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        parent = null;
    }

    /**
     * Create a method that will be called by the MainActivity and the parameter wil be data from the BookListFragment
     */
    public void displayBook(HashMap<String, String> book){
        title_text.setText(book.get(TITLE_KEY));      //Change the text in BookDetailsFragment to the item clicked in the BookListFragment ListView
        author_text.setText(book.get(AUTHOR_KEY));    //Change the text in BookDetailsFragment to the item clicked in the BookListFragment ListView
    }
}
