package com.example.news;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class Details extends AppCompatActivity { //View details of news activity class

    private TextView tv_title, tv_source, tv_author, tv_time, tv_date, tv_desc;
    private WebView webView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        tv_title = findViewById(R.id.title); //Assign a variable for each text view, web view and image view
        tv_author = findViewById(R.id.author);
        tv_date = findViewById(R.id.publishedAt);
        tv_time = findViewById(R.id.time);
        tv_source = findViewById(R.id.source);
        tv_desc = findViewById(R.id.desc);

        webView = findViewById(R.id.webView);
        imageView = findViewById(R.id.img);

        Intent intent = getIntent(); //Get the following details from the Adapter through intent
        String title = intent.getStringExtra("title");
        String author = intent.getStringExtra("author");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
        String source = intent.getStringExtra("source");
        String desc = intent.getStringExtra("desc");
        String imageurl = intent.getStringExtra("imageurl");
        String url = intent.getStringExtra("url");

        tv_title.setText(title); //Show the data acquired within the text views assigned
        tv_author.setText(author);
        tv_date.setText(date);
        tv_time.setText(time);
        tv_source.setText(source);
        tv_desc.setText(desc);

        Picasso.with(Details.this).load(imageurl).into(imageView); //Show the image URL in the image view

        webView = findViewById(R.id.webView); //Show the article in the web view
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);
    }

    public void ShareNews(View view) { //Feature to share news
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plan");
            i.putExtra(Intent.EXTRA_SUBJECT, tv_source.getText());
            String body = tv_title.getText() + "\n" + "\n" + webView.getUrl() + "\n" + "\n" + "Shared from the Nues App." + "\n";
            i.putExtra(Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(i, "Share with: "));
        }catch (Exception e){
            Toast.makeText(this, "Sorry, this article cannot be shared.",Toast.LENGTH_SHORT).show();
        }
    }

    public void viewInBrowser(View view) { //Feature to view news in browser
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(webView.getUrl()));
        startActivity(i);
    }
}