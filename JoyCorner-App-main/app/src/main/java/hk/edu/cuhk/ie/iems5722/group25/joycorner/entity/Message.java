package hk.edu.cuhk.ie.iems5722.group25.joycorner.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Message {
    private int id;
    private int fromId;
    private int destId;
    private String content;
    private String contentType;
    private String msgType;
    private String status;
    private String createDate;
}
