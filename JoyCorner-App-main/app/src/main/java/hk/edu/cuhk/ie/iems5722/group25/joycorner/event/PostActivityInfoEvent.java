package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.ActivityInfo;

public class PostActivityInfoEvent {
    private ActivityInfo activityInfo;

    public PostActivityInfoEvent(ActivityInfo activityInfo) {
        this.activityInfo = activityInfo;
    }

    public ActivityInfo getActivityInfo() {
        return activityInfo;
    }
}
