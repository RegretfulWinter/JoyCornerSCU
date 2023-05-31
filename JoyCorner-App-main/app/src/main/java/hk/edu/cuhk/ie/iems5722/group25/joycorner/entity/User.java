package hk.edu.cuhk.ie.iems5722.group25.joycorner.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private int id;
    private String username;
    private String nickname;
    private String avatar;
    private String location;
    private String birth;
    private String sex;
    private String pw;
    private String signature;
}
