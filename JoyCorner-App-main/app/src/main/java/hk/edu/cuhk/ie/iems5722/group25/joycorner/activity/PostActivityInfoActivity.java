package hk.edu.cuhk.ie.iems5722.group25.joycorner.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.ActivityGameName;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.Status;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.ActivityInfo;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.PostActivityInfoEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.PostActivityInfoSuccessEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.NetworkService;

public class PostActivityInfoActivity extends AppCompatActivity {

    @BindView(R.id.top_bar)
    QMUITopBar topBar;
    @BindView(R.id.l_logo)
    ImageView avatarView;
    @BindView(R.id.group_list_view)
    QMUIGroupListView groupListView;

    private Unbinder unbinder;
    private final Gson gson = new Gson();
    private QMUICommonListItemView name;
    private QMUICommonListItemView type;
    private QMUICommonListItemView content;
    private QMUICommonListItemView date;
    private QMUICommonListItemView startTime;
    private QMUICommonListItemView endTime;
    private QMUICommonListItemView location;
    private QMUICommonListItemView participants;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_activity);

        unbinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        initTopBar();

        initGroupListView();
    }

    private void initGroupListView() {
        name = groupListView.createItemView("Name");
        name.setOrientation(QMUICommonListItemView.HORIZONTAL);
        name.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        name.setId(R.id.activity_name);

        type = groupListView.createItemView("Type");
        type.setOrientation(QMUICommonListItemView.HORIZONTAL);
        type.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        type.setId(R.id.activity_type);

        content = groupListView.createItemView("Content");
        content.setOrientation(QMUICommonListItemView.HORIZONTAL);
        content.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        content.setId(R.id.activity_content);

        date = groupListView.createItemView("Activity Date");
        date.setOrientation(QMUICommonListItemView.HORIZONTAL);
        date.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        date.setId(R.id.activity_start_time);

        startTime = groupListView.createItemView("Start time");
        startTime.setOrientation(QMUICommonListItemView.HORIZONTAL);
        startTime.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        startTime.setId(R.id.activity_start_time);

        endTime = groupListView.createItemView("End time");
        endTime.setOrientation(QMUICommonListItemView.HORIZONTAL);
        endTime.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        endTime.setId(R.id.activity_end_time);

        location = groupListView.createItemView("Location");
        location.setOrientation(QMUICommonListItemView.HORIZONTAL);
        location.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        location.setId(R.id.activity_location);

        participants = groupListView.createItemView("Number of participants");
        participants.setOrientation(QMUICommonListItemView.HORIZONTAL);
        participants.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        participants.setId(R.id.activity_number_of_participants);

        QMUIDialogAction cancel = new QMUIDialogAction("Cancel", (dialog, index) -> dialog.dismiss());

        Calendar calendar = Calendar.getInstance();
        QMUIGroupListView.newSection(this)
                .addItemView(name, v -> {
                    QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(this);

                    QMUIDialogAction confirm = new QMUIDialogAction("Confirm", (dialog, index) -> {
                        String input = builder.getEditText().getText().toString();
                        name.setDetailText(input);
                        dialog.dismiss();
                    });
                    builder.setTitle("Activity Name")
                            .setDefaultText(name.getDetailText())
                            .addAction(cancel)
                            .addAction(confirm)
                            .show();
                })
                .addItemView(type, v -> new QMUIDialog.CheckableDialogBuilder(this)
                        .setTitle("Activity Type")
                        .addItems(ActivityGameName.NAMES, ((dialog, which) -> {
                            type.setDetailText(ActivityGameName.NAMES[which]);
                            avatarView.setImageDrawable(getDrawable(ActivityGameName.getGameDrawable(ActivityGameName.NAMES[which])));
                            dialog.dismiss();
                        })).create().show())
                .addItemView(content, v -> {
                    QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(this);

                    QMUIDialogAction confirm = new QMUIDialogAction("Confirm", (dialog, index) -> {
                        String input = builder.getEditText().getText().toString();
                        content.setDetailText(input);
                        dialog.dismiss();
                    });
                    builder.setTitle("Activity Content")
                            .setDefaultText(content.getDetailText())
                            .addAction(cancel)
                            .addAction(confirm)
                            .show();
                })
                .addItemView(date, v -> new DatePickerDialog(this, (view, year, month, dayOfMonth) -> date.setDetailText(String.format("%d-%02d-%02d", year, month+1, dayOfMonth)), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show())
                .addItemView(startTime, v -> new TimePickerDialog(this, (view, hourOfDay, minute) -> startTime.setDetailText(String.format("%02d:%02d:00", hourOfDay, minute)), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show())
                .addItemView(endTime, v -> new TimePickerDialog(this, (view, hourOfDay, minute) -> endTime.setDetailText(String.format("%02d:%02d:00", hourOfDay, minute)), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show())
                .addItemView(location, v -> {
                    QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(this);

                    QMUIDialogAction confirm = new QMUIDialogAction("Confirm", (dialog, index) -> {
                        String input = builder.getEditText().getText().toString();
                        location.setDetailText(input);
                        dialog.dismiss();
                    });
                    builder.setTitle("Location")
                            .setDefaultText(location.getDetailText())
                            .addAction(cancel)
                            .addAction(confirm)
                            .show();
                })
                .addItemView(participants, v -> {
                    QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(this);

                    QMUIDialogAction confirm = new QMUIDialogAction("Confirm", (dialog, index) -> {
                        String input = builder.getEditText().getText().toString();
                        participants.setDetailText(input);
                        dialog.dismiss();
                    });
                    builder.setTitle("Number of participants")
                            .setDefaultText(participants.getDetailText())
                            .addAction(cancel)
                            .addAction(confirm)
                            .show();
                }).addTo(groupListView);
    }

    private void initTopBar() {
        topBar.setTitle("Post Activity");
        topBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.join_btn)
    public void onClick() {
        if (!checkInputValues()) {
            return;
        }

        String startDatetime = date.getDetailText().toString().trim() + " " + startTime.getDetailText().toString().trim();
        String endDatetime = date.getDetailText().toString().trim() + " " + endTime.getDetailText().toString().trim();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        try {
            Date start = sdf.parse(startDatetime);
            Date end = sdf.parse(endDatetime);
            if (start.after(end)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(end);
                calendar.add(Calendar.DATE, 1);
                endDatetime = sdf.format(calendar.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        ActivityInfo activityInfo = ActivityInfo.builder()
                .creator(LoginActivity.currentUser.getId())
                .nickname(name.getDetailText().toString().trim())
                .activityType(type.getDetailText().toString())
                .content(content.getDetailText().toString().trim())
                .startTime(startDatetime)
                .endTime(endDatetime)
                .location(location.getDetailText().toString().trim())
                .curParticipantsNum(1)
                .maxParticipantsNum(Integer.parseInt(participants.getDetailText().toString().trim()))
                .build();

        QMUITipDialog.Builder builder = new QMUITipDialog.Builder(this);
        builder.setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS);
        QMUITipDialog tip = builder.setTipWord("Successfully posted event").create();
        tip.show();
        groupListView.postDelayed(() -> {
            tip.dismiss();
            this.finish();
        }, 1000);

        EventBus.getDefault().post(new PostActivityInfoEvent(activityInfo));
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(PostActivityInfoEvent event) {
        try {
            String response = NetworkService.post(URL.POST_ACTIVITY, gson.toJson(event.getActivityInfo()));
            JSONObject object = new JSONObject(response);
            String status = object.getString("status");
            if (Status.OK.equals(status)) {
                int activityId = object.getInt("activity_id");
                event.getActivityInfo().setId(activityId);
                EventBus.getDefault().post(new PostActivityInfoSuccessEvent(event.getActivityInfo()));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean checkInputValues() {
        QMUITipDialog.Builder builder = new QMUITipDialog.Builder(this);
        builder.setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL);
        if ("".equals(name.getDetailText().toString().trim())) {
            QMUITipDialog tip = builder.setTipWord("Please input activity name").create();
            tip.show();
            groupListView.postDelayed(tip::dismiss, 1000);
            return false;
        }
        if (name.getDetailText().toString().trim().length() > 20) {
            QMUITipDialog tip = builder.setTipWord("Activity name too long").create();
            tip.show();
            groupListView.postDelayed(tip::dismiss, 1000);
            return false;
        }
        if ("".equals(type.getDetailText().toString())) {
            QMUITipDialog tip = builder.setTipWord("Please select activity type").create();
            tip.show();
            groupListView.postDelayed(tip::dismiss, 1000);
            return false;
        }
        if ("".equals(content.getDetailText().toString().trim())) {
            QMUITipDialog tip = builder.setTipWord("Please input activity content").create();
            tip.show();
            groupListView.postDelayed(tip::dismiss, 1000);
            return false;
        }
        if (content.getDetailText().toString().trim().length() > 255) {
            QMUITipDialog tip = builder.setTipWord("Activity content too long").create();
            tip.show();
            groupListView.postDelayed(tip::dismiss, 1000);
            return false;
        }
        if ("".equals(date.getDetailText().toString().trim())) {
            QMUITipDialog tip = builder.setTipWord("Please select activity date").create();
            tip.show();
            groupListView.postDelayed(tip::dismiss, 1000);
            return false;
        }
        if ("".equals(startTime.getDetailText().toString().trim())) {
            QMUITipDialog tip = builder.setTipWord("Please select activity start time").create();
            tip.show();
            groupListView.postDelayed(tip::dismiss, 1000);
            return false;
        }
        if ("".equals(endTime.getDetailText().toString().trim())) {
            QMUITipDialog tip = builder.setTipWord("Please input activity end time").create();
            tip.show();
            groupListView.postDelayed(tip::dismiss, 1000);
            return false;
        }
        if ("".equals(location.getDetailText().toString().trim())) {
            QMUITipDialog tip = builder.setTipWord("Please input activity location").create();
            tip.show();
            groupListView.postDelayed(tip::dismiss, 1000);
            return false;
        }
        if (location.getDetailText().toString().trim().length() > 20) {
            QMUITipDialog tip = builder.setTipWord("Activity location too long").create();
            tip.show();
            groupListView.postDelayed(tip::dismiss, 1000);
            return false;
        }
        if ("".equals(participants.getDetailText().toString().trim())) {
            QMUITipDialog tip = builder.setTipWord("Please input number of participants").create();
            tip.show();
            groupListView.postDelayed(tip::dismiss, 1000);
            return false;
        }

        if (!isNumeric(participants.getDetailText().toString().trim())) {
            QMUITipDialog tip = builder.setTipWord("Please check the number of participants").create();
            tip.show();
            groupListView.postDelayed(tip::dismiss, 1000);
            return false;
        }

        if (Integer.parseInt(participants.getDetailText().toString().trim()) < 2) {
            QMUITipDialog tip = builder.setTipWord("Number of participants too small").create();
            tip.show();
            groupListView.postDelayed(tip::dismiss, 1000);
            return false;
        }

        return true;
    }

    private boolean isNumeric(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
