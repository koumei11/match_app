package jp.gr.java_conf.datingapp.model;

import java.util.Objects;

public class Match {

    private String user1;
    private String user2;
    private long time_stamp;
    private boolean block;

    public Match(String user1, String user2) {
        this.user1 = user1;
        this.user2 = user2;
    }

    public Match() {
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public long getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(long time_stamp) {
        this.time_stamp = time_stamp;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match)) return false;
        Match match = (Match) o;
        return (getUser1().equals(match.getUser1()) &&
                getUser2().equals(match.getUser2())) || (getUser1().equals(match.getUser2()) &&
                getUser2().equals(match.getUser1()));
    }

    @Override
    public String toString() {
        return "Match{" +
                "user1='" + user1 + '\'' +
                ", user2='" + user2 + '\'' +
                ", time_stamp=" + time_stamp +
                ", block=" + block +
                '}';
    }
}
