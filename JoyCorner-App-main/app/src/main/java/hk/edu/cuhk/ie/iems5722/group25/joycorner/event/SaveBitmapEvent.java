package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

import android.graphics.Bitmap;

public class SaveBitmapEvent {
    private Bitmap bitmap;

    public SaveBitmapEvent(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
