package jp.gr.java_conf.datingapp.models;


import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

public class Chat {
    private String from;
    private String to;
    private String message;
    private String my_img;
    private long time_stamp;
    private String img_uri;
    private Profile profile;
    private boolean firstMessageOfTheDay;
    private boolean isSeen;

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

    public long getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(long time_stamp) {
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

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getImg_uri() {
        return img_uri;
    }

    public void setImg_uri(String img_uri) {
        this.img_uri = img_uri;
    }

    public static Map<String, Object> toMap(Chat chat) {
        Map<String, Object> map = new HashMap<>();
        map.put("from", chat.getFrom());
        map.put("to", chat.getTo());
        map.put("message", chat.getMessage());
        map.put("my_img", chat.getMy_img());
        map.put("time_stamp", chat.getTime_stamp());
        map.put("isFirstMessageOfTheDay", chat.isFirstMessageOfTheDay());
        map.put("isSeen", chat.isSeen());
        map.put("img_uri", chat.getImg_uri());
        return map;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", message='" + message + '\'' +
                ", my_img='" + my_img + '\'' +
                ", time_stamp=" + time_stamp +
                ", imageUri=" + img_uri +
                ", profile=" + profile +
                ", firstMessageOfTheDay=" + firstMessageOfTheDay +
                ", isSeen=" + isSeen +
                '}';
    }
}
