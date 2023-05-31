package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.User;

public class LoadLoginAvatarEvent {
    private User user;

    public LoadLoginAvatarEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
