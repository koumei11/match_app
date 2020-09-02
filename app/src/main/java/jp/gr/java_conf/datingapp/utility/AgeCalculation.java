package jp.gr.java_conf.datingapp.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AgeCalculation {
    public static int calculate(String birthDateString) throws ParseException {
        SimpleDateFormat sdFormat1 = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sdFormat2 = new SimpleDateFormat("yyyyMMdd");
        Date now = new Date();
        Date birthDayDate = sdFormat1.parse(birthDateString);
        if (birthDayDate != null) {
            return (Integer.parseInt(sdFormat2.format(now)) - Integer.parseInt(sdFormat2.format(birthDayDate))) / 10000;
        }
        return 0;
    }
}
