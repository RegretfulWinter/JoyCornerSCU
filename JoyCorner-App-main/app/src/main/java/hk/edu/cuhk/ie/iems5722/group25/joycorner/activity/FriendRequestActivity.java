package hk.edu.cuhk.ie.iems5722.group25.joycorner.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.Status;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.User;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ClearContactRedDotEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.LoadCommunicationAvatarEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.SetItemAvatarEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.ImageUtil;
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

public class FriendRequestActivity extends AppCompatActivity {

    @BindView(R.id.top_bar)
    QMUITopBar topBar;

    @BindView(R.id.group_list_view)
    QMUIGroupListView groupListView;

    private Unbinder unbinder;
    final Gson gson = new Gson();
    private ArrayList<String> requestUserIdList;
    private QMUIGroupListView.Section section;
    private QMUITipDialog tip;
    private Drawable icon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_friend);
        unbinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        requestUserIdList = getIntent().getStringArrayListExtra("userIdList");
        initTopBar();
        initGroupList();
        section = QMUIGroupListView.newSection(this);
        icon = ContextCompat.getDrawable(this, R.drawable.default_avatar);
        initTipDialog();
    }

    private void initTipDialog() {
        tip = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord("Accept new friend")
                .create();
    }

    private void initGroupList() {
        for (String userId : requestUserIdList) {
            Observable.create((ObservableOnSubscribe<User>) emitter -> {
                emitter.onNext(getUserInfo(userId));
                emitter.onComplete();
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<User>() {

                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onNext(User user) {
                    if (user == null) return;
                    QMUICommonListItemView item = groupListView.createItemView(icon, user.getUsername(), user.getNickname(), QMUICommonListItemView.VERTICAL, QMUICommonListItemView.ACCESSORY_TYPE_SWITCH);
                    item.setId(user.getId());
                    item.getSwitch().setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            tip.show();
                            groupListView.postDelayed(() -> {
                                tip.dismiss();
                                groupListView.removeView(item);
                            }, 1000);
                            EventBus.getDefault().post(new ClearContactRedDotEvent(userId, user));
                        }
                    });

                    runOnUiThread(()->{
                        groupListView.removeAllViews();
                        section.addItemView(item, v -> {
                        }).addTo(groupListView);
                    });

                    EventBus.getDefault().post(new LoadCommunicationAvatarEvent(item, user.getAvatar()));
                }

                @Override
                public void onError(@NonNull Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });
        }
    }

    private User getUserInfo(String userId) throws IOException, JSONException {
        String response = NetworkService.get(String.format(URL.GET_USER_BY_ID, userId));
        JSONObject object = new JSONObject(response);
        String status = object.getString("status");
        if (Status.OK.equals(status)) {
            User user = gson.fromJson(object.getString("user"), User.class);
            return user;
        }
        return null;
    }

    private void initTopBar() {
        topBar.setTitle("New friends");
        topBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ClearContactRedDotEvent event) {
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", String.valueOf(LoginActivity.currentUser.getId()))
                .add("friend_id", event.getUserId())
                .add("nickname", event.getUser().getNickname())
                .add("status", Status.ENABLE)
                .build();
        try {
            NetworkService.patch(URL.PATCH_FRIEND, formBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(LoadCommunicationAvatarEvent event) {
        String avatarPath = event.getAvatar();
        if ("/default.jpg".equals(avatarPath)) {
            return;
        }

        Bitmap b = ImageUtil.bitmapContainer.get(avatarPath);

        if (b != null) {
            EventBus.getDefault().post(new SetItemAvatarEvent(event.getItem(), b));
        } else {
            File file = new File(avatarPath);

            if (file.exists()) {
                Bitmap bitmap = ImageUtil.zoomImg(avatarPath, 700);
                EventBus.getDefault().post(new SetItemAvatarEvent(event.getItem(), bitmap));
            } else {
                try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
                    String url = URL.GET_FILE + "/"
                            + "avatar"
                            + "?file_path=" + avatarPath;
                    byte[] response = NetworkService.getFile(url);
                    stream.write(response, 0, response.length);
                    stream.flush();
                    Bitmap bitmap = ImageUtil.zoomImg(avatarPath, 700);
                    EventBus.getDefault().post(new SetItemAvatarEvent(event.getItem(), bitmap));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SetItemAvatarEvent event) {
        event.getItem().setImageDrawable(new BitmapDrawable(getResources(), event.getBitmap()));
    }
}
