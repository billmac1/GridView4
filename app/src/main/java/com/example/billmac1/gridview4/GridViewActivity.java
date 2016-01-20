package com.example.billmac1.gridview4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;



public class GridViewActivity extends AppCompatActivity {
    private static final String TAG = GridViewActivity.class.getSimpleName();
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    private String sortChoice = "popularity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        mGridView = (GridView) findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Initialize with empty data

        mGridData = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);

        //Grid view click event
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Get item at position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                Intent intent = new Intent(GridViewActivity.this, DetailsActivity.class);
                ImageView imageView = (ImageView) v.findViewById(R.id.grid_item_image);

                // Interesting data to pass across are the thumbnail size/location, the
                // resourceId of the source bitmap, the picture description, and the
                // orientation (to avoid returning back to an obsolete configuration if
                // the device rotates again in the meantime)

                int[] screenLocation = new int[2];
                imageView.getLocationOnScreen(screenLocation);

                //Pass the image title and url to DetailsActivity
                intent.putExtra("left", screenLocation[0]).
                        putExtra("top", screenLocation[1]).
                        putExtra("width", imageView.getWidth()).
                        putExtra("height", imageView.getHeight()).
                        putExtra("title", item.getTitle()).
                        putExtra("image", item.getImage()).
                        putExtra("rating", item.getRating()).
                        putExtra("release_date", item.getReleaseDate()).
                        putExtra("overview", item.getOverview());

                //Start details activity
                startActivity(intent);
            }
        });

     //   SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
     //   String sortChoice = sharedPrefs.getString(
     //           getString(R.string.pref_sort_key),
     //           getString(R.string.pref_sort_default));


     //   final String FORECAST_BASE_URL =
    //            "http://api.themoviedb.org/3/discover/movie?sort_by=" + sortChoice + ".desc";
    //   final String API_KEY = "api_key";

     //   Uri builder = Uri.parse(FORECAST_BASE_URL).buildUpon()
     //           .appendQueryParameter(API_KEY, BuildConfig.MOVIE_DB_API_KEY)
     //           .build();

     //   final String FEED_URL = builder.toString();

          //Start download
     //   new GetMovieData().execute(FEED_URL);
     //   mProgressBar.setVisibility(View.VISIBLE);
        updateMovieGrid();

    }


    private void updateMovieGrid(){
        mGridAdapter.clear();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sortChoice = sharedPrefs.getString(
                getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));
        final String FORECAST_BASE_URL =
                "http://api.themoviedb.org/3/discover/movie?sort_by=" + sortChoice + ".desc";
        final String API_KEY = "api_key";

        Uri builder = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(API_KEY, BuildConfig.MOVIE_DB_API_KEY)
                .build();

        final String FEED_URL = builder.toString();

        //Start download
        new GetMovieData().execute(FEED_URL);
    }

 //   @Override
 //   public void onStart(){
 //       super.onStart();
 //       updateMovieGrid();
 //   }


    //Downloading data asynchronously
    public class GetMovieData extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            try {
                // Create Apache HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse httpResponse = httpclient.execute(new HttpGet(params[0]));
                int statusCode = httpResponse.getStatusLine().getStatusCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                    String response = streamToString(httpResponse.getEntity().getContent());
                    parseResult(response);
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // Download complete. Let us update UI
            if (result == 1) {
                mGridAdapter.setGridData(mGridData);
            } else {
                Toast.makeText(GridViewActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
            mProgressBar.setVisibility(View.GONE);
        }
    }

    String streamToString(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        // Close stream
        if (null != stream) {
            stream.close();
        }
        return result;
    }

    /**
     * Parsing the feed results and get the list
     * @param result
     */
    private void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            JSONArray posts = response.optJSONArray("results");
            GridItem item;
            String poster;
            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.optJSONObject(i);

                String title = post.optString("original_title");
                item = new GridItem();
                item.setTitle(title);

                String overview = post.optString("overview");
                item.setOverview(overview);

        //        JSONArray attachments = post.getJSONArray("attachments");
        //        if (null != attachments && attachments.length() > 0) {
         //          JSONObject attachment = attachments.getJSONObject(0);
        //           if (attachment != null)
                    poster =  "http://image.tmdb.org/t/p/w185";
                    poster += post.optString("poster_path");
                item.setImage(poster);

                String rating = post.optString("vote_average");
                item.setRating(rating);

                String release = post.optString("release_date");
                item.setReleaseDate(release);

       //        }
                mGridData.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort_options, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle item selection
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.sort_options) {
            startActivity(new Intent(this, SettingsActivity.class));
        //    newSort();   none of these are needed here???
        //    updateMovieGrid();
        //    recreate();
            return true;
        }
        if (id == R.id.help) {
          //  openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }


    private void newSort(){

      //  SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    //    String sortChoice = sharedPrefs.getString(
       //         getString(R.string.pref_sort_key),
       //         getString(R.string.pref_sort_default));

        final String FORECAST_BASE_URL =
                "http://api.themoviedb.org/3/discover/movie?sort_by=" + sortChoice + ".desc";
        final String API_KEY = "api_key";

        Uri builder = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(API_KEY, BuildConfig.MOVIE_DB_API_KEY)
                .build();

        final String FEED_URL = builder.toString();

        //Start download
        new GetMovieData().execute(FEED_URL);


    }


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        updateMovieGrid();
    }





}
