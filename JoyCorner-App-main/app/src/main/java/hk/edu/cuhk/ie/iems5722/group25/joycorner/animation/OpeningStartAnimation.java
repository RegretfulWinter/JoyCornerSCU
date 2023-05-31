package hk.edu.cuhk.ie.iems5722.group25.joycorner.animation;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.SoftReference;
import java.util.Timer;
import java.util.TimerTask;

public class OpeningStartAnimation extends View {
    private long animationInterval = 1500;
    private long animationFinishTime = 350;
    private final WidthAndHeightOfView mWidthAndHeightOfView;
    private int colorOfBackground = Color.WHITE;
    private float fraction;
    private Drawable mDrawable;
    private int colorOfAppIcon = Color.parseColor("#00897b");
    private String appName;
    private int colorOfAppName = Color.parseColor("#00897b");
    private String appStatement;
    private int colorOfAppStatement = Color.parseColor("#607D8B");
    private DelegateRecycleView mDelegateRecycleView;
    private DrawStrategy mDrawStrategy = new NormalDrawStrategy();
    private static final int FINISH_ANIMATION = 1;

    private OpeningStartAnimation(Context context) {
        super(context);
        PackageManager packageManager = context.getPackageManager();
        mDrawable = context.getApplicationInfo().loadIcon(packageManager);
        appName = (String) packageManager.getApplicationLabel(context.getApplicationInfo());
        appStatement = "Sample Statement";
    }

    @SuppressWarnings("unused")
    private void setFraction(float fraction) {
        this.fraction = fraction;
        invalidate();
    }

    {
        FrameLayout.LayoutParams layoutParams;
        layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        this.setLayoutParams(layoutParams);
        mWidthAndHeightOfView = new WidthAndHeightOfView();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(colorOfBackground);
        super.onDraw(canvas);
        mWidthAndHeightOfView.setHeight(getHeight());
        mWidthAndHeightOfView.setWidth(getWidth());
        mDrawStrategy.drawAppIcon(canvas, fraction, mDrawable, colorOfAppIcon,
                mWidthAndHeightOfView);
        mDrawStrategy.drawAppName(canvas, fraction, appName, colorOfAppName,
                mWidthAndHeightOfView);
        mDrawStrategy.drawAppStatement(canvas, fraction, appStatement, colorOfAppStatement,
                mWidthAndHeightOfView);
    }

    public void show(Activity mActivity) {
        SoftReference<Activity> softReference = new SoftReference<>(mActivity);
        final Activity activity = softReference.get();
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        if (activity instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (actionBar != null)
                actionBar.hide();
        } else {
            android.app.ActionBar actionBar = activity.getActionBar();
            if (actionBar != null)
                actionBar.hide();
        }
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        activity.addContentView(this, layoutParams);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "fraction", 0, 1);
        objectAnimator.setDuration(animationInterval - 50);
        objectAnimator.start();

        final Handler handler = new Handler(message -> {
            if (message.what == FINISH_ANIMATION) {
                moveAnimation(activity);
            }
            return false;
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = FINISH_ANIMATION;
                handler.sendMessage(message);
            }
        }, animationInterval);
    }

    private void moveAnimation(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        if (activity instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (actionBar != null)
                actionBar.show();
        } else {
            android.app.ActionBar actionBar = activity.getActionBar();
            if (actionBar != null)
                actionBar.show();
        }
        this.animate()
                .scaleX(0)
                .scaleY(0)
                .withLayer()
                .alpha(0)
                .setDuration(animationFinishTime);
        mDelegateRecycleView.finishAnimation();
    }

    public static final class Builder implements DelegateRecycleView {
        OpeningStartAnimation mOpeningStartAnimation;

        public Builder(Context context) {
            mOpeningStartAnimation = new OpeningStartAnimation(context);
            mOpeningStartAnimation.mDelegateRecycleView = this;
        }

        public Builder setAnimationInterval(long animationInterval) {
            mOpeningStartAnimation.animationInterval = animationInterval;
            return this;
        }

        public Builder setAnimationFinishTime(long animationFinishTime) {
            mOpeningStartAnimation.animationFinishTime = animationFinishTime;
            return this;
        }

        public Builder setAppIcon(Drawable drawable) {
            mOpeningStartAnimation.mDrawable = drawable;
            return this;
        }

        public Builder setColorOfAppIcon(int colorOfAppIcon) {
            mOpeningStartAnimation.colorOfAppIcon = colorOfAppIcon;
            return this;
        }

        public Builder setAppName(String appName) {
            mOpeningStartAnimation.appName = appName;
            return this;
        }

        public Builder setColorOfAppName(int colorOfAppName) {
            mOpeningStartAnimation.colorOfAppName = colorOfAppName;
            return this;
        }

        public Builder setAppStatement(String appStatement) {
            mOpeningStartAnimation.appStatement = appStatement;
            return this;
        }

        public Builder setColorOfAppStatement(int colorOfAppStatement) {
            mOpeningStartAnimation.colorOfAppStatement = colorOfAppStatement;
            return this;
        }

        public Builder setColorOfBackground(int colorOfBackground) {
            mOpeningStartAnimation.colorOfBackground = colorOfBackground;
            return this;
        }

        public Builder setDrawStrategy(DrawStrategy drawStrategy) {
            mOpeningStartAnimation.mDrawStrategy = drawStrategy;
            return this;
        }

        public OpeningStartAnimation create() {
            return mOpeningStartAnimation;
        }

        @Override
        public void finishAnimation() {
            mOpeningStartAnimation = null;
        }
    }
}
