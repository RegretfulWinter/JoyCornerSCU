package hk.edu.cuhk.ie.iems5722.group25.joycorner.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.jiguang.imui.chatinput.ChatInputView;
import cn.jiguang.imui.chatinput.listener.OnMenuClickListener;
import cn.jiguang.imui.chatinput.model.FileItem;
import cn.jiguang.imui.commons.ImageLoader;
import cn.jiguang.imui.commons.models.IMessage;
import cn.jiguang.imui.messages.MessageList;
import cn.jiguang.imui.messages.MsgListAdapter;
import cn.jiguang.imui.messages.ptr.PtrDefaultHeader;
import cn.jiguang.imui.messages.ptr.PullToRefreshLayout;
import cn.jiguang.imui.utils.DisplayUtil;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.Status;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.JCMessage;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.JCUser;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.User;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.NetworkService;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.TimeUtil;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ChatterbotActivity extends AppCompatActivity {

    private Unbinder unbinder;
    private MsgListAdapter<JCMessage> adapter;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final User user = LoginActivity.currentUser;
    private JCUser me;
    private JCUser bot;

    @BindView(R.id.chat_input)
    ChatInputView chatInputView;

    @BindView(R.id.pull_to_refresh_layout)
    PullToRefreshLayout ptrLayout;

    @BindView(R.id.msg_list)
    MessageList msgList;

    @BindView(R.id.top_bar)
    QMUITopBar topBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_view);
        unbinder = ButterKnife.bind(this);
        if (LoginActivity.currentUser.getAvatar().equals("/default.jpg")) {
            me = new JCUser(String.valueOf(user.getId()), user.getNickname(), "R.drawable.default_avatar");
        } else {
            me = new JCUser(String.valueOf(user.getId()), user.getNickname(), LoginActivity.currentUser.getAvatar());
        }
        bot = new JCUser("bot", "Johnny", "R.drawable.robot_blue");
        initTopBar();
        setup();
    }

    private void initTopBar() {
        topBar.setTitle("Johnny");
        topBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setup() {
        PtrDefaultHeader header = new PtrDefaultHeader(this);
        MsgListAdapter.HoldersConfig holdersConfig = new MsgListAdapter.HoldersConfig();
        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadAvatarImage(ImageView avatarImageView, String string) {
                if (string.contains("R.drawable")) {
                    int resId = getResources().getIdentifier(string.replace("R.drawable.", ""),
                            "drawable", getPackageName());
                    avatarImageView.setImageResource(resId);
                } else {
                    Glide.with(ChatterbotActivity.this)
                            .load(string)
                            .apply(new RequestOptions())
                            .placeholder(R.drawable.aurora_headicon_default)
                            .into(avatarImageView);
                }
            }

            @Override
            public void loadImage(ImageView imageView, String string) {
            }

            @Override
            public void loadVideo(ImageView imageCover, String uri) {
            }
        };
        adapter = new MsgListAdapter<>(String.valueOf(user.getId()), holdersConfig, imageLoader);

        int[] colors = getResources().getIntArray(R.array.google_colors);
        header.setColorSchemeColors(colors);
        header.setLayoutParams(new RelativeLayout.LayoutParams(-1, -2));
        header.setPadding(0, DisplayUtil.dp2px(this, 15f), 0,
                DisplayUtil.dp2px(this, 10f));
        header.setPtrFrameLayout(ptrLayout);

        msgList.setHasFixedSize(true);
        msgList.setAdapter(adapter);
        msgList.setShowSenderDisplayName(true);
        msgList.setShowReceiverDisplayName(true);
        msgList.setOnTouchListener((v, event) -> {
            QMUIKeyboardHelper.hideKeyboard(chatInputView);
            return v.onTouchEvent(event);
        });
        ptrLayout.setLoadingMinTime(1000);
        ptrLayout.setDurationToCloseHeader(1500);
        ptrLayout.setHeaderView(header);
        ptrLayout.addPtrUIHandler(header);
        // 下拉刷新时，内容固定，只有 Header 变化
        ptrLayout.setPinContent(true);

        ptrLayout.setPtrHandler(layout -> {
        });

        chatInputView.setMenuContainerHeight(chatInputView.getSoftKeyboardHeight());
        chatInputView.setMenuClickListener(new OnMenuClickListener() {
            @Override
            public boolean onSendTextMessage(CharSequence input) {
                if (input.length() <= 0) {
                    return false;
                }
                JCMessage message = new JCMessage(input.toString(), IMessage.MessageType.SEND_TEXT.ordinal());
                message.setUser(me);
                message.setTimeString(TimeUtil.getTimeStringAutoShort(new Date(), true));
                adapter.addToStart(message, true);

                getChatterbotResponse(message.getText());
                return true;
            }

            @Override
            public void onSendFiles(List<FileItem> list) {
            }

            @Override
            public boolean switchToMicrophoneMode() {
                return true;
            }

            @Override
            public boolean switchToGalleryMode() {
                return true;
            }

            @Override
            public boolean switchToCameraMode() {
                return true;
            }

            @Override
            public boolean switchToEmojiMode() {
                return true;
            }
        });

        chatInputView.setOnClickEditTextListener(() -> {
        });


    }

    private void getChatterbotResponse(String text) {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            emitter.onNext(askBot(text));
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onNext(@NonNull Boolean alreadyLogin) {

            }

            @Override
            public void onError(@NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private Boolean askBot(String text) {
        try {
            String response = NetworkService.get(String.format(URL.GET_BOT_RES, text));
            JSONObject object = new JSONObject(response);
            String status = object.getString("status");
            if (Status.OK.equals(status)) {
                String res = object.getString("message");
                JCMessage jcMessage = new JCMessage(res, IMessage.MessageType.RECEIVE_TEXT.ordinal());
                jcMessage.setTimeString(TimeUtil.getTimeStringAutoShort(new Date(), true));
                jcMessage.setUser(bot);
                runOnUiThread(() -> {
                    adapter.addToStart(jcMessage, true);
                    adapter.notifyDataSetChanged();
                });
                return true;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

}
