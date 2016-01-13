package com.example.billmac1.gridview4;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by billmac1 on 1/8/2016.
 */
public class DetailsActivity extends AppCompatActivity {
    private TextView titleTextView;
    private ImageView imageView;
    private TextView overviewTextView;
    private TextView ratingTextView;
    private TextView releaseTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        String title = getIntent().getStringExtra("title");
        String image = getIntent().getStringExtra("image");
        String overview = getIntent().getStringExtra("overview");
        String rating = "User Rating: ";
        rating += getIntent().getStringExtra("rating")+ "/10";
        String release = "Release Date: ";
        release += getIntent().getStringExtra("release_date").substring(0,4);  //just get the year

        titleTextView = (TextView) findViewById(R.id.title);
        imageView = (ImageView) findViewById(R.id.grid_item_image);
        overviewTextView = (TextView) findViewById(R.id.overview);
        ratingTextView = (TextView) findViewById(R.id.rating);
        releaseTextView = (TextView) findViewById(R.id.release);

        titleTextView.setText(Html.fromHtml(title));
        Picasso.with(this).load(image).into(imageView);
        overviewTextView.setText(Html.fromHtml(overview));
        ratingTextView.setText(Html.fromHtml(rating));
        releaseTextView.setText(Html.fromHtml(release));

    }
}
