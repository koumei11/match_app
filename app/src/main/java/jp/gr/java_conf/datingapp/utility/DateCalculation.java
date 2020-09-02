package jp.gr.java_conf.datingapp.utility;

import java.util.Date;

public class DateCalculation {
    public static long calculate(long mill) {
        Date now = new Date();
        return (now.getTime() - mill) / 1000 / 60 / 60;
    }
}
