package jp.gr.java_conf.datingapp.models;


public class Chat {
    private String from;
    private String to;
    private String message;
    private String my_img;
    private long time_stamp;
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

    @Override
    public String toString() {
        return "Chat{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", message='" + message + '\'' +
                ", my_img='" + my_img + '\'' +
                ", time_stamp=" + time_stamp +
                ", profile=" + profile +
                ", firstMessageOfTheDay=" + firstMessageOfTheDay +
                ", isSeen=" + isSeen +
                '}';
    }
}
