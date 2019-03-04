package mg.studio.weatherappdesign;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;




public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnClick(null);
    }

    public void btnClick(View view) {

        //Check whether the network is available.
        ConnectivityManager connectivityManager = (ConnectivityManager)this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if(info != null && info.isAvailable())
            new DownloadUpdate().execute();
        else
            Toast.makeText(this, "No network available", Toast.LENGTH_SHORT).show();

    }


    private class DownloadUpdate extends AsyncTask<String, Void, String> {
        protected JSONArray arrayList;


        @Override
        protected String doInBackground(String... strings) {
            String stringUrl = "http://api.openweathermap.org/data/2.5/forecast?q=Chongqing,cn&mode=json&APPID=aa3d744dc145ef9d350be4a80b16ecab";
            HttpURLConnection urlConnection = null;
            BufferedReader reader;

            try {
                URL url = new URL(stringUrl);

                // Create the request to get the information from the server, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Mainly needed for debugging
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                //The temperature
                //return buffer.toString();

                try {
                    JSONObject root = new JSONObject(buffer.toString());
                    String list = root.optString("list").toString();

                    //change list to JSONArray in order to get a value in the Array
                    arrayList = new JSONArray(list);
                    JSONObject today = arrayList.getJSONObject(0);
                    String main = today.optString("main").toString();
                    JSONObject temp = new JSONObject(main);
                    String result = temp.optString("temp").toString();
                    Double tempData = Double.parseDouble(result);
                    tempData = tempData - 273.15;
                    result = String.valueOf(tempData.intValue());

                    return result;
                } catch (JSONException e) {
                    Log.i("异常:", e.toString());
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String temperature) {
            //Update the temperature displayed
            ((TextView) findViewById(R.id.temperature_of_the_day)).setText(temperature);

            Toast.makeText(MainActivity.this, "Weather updated", Toast.LENGTH_SHORT).show();

            updateDateAndLocation();

            //Circularly updating all icons and text.
            for (int i = 0; i <= 4; i++) {
                updateIconByDay(arrayList, i);
                updateDayOfWeek(i);
            }

        }

    }

    protected void updateIconByDay(JSONArray jsonArray, int index){
            try{
                //Parse json file.
                JSONObject thisDay = jsonArray.getJSONObject(index * 8);
                String weather = thisDay.optString("weather").toString();
                JSONArray weatherArray = new JSONArray(weather);
                JSONObject weatherMain = weatherArray.getJSONObject(0);
                String result = weatherMain.optString("main");

                ImageView thisView;

                //Assign target image view ID to change icon according to image view indexes.
                switch(index)
                {
                    case 0:
                        thisView = findViewById(R.id.weather_condition_0);
                        break;
                    case 1:
                        thisView = findViewById(R.id.weather_condition_1);
                        break;
                    case 2:
                        thisView = findViewById(R.id.weather_condition_2);
                        break;
                    case 3:
                        thisView = findViewById(R.id.weather_condition_3);
                        break;
                    case 4:
                        thisView = findViewById(R.id.weather_condition_4);
                        break;
                    default:
                        thisView = findViewById(R.id.weather_condition_0);
                }

                //Change icon according to weather condition.
                switch(result)
                {
                    case"Rain":
                        thisView.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.rainy_small));
                        break;
                    case"Clouds":
                        thisView.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.partly_sunny_small));
                        break;
                    case"Clear":
                        thisView.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.sunny_small));
                        break;
                    default:
                }


            }catch (JSONException e){
                Log.i("Exception:",e.toString());
            }

        }

    protected void updateDayOfWeek(int index){
        //Get corresponding day of week according to date.
        String dayNamesMain[] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String dayNamesShort[] = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        String dayName;
        Calendar calInstance = Calendar.getInstance();
        Date dateInstance = new Date();
        calInstance.setTime(dateInstance);
        int dayOfWeek = calInstance.get(Calendar.DAY_OF_WEEK) - 1;
        if(dayOfWeek < 0)
            dayOfWeek = 0;
        if(index == 0)
            dayName = dayNamesMain[dayOfWeek];
        else dayName = dayNamesShort[(dayOfWeek + index) % 7];

        TextView thisView;

        //Assign target text view ID to change day of week text.
        switch(index)
        {
            case 0:
                thisView = (TextView)findViewById(R.id.day_of_week_0);
                break;
            case 1:
                thisView = (TextView)findViewById(R.id.day_of_week_1);
                break;
            case 2:
                thisView = (TextView)findViewById(R.id.day_of_week_2);
                break;
            case 3:
                thisView = (TextView)findViewById(R.id.day_of_week_3);
                break;
            case 4:
                thisView = (TextView)findViewById(R.id.day_of_week_4);
                break;
            default:
                thisView = (TextView)findViewById(R.id.day_of_week_0);
        }

        thisView.setText(dayName);
    }

    protected void updateDateAndLocation(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date currentDate = new Date();
        String dateString = dateFormat.format(currentDate);
        ((TextView) findViewById(R.id.tv_date)).setText(dateString);
        ((TextView) findViewById(R.id.tv_location)).setText("Chongqing");
    }

}
