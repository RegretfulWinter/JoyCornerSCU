package hk.edu.cuhk.ie.iems5722.group25.joycorner.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Group {
    private int id;
    private String nickname;
    private String avatar;
    private int memberNumber;
    private String announcement;
    private int creator;
    private int administrator;
}
