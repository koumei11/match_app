package jp.gr.java_conf.datingapp.model;


import com.google.firebase.Timestamp;

public class Chat {
    private String from;
    private String to;
    private String message;
    private String my_img;
    private Timestamp time_stamp;
    private Profile profile;
    private boolean firstMessageOfTheDay;

    public Chat() {

    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMy_img() {
        return my_img;
    }

    public void setMy_img(String my_img) {
        this.my_img = my_img;
    }

    public Timestamp getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(Timestamp time_stamp) {
        this.time_stamp = time_stamp;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public boolean isFirstMessageOfTheDay() {
        return firstMessageOfTheDay;
    }

    public void setFirstMessageOfTheDay(boolean firstMessageOfTheDay) {
        this.firstMessageOfTheDay = firstMessageOfTheDay;
    }
}