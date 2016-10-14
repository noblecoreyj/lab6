package edu.calvin.cs262.lab06;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Reads openweathermap's RESTful API for weather forecasts.
 * The code is based on Deitel's WeatherViewer (Chapter 17), simplified based on Murach's NewsReader (Chapter 10).
 * For CS 262, lab 6
 *
 * QUESTIONS
 * 1. The application catches the exception thrown and displays a toast saying that it cannot connect to the API server
 * 2. The API key identifies the calling program for identification purposes and malicious use detection
 * 3. Alrighty, but it's long... {"city":{"id":4994358,"name":"Grand Rapids","coord":{"lon":-85.668091,"lat":42.96336},"country":"US","population":0},"cod":"200","message":0.0154,"cnt":7,"list":[{"dt":1476378000,"temp":{"day":35.26,"min":29.01,"max":35.26,"night":29.39,"eve":35.26,"morn":35.26},"pressure":1009.9,"humidity":75,"weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01n"}],"speed":5.64,"deg":274,"clouds":0},{"dt":1476464400,"temp":{"day":57.58,"min":30.67,"max":59.56,"night":46.44,"eve":51.69,"morn":30.67},"pressure":1009.91,"humidity":75,"weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01d"}],"speed":10.87,"deg":190,"clouds":0},{"dt":1476550800,"temp":{"day":64.99,"min":46.33,"max":65.79,"night":63,"eve":65.17,"morn":46.33},"pressure":1001.21,"humidity":79,"weather":[{"id":502,"main":"Rain","description":"heavy intensity rain","icon":"10d"}],"speed":16.84,"deg":198,"clouds":68,"rain":31.94},{"dt":1476637200,"temp":{"day":69.31,"min":60.84,"max":69.31,"night":63.39,"eve":60.84,"morn":63.59},"pressure":994.36,"humidity":0,"weather":[{"id":502,"main":"Rain","description":"heavy intensity rain","icon":"10d"}],"speed":13.8,"deg":257,"clouds":15,"rain":24.62},{"dt":1476723600,"temp":{"day":66.2,"min":53.69,"max":67.17,"night":53.69,"eve":60.78,"morn":67.17},"pressure":988.98,"humidity":0,"weather":[{"id":502,"main":"Rain","description":"heavy intensity rain","icon":"10d"}],"speed":18.92,"deg":298,"clouds":0,"rain":20.9},{"dt":1476810000,"temp":{"day":63.14,"min":54.43,"max":71.01,"night":59.4,"eve":71.01,"morn":54.43},"pressure":988.17,"humidity":0,"weather":[{"id":501,"main":"Rain","description":"moderate rain","icon":"10d"}],"speed":16.42,"deg":157,"clouds":31,"rain":3.73},{"dt":1476896400,"temp":{"day":57.24,"min":52.45,"max":57.24,"night":52.45,"eve":56.34,"morn":54.88},"pressure":993.07,"humidity":0,"weather":[{"id":500,"main":"Rain","description":"light rain","icon":"10d"}],"speed":22.75,"deg":298,"clouds":80,"rain":1.32}]}
 * 4. The system pulls the JSON data from the URL, converts it into an array list, and pulls out the information it needs and updates the layout accordingly
 * 5. The Weather class provides a Weather object that is used to show the weather forecast - it contains all the parameters that is needed for the forecast
 *
 * @author kvlinden
 * @author cjn8
 * @version summer, 2016
 */
public class MainActivity extends AppCompatActivity {

    private EditText cityText;
    private Button fetchButton;

    private List<Weather> weatherList = new ArrayList<>();
    private ListView itemsListView;

    /* This formater can be used as follows to format temperatures for display.
     *     numberFormat.format(SOME_DOUBLE_VALUE)
     */
    //private NumberFormat numberFormat = NumberFormat.getInstance();

    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityText = (EditText) findViewById(R.id.cityText);
        fetchButton = (Button) findViewById(R.id.fetchButton);
        itemsListView = (ListView) findViewById(R.id.weatherListView);

        // See comments on this formatter above.
        //numberFormat.setMaximumFractionDigits(0);

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard(cityText);
                new GetWeatherTask().execute(createURL(cityText.getText().toString()));
            }
        });
    }

    /**
     * Formats a URL for the webservice specified in the string resources.
     *
     * @param city the target city
     * @return URL formatted for openweathermap.com
     */
    private URL createURL(String city) {
        try {
            String urlString = getString(R.string.web_service_url) +
                    URLEncoder.encode(city, "UTF-8") +
                    "&units=" + getString(R.string.openweather_units) +
                    "&cnt=" + getString(R.string.openweather_count) +
                    "&APPID=" + getString(R.string.openweather_api_key);

            return new URL(urlString);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    /**
     * Deitel's method for programmatically dismissing the keyboard.
     *
     * @param view the TextView currently being edited
     */
    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Inner class for GETing the current weather data from openweathermap.org asynchronously
     */
    private class GetWeatherTask extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;
            StringBuilder result = new StringBuilder();
            try {
                connection = (HttpURLConnection) params[0].openConnection();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    //System.out.println(result.toString());
                    return new JSONObject(result.toString());
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject weather) {
            if (weather != null) {
                //Log.d(TAG, weather.toString());
                convertJSONtoArrayList(weather);
                MainActivity.this.updateDisplay();
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Converts the JSON weather forecast data to an arraylist suitable for a listview adapter
     *
     * @param forecast
     */
    private void convertJSONtoArrayList(JSONObject forecast) {
        weatherList.clear(); // clear old weather data
        try {
            JSONArray list = forecast.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject day = list.getJSONObject(i);
                JSONObject temperatures = day.getJSONObject("temp");
                JSONObject weather = day.getJSONArray("weather").getJSONObject(0);
                weatherList.add(new Weather(
                        day.getLong("dt"),
                        weather.getString("description"),
                        temperatures.getString("min"),
                        temperatures.getString("max")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refresh the weather data on the forecast ListView through a simple adapter
     */
    private void updateDisplay() {
        if (weatherList == null) {
            Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        }
        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        for (Weather item : weatherList) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("day", item.getDay());
            map.put("description", item.getSummary());
            map.put("min", item.getLow());
            map.put("max", item.getHigh());
            data.add(map);
        }

        int resource = R.layout.weather_item;
        String[] from = {"day", "description", "min", "max"};
        int[] to = {R.id.dayTextView, R.id.summaryTextView, R.id.min_text, R.id.max_text};

        SimpleAdapter adapter = new SimpleAdapter(this, data, resource, from, to);
        itemsListView.setAdapter(adapter);
    }

}
