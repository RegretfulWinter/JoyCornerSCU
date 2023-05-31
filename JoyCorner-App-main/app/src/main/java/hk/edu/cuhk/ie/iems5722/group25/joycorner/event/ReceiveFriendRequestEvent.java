package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

public class ReceiveFriendRequestEvent {
    private String from_id;

    public ReceiveFriendRequestEvent(String from_id) {
        this.from_id = from_id;
    }

    public String getFrom_id() {
        return from_id;
    }
}
