package com.island.island.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by poo on 1/26/16.
 */
public class Utils
{
    public static String smartTimestampFromUnixTime(long unixTime)
    {
        // currentTimeMillis is already in UTC!
        long currentTime = System.currentTimeMillis() / 1000;
        long timeDiff = currentTime - unixTime;

        String timestamp = "";

        // Under 1 minute
        if(timeDiff < 60)
        {
            timestamp = timeDiff + (timeDiff == 1 ? " sec" : " secs");
        }
        // Under one hour
        else if(timeDiff >= 60 && timeDiff < 3600)
        {
            long minutes = timeDiff / 60;
            timestamp = minutes + (minutes == 1 ? " min" : " mins");
        }
        // Under 24 hours
        else if(timeDiff >= 3600 && timeDiff < 86400)
        {
            long hours = timeDiff / 3600;
            timestamp = hours + (hours == 1 ? " hr" : " hrs");
        }
        // Display date of post
        else
        {
            TimeZone timeZone = TimeZone.getDefault();

            SimpleDateFormat dateFormat = new SimpleDateFormat();
            dateFormat.setTimeZone(timeZone);

            dateFormat.applyPattern("MMM d, yyyy");
            timestamp = dateFormat.format(new Date(unixTime * 1000));
        }

        return timestamp;
    }
}
