package hk.edu.cuhk.ie.iems5722.group25.joycorner.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Participant {
    private int userId;
    private int activityId;
    private String username;
    private String nickname;
    private String avatar;
}
