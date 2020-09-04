package jp.gr.java_conf.datingapp.utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeConverter {
    public static String getSentTime(long time_stamp) {
        // 日付
        Date date = new Date(time_stamp);
        Date sentDate = new Date(time_stamp);
        java.sql.Date theDay = new java.sql.Date(time_stamp);
        java.sql.Date today = new java.sql.Date(new Date().getTime());
        java.sql.Date yesterday = new java.sql.Date(new Date().getTime() - (1000 * 60 * 60 * 24));
        Calendar calendarNow = Calendar.getInstance();
        calendarNow.setTime(new Date());
        int thisYear = calendarNow.get(Calendar.YEAR);
        Calendar calendarThen = Calendar.getInstance();
        calendarThen.setTime(new Date(time_stamp));
        int then = calendarThen.get(Calendar.YEAR);

        if (theDay.toString().equals(today.toString())) {
            return new SimpleDateFormat("H:mm").format(date);
        } else if (theDay.toString().equals(yesterday.toString())) {
            return "昨日";
        } else if (thisYear == then){
            return new SimpleDateFormat("M/d").format(sentDate);
        } else {
            return new SimpleDateFormat("yyyy/M/d").format(sentDate);
        }
    }
}
