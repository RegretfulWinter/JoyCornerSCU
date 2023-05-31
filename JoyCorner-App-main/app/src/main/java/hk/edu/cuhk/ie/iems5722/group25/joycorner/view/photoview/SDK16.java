package hk.edu.cuhk.ie.iems5722.group25.joycorner.view.photoview;

import android.annotation.TargetApi;
import android.view.View;

@TargetApi(16)
public class SDK16 {

    public static void postOnAnimation(View view, Runnable r) {
        view.postOnAnimation(r);
    }

}
