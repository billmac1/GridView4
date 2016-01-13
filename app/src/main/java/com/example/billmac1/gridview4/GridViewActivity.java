package com.example.billmac1.gridview4;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;



public class GridViewActivity extends AppCompatActivity {
    private static final String TAG = GridViewActivity.class.getSimpleName();
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;

   // private String FEED_URL = "http://javatechig.com/?json=get_recent_posts&count=45";
   final String FORECAST_BASE_URL =
           "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc";
    final String API_KEY = "api_key";

    Uri builder = Uri.parse(FORECAST_BASE_URL).buildUpon()
            .appendQueryParameter(API_KEY, BuildConfig.MOVIE_DB_API_KEY)
            .build();

    private String FEED_URL = builder.toString();




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



        //Start download
        new AsyncHttpTask().execute(FEED_URL);
        mProgressBar.setVisibility(View.VISIBLE);
    //    FetchMovieDataTask movieDataTask = new FetchMovieDataTask();
    //    movieDataTask.execute("");
    }

    //Downloading data asynchronously
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

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

    public class FetchMovieDataTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();


        private String[] getPostersFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "results";
            final String OWM_PATH = "poster_path";
            final String OWM_ID = "id";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWM_LIST);

            int movieArraySize =  movieArray.length();
            String[] resultStrs = new String[ movieArraySize];  //fix later


            for(int i = 0; i < movieArraySize; i++) {
                String postPath;
                // Get the JSON object representing the movie
                JSONObject movieData = movieArray.getJSONObject(i);
                // posterPath is in a child array
                //   JSONObject movieObject = movieData.getJSONArray(movieJsonStr).getJSONObject(0);
                postPath = movieData.getString(OWM_PATH);
                resultStrs[i] = postPath;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Movie jpg: " + s);
            }

            return resultStrs;

        }



        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try{
                // Construct the URL for the MovieDB query
                final String FORECAST_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc";
                final String API_KEY = "api_key";

                Uri builder = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY, BuildConfig.MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builder.toString());

                //   Log.v(LOG_TAG,"Built URI" + builder.toString());

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
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
                //.v(LOG_TAG,"Forecast JSON String: " + forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie link, there's no point in attempting
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
                        //  Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try{
                return getPostersFromJson(movieJsonStr);
            }
            catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;

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
  //      @Override
  //      protected void onPostExecute(String[] result) {
  //          if (result != null) {
         //       mGridAdapter.clear();
          //      mGridAdapter.addAll(result);    //works from KitKat and above
           //     for (String dayForecastStr : result) {
           //             mForecastAdapter.add(dayForecastStr);
           //     }
        //    }
        //}



}
