package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.Friend;

public class ReceiveFriendConfirmEvent {
    private Friend friend;

    public ReceiveFriendConfirmEvent(Friend friend) {
        this.friend = friend;
    }

    public Friend getFriend() {
        return friend;
    }
}
