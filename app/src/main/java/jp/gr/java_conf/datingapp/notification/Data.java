package jp.gr.java_conf.datingapp.notification;

import jp.gr.java_conf.datingapp.enums.NotificationType;

public class Data {
    private String userId;
    private int icon;
    private NotificationType type;
    private String body;
    private String title;
    private String sent;
    private String image;

    public Data(String userId, int icon, NotificationType type, String body, String title, String sent, String image) {
        this.userId = userId;
        this.icon = icon;
        this.type = type;
        this.body = body;
        this.title = title;
        this.sent = sent;
        this.image = image;
    }

    public Data() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
