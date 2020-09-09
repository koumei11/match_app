package jp.gr.java_conf.datingapp.models;

public class Match {

    private String name;
    private String user_id;
    private String img_url;
    private long time_stamp;
    private boolean isBlock;

    public Match(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public long getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(long time_stamp) {
        this.time_stamp = time_stamp;
    }

    public boolean isBlock() {
        return isBlock;
    }

    public void setBlock(boolean block) {
        isBlock = block;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return user_id.equals(match.user_id);
    }

    @Override
    public String toString() {
        return "Match{" +
                "name='" + name + '\'' +
                ", user_id='" + user_id + '\'' +
                ", img_url='" + img_url + '\'' +
                ", time_stamp=" + time_stamp +
                ", isBlock=" + isBlock +
                '}';
    }
}
