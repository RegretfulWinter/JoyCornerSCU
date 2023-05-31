package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

import android.graphics.Bitmap;

import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;

public class SetItemAvatarEvent {
    private QMUICommonListItemView item;
    private Bitmap bitmap;

    public SetItemAvatarEvent(QMUICommonListItemView item, Bitmap bitmap) {
        this.item = item;
        this.bitmap = bitmap;
    }

    public QMUICommonListItemView getItem() {
        return item;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
