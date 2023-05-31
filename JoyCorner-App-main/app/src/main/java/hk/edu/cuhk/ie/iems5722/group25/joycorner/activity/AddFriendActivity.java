package hk.edu.cuhk.ie.iems5722.group25.joycorner.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.Status;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.User;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.LoadCommunicationAvatarEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.LoadSearchUserEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.SearchUserEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.SetItemAvatarEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.ImageUtil;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.NetworkService;

public class AddFriendActivity extends AppCompatActivity {

    @BindView(R.id.top_bar)
    QMUITopBar topBar;

    @BindView(R.id.search)
    EditText search;

    @BindView(R.id.group_list_view)
    QMUIGroupListView groupListView;

    private Unbinder unbinder;
    private final Gson gson = new Gson();
    private final int SEARCH_USER = 1;
    private int size;
    private QMUIGroupListView.Section section;
    private Drawable icon;

    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == SEARCH_USER) {
                String condition = msg.obj.toString();
                if (condition.equals(search.getText().toString())) {
                    EventBus.getDefault().post(new SearchUserEvent(condition));
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        unbinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initTopBar();
        search.addTextChangedListener(new SearchWatcher());
        section = QMUIGroupListView.newSection(this);
        icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.default_avatar);
        size = QMUIDisplayHelper.dp2px(this, 80);
    }

    private void initTopBar() {
        topBar.setTitle("Add friends");
        topBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    private class SearchWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 3 && s.toString().trim().length() > 3) {
                Message message = Message.obtain();
                message.what = SEARCH_USER;
                message.obj = s.toString();
                handler.sendMessageDelayed(message, 1000);
            } else {
                groupListView.removeAllViews();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(SearchUserEvent event) {
        try {
            String response = NetworkService.get(String.format(URL.GET_USER_BY_CONDITION, event.getCondition()));
            JSONObject object = new JSONObject(response);
            String status = object.getString("status");
            if (Status.OK.equals(status)) {
                String data = object.getString("people");
                List<User> people = gson.fromJson(data, new TypeToken<ArrayList<User>>() {
                }.getType());
                EventBus.getDefault().post(new LoadSearchUserEvent(people));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(LoadCommunicationAvatarEvent event) {
        String avatarPath = event.getAvatar();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoadSearchUserEvent event) {
        for (User person : event.getPeople()) {
            QMUICommonListItemView item = groupListView.createItemView(icon, person.getUsername(), person.getNickname(),
                    QMUICommonListItemView.HORIZONTAL, QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON, size);
            item.setId(person.getId());
            if ("/default.jpg".equals(person.getAvatar())) {
                item.setImageDrawable(getResources().getDrawable(R.drawable.default_avatar));
            } else {
                Bitmap avatar = ImageUtil.bitmapContainer.get(person.getAvatar());
                if (avatar != null) {
                    item.setImageDrawable(new BitmapDrawable(getResources(), avatar));
                } else {
                    EventBus.getDefault().post(new LoadCommunicationAvatarEvent(item, person.getAvatar()));
                }
            }
            section.addItemView(item, v -> {
                Intent intent = new Intent(AddFriendActivity.this, StrangerInfoActivity.class);
                intent.putExtra("userId", person.getId());
                intent.putExtra("avatar", person.getAvatar());
                intent.putExtra("username", person.getUsername());
                intent.putExtra("nickname", person.getNickname());
                intent.putExtra("location", person.getLocation());
                intent.putExtra("sex", person.getSex());
                intent.putExtra("signature", person.getSignature());
                startActivity(intent);
            });
        }

        runOnUiThread(() -> {
            groupListView.removeAllViews();
            section.addTo(groupListView);
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SetItemAvatarEvent event) {
        event.getItem().setImageDrawable(new BitmapDrawable(getResources(), event.getBitmap()));
    }

}
