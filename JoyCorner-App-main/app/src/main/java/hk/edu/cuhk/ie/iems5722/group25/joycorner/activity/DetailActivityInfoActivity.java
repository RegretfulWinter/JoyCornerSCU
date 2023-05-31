package hk.edu.cuhk.ie.iems5722.group25.joycorner.activity;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qmuiteam.qmui.layout.QMUIButton;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.ActivityGameName;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.Status;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.ActivityInfo;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.Participant;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.LoadActivityParticipantsEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.NetworkService;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class DetailActivityInfoActivity extends AppCompatActivity {

    @BindView(R.id.top_bar)
    QMUITopBar topBar;
    @BindView(R.id.l_logo)
    ImageView avatarView;
    @BindView(R.id.group_list_view)
    QMUIGroupListView groupListView;
    @BindView(R.id.join_btn)
    QMUIButton joinBtn;

    private Unbinder unbinder;
    private final Gson gson = new Gson();
    private QMUICommonListItemView mName;
    private QMUICommonListItemView mType;
    private QMUICommonListItemView mContent;
    private QMUICommonListItemView mStartDateTime;
    private QMUICommonListItemView mEndDateTime;
    private QMUICommonListItemView mLocation;
    private QMUICommonListItemView mParticipants;
    private ActivityInfo mActivityInfo;
    private List<Participant> mParticipantList = new ArrayList<>();
    private boolean mIsJoined = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_activity);

        unbinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        mActivityInfo = gson.fromJson(getIntent().getStringExtra("activityInfo"), ActivityInfo.class);

        avatarView.setImageDrawable(getDrawable(ActivityGameName.getGameDrawable(mActivityInfo.getActivityType())));

        initTopBar();

        initGroupListView();

        doInBackground();
    }

    private void doInBackground() {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            emitter.onNext(loadActivityParticipants());
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onNext(@NonNull Boolean loadSuccess) {
                if (loadSuccess) {
                    if (mIsJoined) {
                        joinBtn.setText("Already Joined ~");
                        joinBtn.setClickable(false);

                        QMUICommonListItemView item = groupListView.createItemView("Participants List");
                        item.setOrientation(QMUICommonListItemView.HORIZONTAL);
                        item.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
                        String[] parArr = new String[mParticipantList.size()];
                        for (int i = 0; i < mParticipantList.size(); i++) {
                            parArr[i] = mParticipantList.get(i).getUsername() + "( " + mParticipantList.get(i).getNickname() + " )";
                        }

                        mActivityInfo.setCurParticipantsNum(mParticipantList.size());
                        mParticipants.setDetailText(mActivityInfo.getCurParticipantsNum() + " / " + mActivityInfo.getMaxParticipantsNum());

                        EventBus.getDefault().post(new LoadActivityParticipantsEvent(item, parArr));
                    } else {
                        if (mActivityInfo.getCurParticipantsNum() == mActivityInfo.getMaxParticipantsNum()) {
                            joinBtn.setText("Quota Is Full");
                            joinBtn.setClickable(false);
                        }
                    }
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private Boolean loadActivityParticipants() {
        try {
            String response = NetworkService.get(String.format(URL.GET_ACTIVITY_PARTICIPANTS, mActivityInfo.getId()));
            JSONObject object = new JSONObject(response);
            String status = object.getString("status");
            if (Status.OK.equals(status)) {
                String data = object.getString("participants");
                List<Participant> participantList = gson.fromJson(data, new TypeToken<ArrayList<Participant>>() {
                }.getType());
                mParticipantList.clear();
                mParticipantList.addAll(participantList);
                for (Participant p : mParticipantList) {
                    if (p.getUserId() == LoginActivity.currentUser.getId()) {
                        mIsJoined = true;
                        break;
                    }
                }
                return true;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initGroupListView() {
        mName = groupListView.createItemView("Name");
        mName.setOrientation(QMUICommonListItemView.HORIZONTAL);
        mName.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_NONE);
        mName.setDetailText(mActivityInfo.getNickname());
        mName.setId(R.id.activity_name);

        mType = groupListView.createItemView("Type");
        mType.setOrientation(QMUICommonListItemView.HORIZONTAL);
        mType.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_NONE);
        mType.setDetailText(mActivityInfo.getActivityType());
        mType.setId(R.id.activity_type);

        mContent = groupListView.createItemView("Content");
        mContent.setOrientation(QMUICommonListItemView.HORIZONTAL);
        mContent.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_NONE);
        mContent.setDetailText(mActivityInfo.getContent());
        mContent.setId(R.id.activity_content);

        mStartDateTime = groupListView.createItemView("From");
        mStartDateTime.setOrientation(QMUICommonListItemView.HORIZONTAL);
        mStartDateTime.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_NONE);
        mStartDateTime.setDetailText(mActivityInfo.getStartTime());
        mStartDateTime.setId(R.id.activity_start_time);

        mEndDateTime = groupListView.createItemView("To");
        mEndDateTime.setOrientation(QMUICommonListItemView.HORIZONTAL);
        mEndDateTime.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_NONE);
        mEndDateTime.setDetailText(mActivityInfo.getEndTime());
        mEndDateTime.setId(R.id.activity_end_time);

        mLocation = groupListView.createItemView("Location");
        mLocation.setOrientation(QMUICommonListItemView.HORIZONTAL);
        mLocation.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_NONE);
        mLocation.setDetailText(mActivityInfo.getLocation());
        mLocation.setId(R.id.activity_location);

        mParticipants = groupListView.createItemView("Number of participants");
        mParticipants.setOrientation(QMUICommonListItemView.HORIZONTAL);
        mParticipants.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_NONE);
        mParticipants.setDetailText(mActivityInfo.getCurParticipantsNum() + " / " + mActivityInfo.getMaxParticipantsNum());
        mParticipants.setId(R.id.activity_number_of_participants);

        QMUIGroupListView.newSection(this)
                .addItemView(mName, v -> {
                })
                .addItemView(mType, v -> {
                })
                .addItemView(mContent, v -> {
                })
                .addItemView(mStartDateTime, v -> {
                })
                .addItemView(mEndDateTime, v -> {
                })
                .addItemView(mLocation, v -> {
                })
                .addItemView(mParticipants, v -> {
                }).addTo(groupListView);
    }

    private void initTopBar() {
        topBar.setTitle("Activity Information");
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
        joinBtn.setClickable(false);
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            emitter.onNext(joinActivity());
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onNext(@NonNull Boolean joinSuccess) {
                if (joinSuccess) {
                    if (mIsJoined) {
                        joinBtn.setText("Already Joined ~");
                        joinBtn.setClickable(false);

                        QMUICommonListItemView item = groupListView.createItemView("Participants List");
                        item.setOrientation(QMUICommonListItemView.HORIZONTAL);
                        item.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
                        String[] parArr = new String[mParticipantList.size()];
                        for (int i = 0; i < mParticipantList.size(); i++) {
                            parArr[i] = mParticipantList.get(i).getUsername() + "( " + mParticipantList.get(i).getNickname() + " )";
                        }

                        EventBus.getDefault().post(new LoadActivityParticipantsEvent(item, parArr));

                        mParticipants.setDetailText(mActivityInfo.getCurParticipantsNum() + " / " + mActivityInfo.getMaxParticipantsNum());
                    }
                } else {
                    joinBtn.setClickable(true);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                joinBtn.setClickable(true);
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private Boolean joinActivity() {
        String url = String.format(URL.PATCH_ACTIVITY, mActivityInfo.getId());
        RequestBody body = new FormBody.Builder().add("user_id", String.valueOf(LoginActivity.currentUser.getId())).build();
        try {
            String response = NetworkService.patch(url, body);
            JSONObject object = new JSONObject(response);
            String status = object.getString("status");
            if (Status.OK.equals(status)) {
                mIsJoined = true;
                Participant p = Participant.builder()
                        .activityId(mActivityInfo.getId())
                        .userId(LoginActivity.currentUser.getId())
                        .avatar(LoginActivity.currentUser.getAvatar())
                        .nickname(LoginActivity.currentUser.getNickname())
                        .username(LoginActivity.currentUser.getUsername())
                        .build();
                mActivityInfo.setCurParticipantsNum(mActivityInfo.getCurParticipantsNum() + 1);
                mParticipantList.add(p);
                return true;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(LoadActivityParticipantsEvent event) {
        runOnUiThread(() -> QMUIGroupListView.newSection(this)
                .addItemView(event.getItem(), v -> new QMUIDialog.MenuDialogBuilder(this)
                        .setTitle("Participants List")
                        .addItems(event.getParticipants(), ((dialog, which) -> dialog.dismiss())).create().show()).addTo(groupListView));
    }
}
