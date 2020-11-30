package com.example.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.news.Models.Articles;
import com.example.news.Models.Headlines;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AutoCompleteTextView mEditText;
    private NestedScrollView mNestedScrollView;
    private Button mButton;

    long maxId = 0;
    Adapter mAdapter;
    List<Articles> mArticlesList = new ArrayList<>();
    DatabaseReference reff;
    Query query;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            checkFields();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        mRecyclerView = findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(MainActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setNestedScrollingEnabled(false);

        mEditText = findViewById(R.id.atv_query);
        mButton = findViewById(R.id.button_clear);
        mNestedScrollView = findViewById(R.id.scroll_view);

        query = new Query();
        reff = FirebaseDatabase.getInstance().getReference().child("Query"); //Childing the database

        reff.addValueEventListener(new ValueEventListener() { //Retrieving data from database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final List<String> queries = new ArrayList<String>(); //Creating a new array list

                if(dataSnapshot.exists()){
                    maxId = dataSnapshot.getChildrenCount(); //Get total children count of the database of path Query
                    long startLong  = maxId-5; //Set the start of recent searches

                    for (DataSnapshot suggestionSnapshot : dataSnapshot.getChildren()){
                        long currentLong = Long.parseLong(suggestionSnapshot.getKey()); //Sets the end of recent searches
                        if (currentLong > startLong){ //Sets the autocompletetextview to recommend past 5 searches
                            String suggestion = suggestionSnapshot.child("query").getValue(String.class);
                            queries.add(suggestion);
                        }else{
                            continue;
                        }
                    }

                    Collections.reverse(queries); //Reverses the order of the arraylist so that recent searches appear on top
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            MainActivity.this, android.R.layout.simple_list_item_1,queries);
                    mEditText.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final String country = getCountry();
        retrieveJson("", country, API_KEY); //Retrieve information from API

        mNestedScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            //Remove keyboard and clear focus from autocompletetextview upon scroll
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                InputMethodManager imm = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                mEditText.clearFocus();
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            //Enables screen refresh through swipe gesture
            @Override
            public void onRefresh() {
                InputMethodManager imm = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                mEditText.clearFocus();
                retrieveJson("", country, API_KEY);
            }
        });


        mEditText.addTextChangedListener(mTextWatcher); //Look for changes in autocompletetextview

        checkFields(); //Check for characters in the autocompletetextview

        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            //Show dropdown only when autocompletetextview has focus
            @Override
            public void onFocusChange(View view, boolean b) {
                if (hasWindowFocus()){
                    mEditText.showDropDown();
                }
            }
        });

        mEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //Fires when an item of the autocompletetextview is clicked on
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                retrieveJson(mEditText.getText().toString(), country, API_KEY);
                InputMethodManager imm = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                mEditText.clearFocus();
            }
        });

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override //Users can use the search button located on the soft keyboard for search purposes
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    if (mEditText.getText().toString().equals("")){
                        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                retrieveJson("", country, API_KEY);
                            }
                        });
                        retrieveJson("", country, API_KEY);
                    }else{
                        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                retrieveJson(mEditText.getText().toString(), country, API_KEY);
                            }
                        });
                        retrieveJson(mEditText.getText().toString(), country, API_KEY);
                    }

                    query.setQuery(mEditText.getText().toString());

                    if (query.getQuery().equals("")){
                        InputMethodManager imm = (InputMethodManager) mEditText.getContext().
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                        //Hides the keyboard when input is finished
                        mEditText.clearFocus();
                        //Clears focus from autocompletetextview when input is finished/search is done
                    }else{
                        reff.child(String.valueOf(maxId+1)).setValue(query);
                        InputMethodManager imm = (InputMethodManager) mEditText.getContext().
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                        //Hides the keyboard when input is finished
                        mEditText.clearFocus();
                        //Clears focus from autocompletetextview when input is finished/search is done
                    }
                    return true;
                }else{
                    return false;
                }
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() { //Fires when the search button is clicked (search icon)
            @Override
            public void onClick(View view) {
                mEditText.getText().clear(); //Clear the text on the autocompletetextview on click
            }
        });
    }

    private void checkFields(){ //Check if autocompletetextview has any characters
        Button b = findViewById(R.id.button_clear);

        String s1 = mEditText.getText().toString();

        if (s1.equals("")){
            b.setVisibility(View.INVISIBLE); //Remove button visibility if autocompletetextview has no characters
        }else{
            b.setVisibility(View.VISIBLE); //Show button if autocompletetextview has any characters
        }
    }

    public String getCountry(){ //Get user country
        Locale locale = Locale.getDefault();
        String country = locale.getCountry();
        return country.toLowerCase();
    }

    public void retrieveJson(String query, String country, String API_KEY){ //Retrieve news from API
        Call<Headlines> call;

        if (!mEditText.getText().toString().equals("")){ //Retrieve news without keywords
            call = ApiClient.getInstance().getApi().getQuery(query,API_KEY);
        }else{ //Retrieve news with specific keywords
            call = ApiClient.getInstance().getApi().getHeadlines(country,API_KEY);
        }

        call.enqueue(new Callback<Headlines>(){ //Enqueue results to an array list
            @Override
            public void onResponse(Call<Headlines> call, Response<Headlines> response) {
                if (response.isSuccessful() && response.body().getArticles() != null) {
                    mArticlesList.clear();
                    mArticlesList = response.body().getArticles();
                    mAdapter = new Adapter(MainActivity.this,mArticlesList);
                    mRecyclerView.setAdapter(mAdapter);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<Headlines> call, Throwable t) { //Error message on failure
                Toast.makeText(MainActivity.this, "No Result!", Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void hideKeyboard(View view) { //Hide keyboard and clear focus of autocompletetextview
        InputMethodManager imm = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        mEditText.clearFocus();
    }
}