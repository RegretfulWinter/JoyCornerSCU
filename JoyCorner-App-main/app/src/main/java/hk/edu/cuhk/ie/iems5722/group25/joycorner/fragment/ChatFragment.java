package hk.edu.cuhk.ie.iems5722.group25.joycorner.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;
import butterknife.Unbinder;
import cn.jiguang.imui.chatinput.ChatInputView;
import cn.jiguang.imui.chatinput.listener.CameraControllerListener;
import cn.jiguang.imui.chatinput.listener.OnCameraCallbackListener;
import cn.jiguang.imui.chatinput.listener.OnMenuClickListener;
import cn.jiguang.imui.chatinput.listener.RecordVoiceListener;
import cn.jiguang.imui.chatinput.model.FileItem;
import cn.jiguang.imui.chatinput.model.VideoItem;
import cn.jiguang.imui.commons.ImageLoader;
import cn.jiguang.imui.commons.models.IMessage;
import cn.jiguang.imui.messages.MessageList;
import cn.jiguang.imui.messages.MsgListAdapter;
import cn.jiguang.imui.messages.ptr.PtrDefaultHeader;
import cn.jiguang.imui.messages.ptr.PullToRefreshLayout;
import cn.jiguang.imui.utils.DisplayUtil;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.activity.BrowserImageActivity;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.activity.FriendInfoActivity;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.activity.LoginActivity;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.activity.VideoActivity;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.ContactType;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.MsgContentType;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.Status;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.Friend;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.JCMessage;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.JCUser;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.Message;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.User;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ChangeCommunicationEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ClearSignCountEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.LoadFileMessageEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ReceiveNewMessageEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ResetCommunicationDisplayEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.ImageUtil;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.NetworkService;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.TimeUtil;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pub.devrel.easypermissions.EasyPermissions;

public class ChatFragment extends Fragment implements View.OnTouchListener {
    private final int RC_RECORD_VOICE = 0x0001;
    private final int RC_CAMERA = 0x0002;
    private final int RC_PHOTO = 0x0003;

    private Unbinder unbinder;
    private MsgListAdapter<JCMessage> adapter;
    private InputMethodManager imm;
    private Window window;
    final Handler handler = new Handler();
    final Gson gson = new Gson();
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @BindView(R.id.chat_input)
    ChatInputView chatInputView;

    @BindView(R.id.pull_to_refresh_layout)
    PullToRefreshLayout ptrLayout;

    @BindView(R.id.msg_list)
    MessageList msgList;

    @BindView(R.id.top_bar)
    QMUITopBar topBar;

    private final static String TAG = "ChatFragment";
    private String displayName;
    private int contactId;
    private String contactType;
    private String avatar;
    private int pageIndex = 0;
    private final int PAGE_SIZE = 10;
    private final User user = LoginActivity.currentUser;
    private JCUser me;
    private JCUser other;
    private boolean firstInit = true;

