package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

import java.util.Date;

public class ChangeCommunicationEvent {
    private int contactId;
    private String text;
    private Date date;

    public ChangeCommunicationEvent(int contactId, String text, Date date) {
        this.contactId = contactId;
        this.text = text;
        this.date = date;
    }

    public int getContactId() {
        return contactId;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }
}
