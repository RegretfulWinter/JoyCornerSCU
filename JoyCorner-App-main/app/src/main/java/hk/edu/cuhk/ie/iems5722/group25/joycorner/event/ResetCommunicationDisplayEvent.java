package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

public class ResetCommunicationDisplayEvent {
    private int contactId;
    private String display;

    public ResetCommunicationDisplayEvent(int contactId, String display) {
        this.contactId = contactId;
        this.display = display;
    }

    public int getContactId() {
        return contactId;
    }

    public String getDisplay() {
        return display;
    }
}
