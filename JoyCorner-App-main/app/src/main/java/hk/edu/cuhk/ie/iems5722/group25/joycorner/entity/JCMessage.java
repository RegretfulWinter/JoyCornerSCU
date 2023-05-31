package hk.edu.cuhk.ie.iems5722.group25.joycorner.entity;

import java.util.HashMap;
import java.util.UUID;

import cn.jiguang.imui.commons.models.IMessage;
import cn.jiguang.imui.commons.models.IUser;

public class JCMessage implements IMessage {
    private String msgId;
    private String text;
    private String timeString;
    private int type;
    private JCUser user;
    private String mediaFilePath;
    private long duration;
    private String progress;

    public JCMessage(String text, int type) {
        this.msgId = UUID.randomUUID().toString();
        this.text = text;
        this.type = type;
    }

    public JCMessage(String text, int type, JCUser user) {
        this.msgId = UUID.randomUUID().toString();
        this.text = text;
        this.type = type;
        this.user = user;
    }

    public JCMessage(String msgId, String text, String timeString, int type, JCUser user, String mediaFilePath, long duration, String progress) {
        this.msgId = msgId;
        this.text = text;
        this.timeString = timeString;
        this.type = type;
        this.user = user;
        this.mediaFilePath = mediaFilePath;
        this.duration = duration;
        this.progress = progress;
    }

    @Override
    public String getMsgId() {
        return this.msgId;
    }

    @Override
    public IUser getFromUser() {
        return this.user == null ? new JCUser("0", "user1", "") : this.user;
    }

    @Override
    public String getTimeString() {
        return this.timeString;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public MessageStatus getMessageStatus() {
        // Message status. After sending Message, change the status so that the progress bar will dismiss.
        return IMessage.MessageStatus.SEND_SUCCEED;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public String getMediaFilePath() {
        return this.mediaFilePath;
    }

    @Override
    public long getDuration() {
        return this.duration;
    }

    @Override
    public String getProgress() {
        return this.progress;
    }

    @Override
    public HashMap<String, String> getExtras() {
        return null;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    public void setType(int type) {
        if (type >= 0 && type <= 12) {
            throw new IllegalArgumentException("Message type should not take the value between 0 and 12");
        }
        this.type = type;
    }

    public void setUser(JCUser user) {
        this.user = user;
    }

    public void setMediaFilePath(String mediaFilePath) {
        this.mediaFilePath = mediaFilePath;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

}
