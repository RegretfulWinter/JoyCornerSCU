package hk.edu.cuhk.ie.iems5722.group25.joycorner.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivityInfo {
    private int id;
    private String nickname;
    private int creator;
    private String startTime;
    private String endTime;
    private String location;
    private String content;
    private String activityType;
    private int curParticipantsNum;
    private int maxParticipantsNum;
    private String status;
}
