package edu.temple.bookshelf;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class BooksAdapter extends BaseAdapter {

    Context context;
    ArrayList<Book> books;

    public BooksAdapter (Context context, ArrayList books) {
        this.context = context;
        this.books = books;
    }

    @Override
    public int getCount() {
        return books.size();
    }

    @Override
    public Object getItem(int position) {
        return books.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView titleTextView, authorTextView;

        if (!(convertView instanceof LinearLayout)) {
            /*
            Inflate a predefined layout file that includes 2 text views.
            We could do this in code, but this seems a little easier
             */
            convertView = LayoutInflater.from(context).inflate(R.layout.book_item, parent, false);
        }

        titleTextView = convertView.findViewById(R.id.book_list_title);
        authorTextView = convertView.findViewById(R.id.book_list_author);

        titleTextView.setText(((Book) getItem(position)).getTitle());
        authorTextView.setText(((Book) getItem(position)).getAuthor());

        return convertView;
    }

    public void clearAll() {
        if(books != null){
            //Log.d("TESTESTEST", "updateBookList: clearing previous booklist");
            books.clear();
            notifyDataSetChanged(); // after clearing out the books notify that the adapter has been updated
        }
    }

    public void updateBookList(ArrayList<Book> books){
        //Log.d("TESTESTEST", "updateBookList: adding new booklist to existing adapter");
        this.books = books;
        notifyDataSetChanged(); // after adding new book items to the adapter notify that the data set has been updated
    }
}
