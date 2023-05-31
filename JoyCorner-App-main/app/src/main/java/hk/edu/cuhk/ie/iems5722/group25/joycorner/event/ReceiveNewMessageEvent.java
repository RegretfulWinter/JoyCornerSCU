package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

import java.util.Map;

public class ReceiveNewMessageEvent {
    private Map<String, String> dataPayload;

    public ReceiveNewMessageEvent(Map<String, String> dataPayload) {
        this.dataPayload = dataPayload;
    }

    public Map<String, String> getDataPayload() {
        return dataPayload;
    }
}
