package jp.gr.java_conf.datingapp.model;

public class SwitchButton {
    boolean isOn;

    public SwitchButton(boolean isOn) {
        this.isOn = isOn;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }
}
