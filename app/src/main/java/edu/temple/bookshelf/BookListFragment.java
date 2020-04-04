package edu.temple.bookshelf;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A Fragment to display book titles in a ListView
 */
public class BookListFragment extends Fragment {

    private BookSelectorInterface bookSelector;
    private static final String BOOK_LIST_KEY = "_bookList";


    Context parentContext;

    private ArrayList<Book> bookList;
    private BooksAdapter booksAdapter;

    public BookListFragment() {
        // Required empty public constructor
    }

    public static BookListFragment newInstance(ArrayList<Book> books){
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putSerializable(BOOK_LIST_KEY, books);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof BookSelectorInterface){
            parentContext = context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            bookList = (ArrayList<Book>) getArguments().getSerializable(BOOK_LIST_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_book_list, container, false);
        final ListView bookListView = rootView.findViewById(R.id.listView);
        booksAdapter = new BooksAdapter(this.getActivity(), bookList);
        bookListView.setAdapter(booksAdapter);
        bookListView.setOnItemClickListener(new ListView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                ((BookSelectorInterface) parentContext).selectBook(((Book)parent.getItemAtPosition(position)));
            }
        });
        return rootView;
    }

    /**
     * Method that waits for parent activity to load before loading fragment
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);



    }

    @Override
    public void onDetach() {
        super.onDetach();
        parentContext = null;
    }

    public interface BookSelectorInterface {
        void selectBook(Book book);
    }
}
