package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.JCMessage;

public class LoadFileMessageEvent {
    private JCMessage message;
    private String type;
    private boolean isNewMessage;


    public boolean isNewMessage() {
        return isNewMessage;
    }


    public LoadFileMessageEvent(JCMessage fileMessage, String type) {
        this.message = fileMessage;
        this.type = type;
        this.isNewMessage = false;
    }

    public LoadFileMessageEvent(JCMessage fileMessage, String type, boolean isNewMessage) {
        this.message = fileMessage;
        this.type = type;
        this.isNewMessage = isNewMessage;
    }

    public JCMessage getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }
}
