package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;

public class LoadActivityParticipantsEvent {
    private QMUICommonListItemView item;
    private String[] participants;

    public LoadActivityParticipantsEvent(QMUICommonListItemView item, String[] participants) {
        this.item = item;
        this.participants = participants;
    }


    public QMUICommonListItemView getItem() {
        return item;
    }

    public String[] getParticipants() {
        return participants;
    }
}
