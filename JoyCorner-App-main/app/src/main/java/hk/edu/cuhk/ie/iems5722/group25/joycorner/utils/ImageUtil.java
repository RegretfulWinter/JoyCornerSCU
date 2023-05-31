package hk.edu.cuhk.ie.iems5722.group25.joycorner.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.qmuiteam.qmui.util.QMUIDrawableHelper;

import java.util.HashMap;
import java.util.Map;

import cn.jiguang.imui.commons.BitmapLoader;

public class ImageUtil {
    public static final Map<String, Bitmap> bitmapContainer = new HashMap<>();

    public static Bitmap zoomImg(String path, int length) {
        Bitmap bitmap = BitmapLoader.getBitmapFromFile(path, length, length);
        bitmap = zoomImg(bitmap, length, length);
        bitmap = getRoundedCornerBitmap(bitmap);
        bitmapContainer.put(path, bitmap);
        return bitmap;
    }

    private static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        Bitmap newBm = null;
        if (bm != null) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            newBm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        }
        return newBm;
    }

    private static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                bitmap.getHeight());
        final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),
                bitmap.getHeight()));
        final float roundPx = 100;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        final Rect src = new Rect(0, 0, bitmap.getWidth(),
                bitmap.getHeight());
        canvas.drawBitmap(bitmap, src, rect, paint);
        return output;
    }

    private static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static Drawable zoomDrawable(Drawable drawable, int size) {
        return new BitmapDrawable(null, zoomImg(QMUIDrawableHelper.drawableToBitmap(drawable), size, size));
    }
}
