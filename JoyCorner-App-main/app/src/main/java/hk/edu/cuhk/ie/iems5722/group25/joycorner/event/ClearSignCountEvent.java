package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

public class ClearSignCountEvent {
    private int contactId;

    public ClearSignCountEvent(int contactId) {
        this.contactId = contactId;
    }

    public int getContactId() {
        return contactId;
    }
}
