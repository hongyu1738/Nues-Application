package com.example.news;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.news.Models.Articles;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

public class Adapter extends RecyclerView.Adapter<Adapter.Viewholder>{ //Adapter class to display details within a recycler view

    Context context;
    List<Articles> articles;

    public Adapter(Context context, List<Articles> articles) { //Constructor
        this.context = context;
        this.articles = articles;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //Initializes the view holder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        //Binds the view holder to the adapter, pass data to view holder
        final Articles a = articles.get(position);
        holder.tv_title.setText(a.getTitle());
        holder.tv_source.setText(a.getSource().getName());
        holder.tv_publishedat.setText(prettyDate(a.getPublishedAt()));
        holder.tv_author.setText(a.getAuthor());
        holder.tv_desc.setText(a.getDescription());
        holder.tv_time.setText(dateTime(a.getPublishedAt()));

        String imageurl = a.getUrlToImage();
        String url = a.getUrl();

        Picasso.with(context).load(imageurl).into(holder.imageView); //Loads the image of the article into the image view

        holder.cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) { //Send intent and details to Details.java upon clicking of the card view
                Intent intent = new Intent(context, Details.class);
                intent.putExtra("title",a.getTitle());
                intent.putExtra("source",a.getSource().getName());
                intent.putExtra("date",prettyDate(a.getPublishedAt()));
                intent.putExtra("author",a.getAuthor());
                intent.putExtra("desc",a.getDescription());
                intent.putExtra("time",dateTime(a.getPublishedAt()));
                intent.putExtra("imageurl", a.getUrlToImage());
                intent.putExtra("url", a.getUrl());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() { //Returns number of articles in the array list
        return articles.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder{ //Initialises the element within the view holder

        CardView cardView;
        TextView tv_title, tv_source, tv_desc, tv_author, tv_publishedat, tv_time;
        ImageView imageView;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            tv_title = itemView.findViewById(R.id.title);
            tv_source = itemView.findViewById(R.id.source);
            tv_desc = itemView.findViewById(R.id.desc);
            tv_author = itemView.findViewById(R.id.author);
            tv_publishedat = itemView.findViewById(R.id.publishedAt);
            tv_time = itemView.findViewById(R.id.time);
            imageView = itemView.findViewById(R.id.img);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    public String dateTime(String t){ //To show the time in a readable format
        PrettyTime prettyTime = new PrettyTime(new Locale(getCountry()));
        String time = null;

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:", Locale.ENGLISH);
            Date date = simpleDateFormat.parse(t);
            time = prettyTime.format(date);
        } catch (Exception e){
            e.printStackTrace();
        }
        return time;
    }

    public String prettyDate(String d){ //To show the date in a readable format
        String formattedDate = null;

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:", Locale.ENGLISH);
            Date date = simpleDateFormat.parse(d);
            formattedDate = DateFormat.getDateInstance().format(date);
        } catch (Exception e){
            e.printStackTrace();
        }
       return formattedDate;
    }

    public String getCountry(){ //Retrieve country or region of the locale specified
        Locale locale = Locale.getDefault();
        String country = locale.getCountry();
        return country.toLowerCase();
    }
}
