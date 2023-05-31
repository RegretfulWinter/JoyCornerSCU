package hk.edu.cuhk.ie.iems5722.group25.joycorner.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
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

public class StrangerInfoActivity extends AppCompatActivity {
    @BindView(R.id.top_bar)
    QMUITopBar topBar;
    @BindView(R.id.l_logo)
    QMUIRadiusImageView avatarView;
    @BindView(R.id.group_list_view)
    QMUIGroupListView groupListView;
    private QMUITipDialog tip;
    private Unbinder unbinder;
    private int userId;
    private String avatar;
    private String username;
    private String nickname;
    private String location;
    private String sex;
    private String signature;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stranger_info);
        unbinder = ButterKnife.bind(this);

        initVariables();
        initTopBar();
        initAvatar();
        initGroupListView();
        initTipDialog();
    }

    private void initTipDialog() {
        tip = new QMUITipDialog.Builder(StrangerInfoActivity.this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord("Apply successfully")
                .create();
    }

    private void initVariables() {
        userId = getIntent().getIntExtra("userId", -1);
        avatar = getIntent().getStringExtra("avatar");
        username = getIntent().getStringExtra("username");
        nickname = getIntent().getStringExtra("nickname");
        location = getIntent().getStringExtra("location");
        sex = getIntent().getStringExtra("sex");
        signature = getIntent().getStringExtra("signature");
    }

    private void initAvatar() {
        if (ImageUtil.bitmapContainer.get(avatar) != null) {
            avatarView.setImageBitmap(ImageUtil.bitmapContainer.get(avatar));
        }
    }

    private void initGroupListView() {
        QMUICommonListItemView usernameItem = groupListView.createItemView("Username");
        usernameItem.setDetailText(username);
        usernameItem.setOrientation(QMUICommonListItemView.HORIZONTAL);
        usernameItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_NONE);

        QMUICommonListItemView nicknameItem = groupListView.createItemView("Nickname");
        nicknameItem.setDetailText(nickname);
        nicknameItem.setOrientation(QMUICommonListItemView.HORIZONTAL);
        nicknameItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_NONE);

        QMUICommonListItemView locationItem = groupListView.createItemView("Location");
        locationItem.setDetailText(location);
        locationItem.setOrientation(QMUICommonListItemView.HORIZONTAL);
        locationItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_NONE);

        QMUICommonListItemView sexItem = groupListView.createItemView("Sex");
        sexItem.setDetailText("0".equals(sex) ? "female" : "male");
        sexItem.setOrientation(QMUICommonListItemView.HORIZONTAL);
        sexItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_NONE);

        QMUICommonListItemView signatureItem = groupListView.createItemView("Signature");
        signatureItem.setDetailText(signature);
        signatureItem.setOrientation(QMUICommonListItemView.HORIZONTAL);
        signatureItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_NONE);

        QMUIGroupListView.newSection(this)
                .addItemView(usernameItem, v -> {
                })
                .addItemView(nicknameItem, v -> {
                })
                .addItemView(locationItem, v -> {
                })
                .addItemView(sexItem, v -> {
                })
                .addItemView(signatureItem, v -> {
                })
                .addTo(groupListView);
    }

    private void initTopBar() {
        topBar.setTitle("Profile");
        topBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.apply_friend_btn)
    public void clickApply() {
        tip.show();
        groupListView.postDelayed(() -> tip.dismiss(), 1000);

        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            emitter.onNext(submitApplication());
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {

            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Boolean aBoolean) {

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private Boolean submitApplication() throws IOException {
        RequestBody body = new FormBody.Builder()
                .add("user_id", String.valueOf(LoginActivity.currentUser.getId()))
                .add("friend_id", String.valueOf(userId))
                .build();
        NetworkService.post(URL.POST_FRIEND, body);
        return true;
    }
}
