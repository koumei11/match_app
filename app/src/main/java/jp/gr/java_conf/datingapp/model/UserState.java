package jp.gr.java_conf.datingapp.model;

public class UserState {
    private String state;
    private long last_changed;

    UserState() {

    }

    UserState(String state, long last_changed) {
        this.state = state;
        this.last_changed = last_changed;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getLast_changed() {
        return last_changed;
    }

    public void setLast_changed(long last_changed) {
        this.last_changed = last_changed;
    }
}
