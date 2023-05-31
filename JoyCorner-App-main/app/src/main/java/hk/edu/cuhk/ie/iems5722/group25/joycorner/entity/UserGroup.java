package hk.edu.cuhk.ie.iems5722.group25.joycorner.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserGroup {
    private int id;
    private int userId;
    private int groupId;
    private String nickname;
    private Group group;
}
