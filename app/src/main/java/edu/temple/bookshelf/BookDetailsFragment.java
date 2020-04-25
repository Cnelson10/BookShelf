package edu.temple.bookshelf;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A fragment to display the title of the book clicked on in the BookDetailsFragment
 */
public class BookDetailsFragment extends Fragment {

    private static final String CURRENT_BOOK_KEY = "_currentBook";
    private static final String PLAY_KEY = "_playing";
    private static final String PAUSE_KEY = "_paused";

    private Book book;
    Context parentContext;

    TextView title_text;
    TextView author_text;
    ImageView cover_img;

    private boolean playing;
    private boolean paused;

    ImageButton pauseButton;
    Button playButton;
    Button stopButton;

    PlayStatusInterface parent;

    public BookDetailsFragment() {
        // Required empty public constructor
    }

    public static BookDetailsFragment newInstance(Book book){
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(CURRENT_BOOK_KEY, book);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        parentContext = context;
        super.onAttach(parentContext);

        if(parentContext instanceof PlayStatusInterface) {
            parent = (PlayStatusInterface) parentContext;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = getArguments().getParcelable(CURRENT_BOOK_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(savedInstanceState != null){
            book = savedInstanceState.getParcelable(CURRENT_BOOK_KEY);
            playing = savedInstanceState.getBoolean(PLAY_KEY);
            paused = savedInstanceState.getBoolean(PAUSE_KEY);
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_book_details, container, false);
        title_text = rootView.findViewById(R.id.book_details_title);
        author_text = rootView.findViewById(R.id.book_details_author);
        cover_img = rootView.findViewById(R.id.book_details_cover);
        pauseButton = rootView.findViewById(R.id.pause_button);
        playButton = rootView.findViewById(R.id.play_button);
        stopButton = rootView.findViewById(R.id.stop_button);

        this.setButtonListeners();

        if(book != null) {
            displayBook(book);
        }
        displayButtons();
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
    public void displayBook(Book book){
        title_text.setText(book.getTitle());      //Change the text in BookDetailsFragment to the item clicked in the BookListFragment ListView
        author_text.setText(book.getAuthor());    //Change the text in BookDetailsFragment to the item clicked in the BookListFragment ListView
        Picasso.get().load(book.getCoverURL()).into(cover_img);
    }

    public void displayButtons(){
        if(parent.getPlayStatus()){
            this.playing = true;
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.VISIBLE);
        } else {
            this.playing = false;
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.GONE);
        }
        if(parent.getPauseStatus()) {
            this.paused = true;
            pauseButton.setImageResource(R.drawable.unpause);
            pauseButton.setBackgroundColor(ContextCompat.getColor(parentContext, R.color.colorWhite));
        } else {
            this.paused = false;
            pauseButton.setImageResource(R.drawable.pause);
            pauseButton.setBackgroundColor(ContextCompat.getColor(parentContext, R.color.colorBlack));
        }
    }

    private void setButtonListeners(){
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.play();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!paused){
                    parent.pause();
                } else {
                    parent.resume();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.stop();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(CURRENT_BOOK_KEY, book);
        outState.putBoolean(PLAY_KEY, playing);
        outState.putBoolean(PAUSE_KEY, paused);
    }

    public interface PlayStatusInterface {
        void play();
        void pause();
        void stop();
        void resume();
        boolean getPlayStatus();
        boolean getPauseStatus();

    }
}