package jp.gr.java_conf.datingapp.utility;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeConverter {
    public static String getSentTime(Timestamp time_stamp) {
        // 日付

        Date now = new Date();
        long mill = now.getTime() - time_stamp.getSeconds() * 1000;
        long second = mill / 1000;
        if (second / 60 < 60) {
            return second / 60 + "分前";
        } else if (second / 60 / 60 < 24) {
            return second / 60 / 60 + "時間前";
        } else {
            Date sentDate = new Date(time_stamp.getSeconds() * 1000);
            return new SimpleDateFormat("yyyy/MM/dd").format(sentDate);
        }
    }
}
