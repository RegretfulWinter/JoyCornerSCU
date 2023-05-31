package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;

public class LoadCommunicationAvatarEvent {
    private QMUICommonListItemView item;
    private String avatar;

    public LoadCommunicationAvatarEvent(QMUICommonListItemView item, String avatar) {
        this.item = item;
        this.avatar = avatar;
    }

    public QMUICommonListItemView getItem() {
        return item;
    }

    public String getAvatar() {
        return avatar;
    }
}
