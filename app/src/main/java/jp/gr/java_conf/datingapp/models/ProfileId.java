package jp.gr.java_conf.datingapp.models;

public class ProfileId {
    String profileId;
    public <T extends ProfileId> T withId(String s) {
        this.profileId = s;
        return (T) this;
    }
}
