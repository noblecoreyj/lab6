package edu.calvin.cs262.lab06;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Weather forecast object (POJO), one per day, based on openweathermap's RESTful API.
 * Based on Deitel's WeatherViewer app (chapter 17).
 *
 * @author deitel
 * @author kvlinden
 * @version spring, 2017
 */
public class Weather {

    private String day, summary, low, high;

    public Weather(long dt, String summary, String low, String high) {
        this.day = convertTimeStampToDay(dt);
        this.summary = summary;
        this.low = low;
        this.high = high;
    }

    public String getDay() {
        return day;
    }
    public String getSummary() {
        return summary;
    }
    public String getLow() {return low;}
    public String getHigh() {return high;}

    private static String convertTimeStampToDay(long dt) {
        // Convert from datetime in milliseconds to calendar.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dt * 1000);

        // Adjust time for device's time zone.
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));

        // Format/return the day's name.
        return new SimpleDateFormat("EEEE").format(calendar.getTime());
    }
}
