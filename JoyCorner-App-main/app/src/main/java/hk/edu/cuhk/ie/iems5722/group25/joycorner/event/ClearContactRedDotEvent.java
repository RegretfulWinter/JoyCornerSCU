package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.User;

public class ClearContactRedDotEvent {
    private String userId;
    private User user;

    public ClearContactRedDotEvent(String userId, User user) {
        this.userId = userId;
        this.user = user;
    }

    public String getUserId() {
        return userId;
    }

    public User getUser() {
        return user;
    }
}
