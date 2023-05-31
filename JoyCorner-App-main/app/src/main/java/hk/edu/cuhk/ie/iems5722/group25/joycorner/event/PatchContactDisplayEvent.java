package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

public class PatchContactDisplayEvent {

    private int userId;
    private int contactId;
    private String contactType;
    private String display;

    public PatchContactDisplayEvent(int userId, int contactId, String contactType, String display) {
        this.userId = userId;
        this.contactId = contactId;
        this.contactType = contactType;
        this.display = display;
    }

    public int getUserId() {
        return userId;
    }

    public int getContactId() {
        return contactId;
    }

    public String getContactType() {
        return contactType;
    }

    public String getDisplay() {
        return display;
    }
}
