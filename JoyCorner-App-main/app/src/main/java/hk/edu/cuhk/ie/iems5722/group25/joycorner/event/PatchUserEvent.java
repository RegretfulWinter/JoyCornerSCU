package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

import hk.edu.cuhk.ie.iems5722.group25.joycorner.activity.LoginActivity;

public class PatchUserEvent {
    private int userId;
    private String type;
    private String value;

    public PatchUserEvent(String type, String value) {
        this.userId = LoginActivity.currentUser.getId();
        this.type = type;
        this.value = value;
    }

    public int getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
