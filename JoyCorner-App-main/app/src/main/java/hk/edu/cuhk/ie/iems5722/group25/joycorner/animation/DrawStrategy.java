package hk.edu.cuhk.ie.iems5722.group25.joycorner.animation;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public interface DrawStrategy {

    void drawAppName(Canvas canvas, float fraction, String name, int colorOfAppName,
                     WidthAndHeightOfView widthAndHeightOfView);

    void drawAppIcon(Canvas canvas, float fraction, Drawable icon, int colorOfIcon,
                     WidthAndHeightOfView widthAndHeightOfView);

    void drawAppStatement(Canvas canvas, float fraction, String statement, int colorOfStatement,
                          WidthAndHeightOfView widthAndHeightOfView);
}