    private final ArrayList<String> mPathList = new ArrayList<>();
    private final ArrayList<String> mMsgIdList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_view, container, false);
        unbinder = ButterKnife.bind(this, view);
        displayName = getActivity().getIntent().getStringExtra("display");
        contactId = getActivity().getIntent().getIntExtra("contactId", -1);
        contactType = getActivity().getIntent().getStringExtra("contactType");
        avatar = getActivity().getIntent().getStringExtra("avatar");
        if (LoginActivity.currentUser.getAvatar().equals("/default.jpg")) {
            me = new JCUser(String.valueOf(user.getId()), user.getNickname(), "R.drawable.default_avatar");
        } else {
            me = new JCUser(String.valueOf(user.getId()), user.getNickname(), LoginActivity.currentUser.getAvatar());
        }

        if (ImageUtil.bitmapContainer.get(avatar) != null) {
            other = new JCUser(String.valueOf(contactId), displayName, avatar);
        } else {
            other = new JCUser(String.valueOf(contactId), displayName, "R.drawable.default_avatar");
        }

        initTopBar();
        setup();
        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (chatInputView.getMenuState() == View.VISIBLE) {
                    chatInputView.dismissMenuLayout();
                }
                setChecked(0);
                try {
                    View v = getActivity().getCurrentFocus();
                    if (imm != null && v != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                        view.clearFocus();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case MotionEvent.ACTION_UP:
                view.performClick();
                break;
        }
        return false;
    }

    private void initTopBar() {
        topBar.setTitle(displayName);
        topBar.addLeftBackImageButton().setOnClickListener(v -> {
            EventBus.getDefault().post(new ClearSignCountEvent(contactId));
            getActivity().finish();
        });
    }

    private void setup() {
        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        window = getActivity().getWindow();
        PtrDefaultHeader header = new PtrDefaultHeader(getContext());
        MsgListAdapter.HoldersConfig holdersConfig = new MsgListAdapter.HoldersConfig();
        msgList.setOnTouchListener(this::onTouch);
        final float density = getResources().getDisplayMetrics().density;
        final float MIN_WIDTH = 60 * density;
        final float MAX_WIDTH = 200 * density;
        final float MIN_HEIGHT = 60 * density;
        final float MAX_HEIGHT = 200 * density;
        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadAvatarImage(ImageView avatarImageView, String string) {
                if (string.contains("R.drawable")) {
                    int resId = getResources().getIdentifier(string.replace("R.drawable.", ""),
                            "drawable", getActivity().getPackageName());
                    avatarImageView.setImageResource(resId);
                } else {
                    Glide.with(getContext())
                            .load(string)
                            .apply(new RequestOptions())
                            .placeholder(R.drawable.aurora_headicon_default)
                            .into(avatarImageView);
                }
            }

            @Override
            public void loadImage(ImageView imageView, String string) {
                Glide.with(getContext())
                        .asBitmap()
                        .load(string)
                        .apply(new RequestOptions().fitCenter().placeholder(R.drawable.aurora_picture_not_found))
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                int imageWidth = resource.getWidth();
                                int imageHeight = resource.getHeight();
                                Log.d(TAG, "Image width " + imageWidth + " height: " + imageHeight);

                                float width, height;
                                if (imageWidth > imageHeight) {
                                    if (imageWidth > MAX_WIDTH) {
                                        float temp = MAX_WIDTH / imageWidth * imageHeight;
                                        height = temp > MIN_HEIGHT ? temp : MIN_HEIGHT;
                                        width = MAX_WIDTH;
                                    } else if (imageWidth < MIN_WIDTH) {
                                        float temp = MIN_WIDTH / imageWidth * imageHeight;
                                        height = temp < MAX_HEIGHT ? temp : MAX_HEIGHT;
                                        width = MIN_WIDTH;
                                    } else {
                                        float ratio = imageWidth / imageHeight;
                                        if (ratio > 3) {
                                            ratio = 3;
                                        }
                                        height = imageHeight * ratio;
                                        width = imageWidth;
                                    }
                                } else {
                                    if (imageHeight > MAX_HEIGHT) {
                                        float temp = MAX_HEIGHT / imageHeight * imageWidth;
                                        width = temp > MIN_WIDTH ? temp : MIN_WIDTH;
                                        height = MAX_HEIGHT;
                                    } else if (imageHeight < MIN_HEIGHT) {
                                        float temp = MIN_HEIGHT / imageHeight * imageWidth;
                                        width = temp < MAX_WIDTH ? temp : MAX_WIDTH;
                                        height = MIN_HEIGHT;
                                    } else {
                                        float ratio = imageHeight / imageWidth;
                                        if (ratio > 3) {
                                            ratio = 3;
                                        }
                                        width = imageWidth * ratio;
                                        height = imageHeight;
                                    }
                                }
                                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                                params.width = (int) width;
                                params.height = (int) height;
                                imageView.setLayoutParams(params);
                                Matrix matrix = new Matrix();
                                float scaleWidth = width / imageWidth;
                                float scaleHeight = height / imageHeight;
                                matrix.postScale(scaleWidth, scaleHeight);
                                imageView.setImageBitmap(Bitmap.createBitmap(resource, 0, 0, imageWidth, imageHeight, matrix, true));
                            }
                        });
            }

            @Override
            public void loadVideo(ImageView imageCover, String uri) {
                long interval = 5000 * 1000;
                Glide.with(getContext())
                        .asBitmap()
                        .load(uri)
                        .apply(new RequestOptions().frame(interval).override(200, 400))
                        .into(imageCover);
            }
        };

        adapter = new MsgListAdapter<>(String.valueOf(user.getId()), holdersConfig, imageLoader);
        adapter.setOnMsgClickListener(message -> {
            if (message.getType() == IMessage.MessageType.RECEIVE_VIDEO.ordinal()
                    || message.getType() == IMessage.MessageType.SEND_VIDEO.ordinal()) {
                if (!TextUtils.isEmpty(message.getMediaFilePath())) {
                    Intent intent = new Intent(getActivity(), VideoActivity.class);
                    intent.putExtra(VideoActivity.VIDEO_PATH, message.getMediaFilePath());
                    startActivity(intent);
                }
            } else if (message.getType() == IMessage.MessageType.RECEIVE_IMAGE.ordinal()
                    || message.getType() == IMessage.MessageType.SEND_IMAGE.ordinal()) {
                Intent intent = new Intent(getActivity(), BrowserImageActivity.class);
                intent.putExtra("msgId", message.getMsgId());
                intent.putStringArrayListExtra("pathList", mPathList);
                intent.putStringArrayListExtra("idList", mMsgIdList);
                startActivity(intent);
            } else {
            }
        });

        adapter.setOnAvatarClickListener(message -> {
            if (Integer.parseInt(message.getFromUser().getId()) == user.getId()) {
                return;
            }
            if (contactType.equals(ContactType.P2P)) {
                if (null == ContactFragment.friends) {
                    return;
                }
                for (Friend friend : ContactFragment.friends) {
                    if (friend.getFriendId() == contactId) {
                        Intent intent = new Intent(getActivity(), FriendInfoActivity.class);
                        intent.putExtra("contactId", contactId);
                        intent.putExtra("contactType", contactType);
                        intent.putExtra("avatar", avatar);
                        intent.putExtra("display", displayName);
                        intent.putExtra("username", friend.getFriend().getUsername());
                        intent.putExtra("nickname", friend.getFriend().getNickname());
                        intent.putExtra("location", friend.getFriend().getLocation());
                        intent.putExtra("birth", friend.getFriend().getBirth());
                        intent.putExtra("sex", friend.getFriend().getSex());
                        intent.putExtra("signature", friend.getFriend().getSignature());
                        startActivity(intent);
                        return;
                    }
                }
            } else {
                // GROUP CHAT
            }

        });

        adapter.setMsgLongClickListener((view, message) -> {
        });

        loadMessageEvent();

        int[] colors = getResources().getIntArray(R.array.google_colors);
        header.setColorSchemeColors(colors);
        header.setLayoutParams(new RelativeLayout.LayoutParams(-1, -2));
        header.setPadding(0, DisplayUtil.dp2px(getContext(), 15f), 0,
                DisplayUtil.dp2px(getContext(), 10f));
        header.setPtrFrameLayout(ptrLayout);

        msgList.setHasFixedSize(true);
        msgList.setAdapter(adapter);
        msgList.setShowSenderDisplayName(true);
        msgList.setShowReceiverDisplayName(true);
        ptrLayout.setLoadingMinTime(1000);
        ptrLayout.setDurationToCloseHeader(1500);
        ptrLayout.setHeaderView(header);
        ptrLayout.addPtrUIHandler(header);
        // 下拉刷新时，内容固定，只有 Header 变化
        ptrLayout.setPinContent(true);

        ptrLayout.setPtrHandler(layout -> {
            Log.d("MessageListActivity", "Loading next page");
            loadMessageEvent();
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
                message.setTimeString(sdf.format(new Date()));
                adapter.addToStart(message, true);

                sendMessageEvent(message);
                return true;
            }

            @Override
            public void onSendFiles(List<FileItem> list) {
                if (list == null || list.isEmpty()) {
                    return;
                }

                JCMessage message;
                for (FileItem item : list) {
                    if (item.getType() == FileItem.Type.Image) {
                        message = new JCMessage("", IMessage.MessageType.SEND_IMAGE.ordinal());
                    } else if (item.getType() == FileItem.Type.Video) {
                        message = new JCMessage("", IMessage.MessageType.SEND_VIDEO.ordinal());
                        message.setDuration(((VideoItem) item).getDuration());
                    } else {
                        throw new RuntimeException("Invalid FileItem type. Must be Type.Image or Type.Video");
                    }
                    message.setTimeString(sdf.format(new Date()));
                    message.setMediaFilePath(item.getFilePath());
                    message.setUser(me);

                    mMsgIdList.add(message.getMsgId());
                    mPathList.add(message.getMediaFilePath());

                    JCMessage finalMessage = message;
                    getActivity().runOnUiThread(() -> adapter.addToStart(finalMessage, true));

                    sendMessageEvent(message);
                }
            }

            @Override
            public boolean switchToMicrophoneMode() {
                setChecked(1);
                scrollToBottom();
                String params[] = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (!EasyPermissions.hasPermissions(getContext(), params)) {
                    EasyPermissions.requestPermissions(getActivity(),
                            getResources().getString(R.string.rationale_record_voice), RC_RECORD_VOICE, params);
                }
                return true;
            }

            @Override
            public boolean switchToGalleryMode() {
                setChecked(2);
                scrollToBottom();
                String[] params = {Manifest.permission.READ_EXTERNAL_STORAGE};

                if (!EasyPermissions.hasPermissions(getContext(), params)) {
                    EasyPermissions.requestPermissions(getActivity(),
                            getResources().getString(R.string.rationale_photo), RC_PHOTO, params);
                }
                return true;
            }

            @Override
            public boolean switchToCameraMode() {
                setChecked(3);
                scrollToBottom();
                String[] params = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO};

                if (!EasyPermissions.hasPermissions(getContext(), params)) {
                    EasyPermissions.requestPermissions(getActivity(),
                            getResources().getString(R.string.rationale_camera), RC_CAMERA, params);
                }
                return true;
            }

            @Override
            public boolean switchToEmojiMode() {
                setChecked(4);
                scrollToBottom();
                return true;
            }
        });

        chatInputView.setOnClickEditTextListener(() -> {
            setChecked(0);
            handler.postDelayed(() -> msgList.smoothScrollToPosition(0), 100);
        });

        chatInputView.getRecordVoiceButton().setRecordVoiceListener(new RecordVoiceListener() {
            @Override
            public void onStartRecord() {
                String path = getContext().getFilesDir().getAbsolutePath() + "/voice";
                chatInputView.getRecordVoiceButton().setVoiceFilePath(path, DateFormat.format("yyyy-MM-dd-hhmmss",
                        Calendar.getInstance(Locale.getDefault())).toString());
            }

            @Override
            public void onFinishRecord(File voiceFile, int duration) {
                JCMessage message = new JCMessage("", IMessage.MessageType.SEND_VOICE.ordinal());
                message.setUser(me);
                message.setMediaFilePath(voiceFile.getPath());
                message.setDuration(duration);
                message.setTimeString(sdf.format(new Date()));

                mMsgIdList.add(message.getMsgId());
                mPathList.add(message.getMediaFilePath());

                adapter.addToStart(message, true);
            }

            @Override
            public void onCancelRecord() {

            }

            @Override
            public void onPreviewCancel() {

            }

            @Override
            public void onPreviewSend() {

            }
        });

        chatInputView.setOnCameraCallbackListener(new OnCameraCallbackListener() {
            @Override
            public void onTakePictureCompleted(String photoPath) {
                JCMessage message = new JCMessage("", IMessage.MessageType.SEND_IMAGE.ordinal());
                message.setTimeString(sdf.format(new Date()));
                message.setMediaFilePath(photoPath);
                message.setUser(me);

                mMsgIdList.add(message.getMsgId());
                mPathList.add(message.getMediaFilePath());

                getActivity().runOnUiThread(() -> adapter.addToStart(message, true));
                sendMessageEvent(message);
            }

            @Override
            public void onStartVideoRecord() {

            }

            @Override
            public void onFinishVideoRecord(String videoPath) {
                // 点击发送视频的事件会回调给 onSendFiles，在录制完视频后触发
            }

            @Override
            public void onCancelVideoRecord() {

            }
        });

        chatInputView.setCameraControllerListener(new CameraControllerListener() {
            @Override
            public void onFullScreenClick() {

            }

            @Override
            public void onRecoverScreenClick() {

            }

            @Override
            public void onCloseCameraClick() {

            }

            @Override
            public void onSwitchCameraModeClick(boolean isRecordVideoMode) {
                // 切换拍照与否，通过 isRecordVideoMode 判断
            }
        });
    }

    private void sendMessageEvent(JCMessage jcMessage) {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            emitter.onNext(sendMessage(jcMessage));
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean isOK) {
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
                switch (jcMessage.getType()) {
                    case 1:
                        EventBus.getDefault().post(new ChangeCommunicationEvent(contactId, jcMessage.getText(), new Date()));
                        break;
                    case 3:
                        EventBus.getDefault().post(new ChangeCommunicationEvent(contactId, "[Picture]", new Date()));
                        break;
                    case 5:
                        EventBus.getDefault().post(new ChangeCommunicationEvent(contactId, "[Audio]", new Date()));
                        break;
                    case 7:
                        EventBus.getDefault().post(new ChangeCommunicationEvent(contactId, "[Video]", new Date()));
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private Boolean sendMessage(JCMessage jcMessage) throws IOException {
        switch (jcMessage.getType()) {
            //SEND_TEXT
            case 1:
                Message message = Message.builder()
                        .fromId(user.getId())
                        .destId(contactId)
                        .content(jcMessage.getText())
                        .contentType(MsgContentType.TEXT)
                        .msgType(contactType).build();
                NetworkService.postMessage(message);
                break;
            //SEND_IMAGE
            case 3:
                Message imgMsg = Message.builder()
                        .fromId(user.getId())
                        .destId(contactId)
                        .content(jcMessage.getMediaFilePath())
                        .contentType(MsgContentType.IMAGE)
                        .msgType(contactType).build();
                NetworkService.postMessage(imgMsg);
                break;
            case 5://SEND_VOICE
                Message audMsg = Message.builder()
                        .fromId(user.getId())
                        .destId(contactId)
                        .content(jcMessage.getMediaFilePath())
                        .contentType(MsgContentType.AUDIO)
                        .msgType(contactType).build();
                NetworkService.postMessage(audMsg);
                break;
            case 7://SEND_VIDEO
                Message vdoMsg = Message.builder()
                        .fromId(user.getId())
                        .destId(contactId)
                        .content(jcMessage.getMediaFilePath())
                        .contentType(MsgContentType.VIDEO)
                        .msgType(contactType).build();
                NetworkService.postMessage(vdoMsg);
                break;
        }
        return true;
    }

    private void loadMessageEvent() {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            emitter.onNext(loadNextPage());
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean isOK) {
                ptrLayout.refreshComplete();
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
                if (firstInit) {
                    firstInit = false;
                    msgList.scrollToPosition(0);
                }
            }
        });
    }

    private Boolean loadNextPage() throws IOException, JSONException {

        pageIndex++;
        String url = String.format(URL.GET_MESSAGES, user.getId(), contactId, contactType, pageIndex, PAGE_SIZE);
        String response = NetworkService.get(url);
        JSONObject object = new JSONObject(response);
        String status = object.getString("status");
        if (Status.OK.equals(status)) {
            String data = object.getString("messages");
            List<Message> messages = gson.fromJson(data, new TypeToken<ArrayList<Message>>() {
            }.getType());
            if (messages.size() < 1) {
                pageIndex--;
                return true;
            }
            List<JCMessage> jcMessages = new ArrayList<>();
            for (Message msg : messages) {
                JCUser jcUser = msg.getFromId() == user.getId() ? me : other;

                switch (msg.getContentType()) {
                    case MsgContentType.TEXT:
                        JCMessage jcMessage = new JCMessage(msg.getContent(), msg.getFromId() == user.getId()
                                ? IMessage.MessageType.SEND_TEXT.ordinal() : IMessage.MessageType.RECEIVE_TEXT.ordinal(), jcUser);
                        jcMessage.setTimeString(TimeUtil.getMessageTime(msg.getCreateDate()));
                        jcMessages.add(jcMessage);

                        break;
                    case MsgContentType.IMAGE:
                        JCMessage imageMsg = new JCMessage("", msg.getFromId() == user.getId()
                                ? IMessage.MessageType.SEND_IMAGE.ordinal() : IMessage.MessageType.RECEIVE_IMAGE.ordinal(), jcUser);
                        imageMsg.setTimeString(TimeUtil.getMessageTime(msg.getCreateDate()));
                        imageMsg.setMediaFilePath(msg.getContent());
                        EventBus.getDefault().post(new LoadFileMessageEvent(imageMsg, MsgContentType.IMAGE));

                        jcMessages.add(imageMsg);

                        mMsgIdList.add(imageMsg.getMsgId());
                        mPathList.add(msg.getContent());
                        break;
                    default:
                        break;

                }
            }
            adapter.addToEnd(jcMessages);
        }
        return true;
    }

    private void scrollToBottom() {
        adapter.getLayoutManager().scrollToPosition(0);
    }

    private void setChecked(int flag) {
        switch (flag) {
            case 1:
                chatInputView.getVoiceBtn().setImageDrawable(getResources().getDrawable(R.drawable.default_menuitem_voice_pres));
                chatInputView.getPhotoBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_photo));
                chatInputView.getCameraBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_camera));
                chatInputView.getEmojiBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_emoji));
                break;
            case 2:
                chatInputView.getVoiceBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_mic));
                chatInputView.getPhotoBtn().setImageDrawable(getResources().getDrawable(R.drawable.default_menuitem_photo_pres));
                chatInputView.getCameraBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_camera));
                chatInputView.getEmojiBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_emoji));
                break;
            case 3:
                chatInputView.getVoiceBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_mic));
                chatInputView.getPhotoBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_photo));
                chatInputView.getCameraBtn().setImageDrawable(getResources().getDrawable(R.drawable.default_menuitem_camera_pres));
                chatInputView.getEmojiBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_emoji));
                break;
            case 4:
                chatInputView.getVoiceBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_mic));
                chatInputView.getPhotoBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_photo));
                chatInputView.getCameraBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_camera));
                chatInputView.getEmojiBtn().setImageDrawable(getResources().getDrawable(R.drawable.default_menuitem_emoji_pres));
                break;
            default:
                chatInputView.getVoiceBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_mic));
                chatInputView.getPhotoBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_photo));
                chatInputView.getCameraBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_camera));
                chatInputView.getEmojiBtn().setImageDrawable(getResources().getDrawable(R.drawable.aurora_menuitem_emoji));
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @OnTouch(R.id.msg_list)
    void touchMsgList() {
        QMUIKeyboardHelper.hideKeyboard(chatInputView);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onLoadFileMessageEvent(LoadFileMessageEvent event) {
        File file = new File(event.getMessage().getMediaFilePath());
        if (!file.exists()) {
            try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
                String url = URL.GET_FILE + "/"
                        + (event.getType().equals(MsgContentType.IMAGE) ? "image"
                        : event.getType().equals(MsgContentType.VIDEO) ? "video" : "audio")
                        + "?file_path=" + event.getMessage().getMediaFilePath();
                byte[] response = NetworkService.getFile(url);
                stream.write(response, 0, response.length);
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (event.isNewMessage()) {
            getActivity().runOnUiThread(() -> {
                adapter.addToStart(event.getMessage(), true);
                adapter.notifyDataSetChanged();
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ResetCommunicationDisplayEvent event) {
        displayName = event.getDisplay();
        other.setDisplayName(displayName);
        getActivity().runOnUiThread(() -> topBar.setTitle(displayName));
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ReceiveNewMessageEvent event) {
        String msgType = event.getDataPayload().get("msg_type");
        if (contactType.equals(msgType)) {
            int fromId = Integer.parseInt(event.getDataPayload().get("from_id"));
            if (Math.abs(contactId) == fromId) {
                String contentType = event.getDataPayload().get("content_type");
                if (MsgContentType.TEXT.equals(contentType)) {
                    JCMessage jcMessage = new JCMessage(event.getDataPayload().get("content"), IMessage.MessageType.RECEIVE_TEXT.ordinal());
                    jcMessage.setTimeString(TimeUtil.getMessageTime(event.getDataPayload().get("timestamp")));
                    jcMessage.setUser(other);
                    getActivity().runOnUiThread(() -> {
                        adapter.addToStart(jcMessage, true);
                        adapter.notifyDataSetChanged();
                    });
                } else if (MsgContentType.IMAGE.equals(contentType)) {
                    JCMessage imageMsg = new JCMessage("", IMessage.MessageType.RECEIVE_IMAGE.ordinal(), other);
                    imageMsg.setTimeString(TimeUtil.getMessageTime(event.getDataPayload().get("timestamp")));
                    imageMsg.setMediaFilePath(event.getDataPayload().get("content"));

                    mMsgIdList.add(imageMsg.getMsgId());
                    mPathList.add(event.getDataPayload().get("content"));
                    EventBus.getDefault().post(new LoadFileMessageEvent(imageMsg, MsgContentType.IMAGE, true));
                }
            }
        }
    }

}
