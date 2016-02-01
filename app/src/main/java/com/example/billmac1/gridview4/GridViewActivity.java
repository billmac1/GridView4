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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;



public class GridViewActivity extends AppCompatActivity {
    private static final String TAG = GridViewActivity.class.getSimpleName();
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    String sortChoice1;

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

        updateMovieGrid();

    }


    private void updateMovieGrid(){
        mGridAdapter.clear();
   //     String pref_lang ="en";
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sortChoice1 = sharedPrefs.getString(
                getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));
        final String FORECAST_BASE_URL =
                "http://api.themoviedb.org/3/discover/movie?sort_by=" + sortChoice1 + ".desc";
        final String API_KEY = "api_key";
 //       final String LANGUAGE = "language";   //could be a setting

        Uri builder = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(API_KEY, BuildConfig.MOVIE_DB_API_KEY)
 //               .appendQueryParameter(LANGUAGE,pref_lang)
                .build();

        final String FEED_URL = builder.toString();

        new GetMovieData().execute(FEED_URL);
    }


    //Downloading data asynchronously
    public class GetMovieData extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = GetMovieData.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {

                URL url = new URL( params[0]);
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    line += "\n";
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        private String[] getMovieDataFromJson(String movieJsonStr)
                throws JSONException {
            GridItem item;
            String poster;
          //  String[] result = null;

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "results";
            final String OWM_TITLE = "original_title";
            final String OWM_POSTER = "poster_path";
            final String OWM_RATING = "vote_average";
            final String OWM_RELEASE = "release_date";
            final String OWM_OVERVIEW = "overview";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWM_LIST);

            String[] result = new String[movieArray.length()];

            for(int i = 0; i < movieArray.length(); i++) {
                    // Get the JSON object
                JSONObject movieData = movieArray.getJSONObject(i);

                item = new GridItem();

                String title = movieData.optString(OWM_TITLE);
                item.setTitle(title);

                String overview = movieData.optString(OWM_OVERVIEW);
                item.setOverview(overview);

                String poster_url =  "http://image.tmdb.org/t/p/w185";
                poster = movieData.optString(OWM_POSTER );
                if (!poster.equals("null")) {
                    item.setImage(poster_url + poster);
                }
                else{
                      item.setImage("R.drawable.no_image");
                }


                String rating = movieData.optString(OWM_RATING);
                item.setRating(rating);

                String release = movieData.optString(OWM_RELEASE);
                item.setReleaseDate(release);

                mGridData.add(item);
                result[i] = poster;   //make result not null
            }
            return result;
        }

        @Override
        protected void onPostExecute(String[] result) {
            // Download complete. Let us update UI
            if (result != null) {
              mGridAdapter.setGridData(mGridData);
            } else {
                Toast.makeText(GridViewActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
            mProgressBar.setVisibility(View.GONE);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.preferences:
            {
                Intent intent = new Intent();
                intent.setClassName(this, "com.example.billmac1.gridview4.MyPreferenceActivity");
                startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        //there must be a better way to catch a preference change!!!
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sortChoice2 = sharedPrefs.getString(
                getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));
        if (!sortChoice1.equals(sortChoice2)) {
            mGridAdapter.clear();  // Should only run these lines if the preference changed
            updateMovieGrid();
        }
    }





}
