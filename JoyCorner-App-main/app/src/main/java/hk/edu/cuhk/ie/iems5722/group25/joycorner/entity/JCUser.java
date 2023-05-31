package hk.edu.cuhk.ie.iems5722.group25.joycorner.entity;

import cn.jiguang.imui.commons.models.IUser;

public class JCUser implements IUser {
    private String id;
    private String displayName;
    private String avatar;

    public JCUser(String id, String displayName, String avatar) {
        this.id = id;
        this.displayName = displayName;
        this.avatar = avatar;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String getAvatarFilePath() {
        return this.avatar;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
