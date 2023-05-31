package hk.edu.cuhk.ie.iems5722.group25.joycorner.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.ContactType;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ClearSignCountEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.PatchContactDisplayEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ResetCommunicationDisplayEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.ImageUtil;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.NetworkService;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.TimeUtil;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class FriendInfoActivity extends AppCompatActivity {
    @BindView(R.id.top_bar)
    QMUITopBar topBar;
    @BindView(R.id.l_logo)
    QMUIRadiusImageView avatarView;
    @BindView(R.id.group_list_view)
    QMUIGroupListView groupListView;

    private Unbinder unbinder;
    private int contactId;
    private String display;
    private String avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);

        unbinder = ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        initTopBar();

        initGroupListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initGroupListView() {
        contactId = getIntent().getIntExtra("contactId", -1);
        String contactType = getIntent().getStringExtra("contactType");
        avatar = getIntent().getStringExtra("avatar");
        display = getIntent().getStringExtra("display");
        String username = getIntent().getStringExtra("username");
        String nickname = getIntent().getStringExtra("nickname");
        String location = getIntent().getStringExtra("location");
        String birth = getIntent().getStringExtra("birth");
        String sex = getIntent().getStringExtra("sex");
        String signature = getIntent().getStringExtra("signature");

        if (ImageUtil.bitmapContainer.get(avatar) != null) {
            avatarView.setImageBitmap(ImageUtil.bitmapContainer.get(avatar));
        }

        QMUICommonListItemView header = groupListView.createItemView("Display name");
        header.setDetailText(display);
        header.setOrientation(QMUICommonListItemView.HORIZONTAL);
        header.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

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

        QMUICommonListItemView birthItem = groupListView.createItemView("Birth");
        birthItem.setDetailText(TimeUtil.formatBirth(birth));
        birthItem.setOrientation(QMUICommonListItemView.HORIZONTAL);
        birthItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_NONE);

        QMUICommonListItemView sexItem = groupListView.createItemView("Sex");
        sexItem.setDetailText("0".equals(sex) ? "female" : "male");
        sexItem.setOrientation(QMUICommonListItemView.HORIZONTAL);
        sexItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_NONE);

        QMUICommonListItemView signatureItem = groupListView.createItemView("Signature");
        signatureItem.setDetailText(signature);
        signatureItem.setOrientation(QMUICommonListItemView.HORIZONTAL);
        signatureItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_NONE);

        QMUIGroupListView.newSection(this)
                .addItemView(header, v -> {
                    QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(this);
                    QMUIDialogAction cancel = new QMUIDialogAction("Cancel", (dialog, index) -> {
                        dialog.dismiss();
                    });
                    QMUIDialogAction confirm = new QMUIDialogAction("Confirm", (dialog, index) -> {
                        String input = builder.getEditText().getText().toString();
                        header.setDetailText(input);
                        EventBus.getDefault().post(new PatchContactDisplayEvent(LoginActivity.currentUser.getId(), contactId, contactType, input));
                        EventBus.getDefault().post(new ResetCommunicationDisplayEvent(contactId, input));
                        dialog.dismiss();
                    });
                    builder.setTitle("Edit the alias")
                            .setPlaceholder("Set a new alias here")
                            .setDefaultText(display)
                            .addAction(cancel)
                            .addAction(confirm)
                            .show();
                })
                .addItemView(usernameItem, v -> {

                })
                .addItemView(nicknameItem, v -> {

                })
                .addItemView(locationItem, v -> {

                })
                .addItemView(birthItem, v -> {

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
        topBar.addRightImageButton(R.drawable.menu_white, R.id.topbar_right).setOnClickListener(v -> {

        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(PatchContactDisplayEvent event) {
        String url = String.format(URL.PATCH_CONTACT, event.getUserId(), event.getContactId(), event.getContactType());
        RequestBody formBody = new FormBody.Builder()
                .add("display", event.getDisplay())
                .build();
        try {
            NetworkService.patch(url, formBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.send_msg_btn)
    public void onClick() {
        EventBus.getDefault().post(new ClearSignCountEvent(contactId));
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("display", display);
        intent.putExtra("contactId", contactId);
        intent.putExtra("contactType", ContactType.P2P);
        intent.putExtra("avatar", avatar);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
}
