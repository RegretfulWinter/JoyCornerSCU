package hk.edu.cuhk.ie.iems5722.group25.joycorner.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Communication {
    /**
     * when contactType is P2P, this id is userId
     * when contactType is GRP, this id is groupId
     */
    private int contactId;
    /**
     * P2P or GRP
     */
    private String contactType;
    private String nickname;
    /**
     * when contactType is P2P, there will be a remark for the friend
     */
    private String remark;
    private String avatar;
    private String latestMessage;
    /**
     * when the latest message was sent
     */
    private String when;
}
