package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

import android.graphics.Bitmap;

public class SetUserAvatarEvent {
    private Bitmap bitmap;

    public SetUserAvatarEvent(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
