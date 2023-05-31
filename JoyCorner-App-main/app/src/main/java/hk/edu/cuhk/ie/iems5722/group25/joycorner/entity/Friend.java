package hk.edu.cuhk.ie.iems5722.group25.joycorner.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Friend {
    private int id;
    private int userId;
    private int friendId;
    private String nickname;
    private User friend;
}
