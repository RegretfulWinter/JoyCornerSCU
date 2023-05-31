package hk.edu.cuhk.ie.iems5722.group25.joycorner.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.activity.ChatActivity;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.activity.LoginActivity;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.ContactType;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.Status;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.Communication;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.Friend;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.User;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ChangeCommunicationEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ClearSignCountEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.LoadCommunicationAvatarEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ReceiveNewMessageEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ResetCommunicationDisplayEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.SetItemAvatarEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.ImageUtil;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.NetworkService;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.TimeUtil;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MessageFragment extends BaseFragment {
    private Unbinder unbinder;
    final Gson gson = new Gson();
    private boolean isInit = false;
    private static List<Communication> communications;
    private ImageView tips;
    private int size;
    private QMUIGroupListView.Section section;
    private Drawable icon;

    @BindView(R.id.group_list_view)
    QMUIGroupListView groupListView;

    private final Map<Integer, String> eventBox = new HashMap<>(4);
    private final Map<Integer, String> displayBox = new HashMap<>(4);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_view, container, false);
        unbinder = ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        doInBackground();
        isInit = true;
        size = QMUIDisplayHelper.dp2px(getContext(), 80);
        section = QMUIGroupListView.newSection(getContext());
        icon = ContextCompat.getDrawable(getContext(), R.drawable.default_avatar);
        tips = new ImageView(getContext());
        tips.setImageResource(R.drawable.tipnew);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        for (Integer id : eventBox.keySet()) {
            QMUICommonListItemView item = groupListView.findViewById(id);
            if (item != null) {
                item.setDetailText(eventBox.get(id));
            } else {
                for (Friend friend : ContactFragment.friends) {
                    if (friend.getFriendId() == id) {
                        QMUICommonListItemView newItem = groupListView.createItemView(icon, friend.getNickname(),
                                eventBox.get(id),
                                QMUICommonListItemView.VERTICAL, QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM, size);
                        newItem.addAccessoryCustomView(tips);
                        newItem.setId(id);
                        section.addItemView(newItem, v -> {
                            newItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
                            EventBus.getDefault().post(new ClearSignCountEvent(friend.getFriendId()));
                            Intent intent = new Intent(getContext(), ChatActivity.class);
                            intent.putExtra("display", friend.getNickname());
                            intent.putExtra("contactId", friend.getFriendId());
                            intent.putExtra("contactType", id > 0 ? ContactType.P2P : ContactType.GROUP);
                            intent.putExtra("avatar", friend.getFriend().getAvatar());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                        });

                        EventBus.getDefault().post(new LoadCommunicationAvatarEvent(newItem, friend.getFriend().getAvatar()));
                        getActivity().runOnUiThread(() -> {
                            groupListView.removeAllViews();
                            section.addTo(groupListView);
                        });
                        break;
                    }
                }
            }
        }
        eventBox.clear();

        for (Integer id : displayBox.keySet()) {
            QMUICommonListItemView item = groupListView.findViewById(id);
            if (null == item) {
                break;
            }
            item.setText(displayBox.get(id));
            for (Communication communication : communications) {
                if (communication.getContactId() == id) {
                    communication.setRemark(displayBox.get(id));
                    break;
                }
            }
        }
        displayBox.clear();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ChangeCommunicationEvent event) {
        String message;
        if (event.getText().contains("\n")) {
            message = event.getText().replaceAll("\n", " ");
        } else {
            message = event.getText();
        }
        if (message.length() >= 35) {
            message = message.substring(0, 35) + "...";
        }
        eventBox.put(event.getContactId(), message + "\n" + TimeUtil.getWhen(event.getDate()));
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ResetCommunicationDisplayEvent event) {
        displayBox.put(event.getContactId(), event.getDisplay());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    private void doInBackground() {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            emitter.onNext(fetchCommunications());
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onNext(@NonNull Boolean isOK) {
                if (isOK) {
                    for (Communication communication : communications) {
                        String message;
                        if (communication.getLatestMessage().contains("\n")) {
                            message = communication.getLatestMessage().replaceAll("\n", " ");
                        } else {
                            message = communication.getLatestMessage();
                        }
                        if (message.length() >= 35) {
                            message = message.substring(0, 35) + "...";
                        }
                        QMUICommonListItemView item = groupListView.createItemView(icon, communication.getRemark(),
                                message + "\n" + TimeUtil.getWhen(communication.getWhen()),
                                QMUICommonListItemView.VERTICAL, QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON, size);
                        item.setId(communication.getContactId());
                        section.addItemView(item, v -> {
                            item.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
                            EventBus.getDefault().post(new ClearSignCountEvent(communication.getContactId()));
                            Intent intent = new Intent(getContext(), ChatActivity.class);
                            intent.putExtra("display", communication.getRemark());
                            intent.putExtra("contactId", communication.getContactId());
                            intent.putExtra("contactType", communication.getContactType());
                            intent.putExtra("avatar", communication.getAvatar());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                        });

                        EventBus.getDefault().post(new LoadCommunicationAvatarEvent(item, communication.getAvatar()));
                    }
                    section.addTo(groupListView);
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

    private Boolean fetchCommunications() throws IOException, JSONException {
        User currentUser = LoginActivity.currentUser;
        String response = NetworkService.get(URL.GET_USER_COMMUNICATIONS + currentUser.getId());
        JSONObject object = new JSONObject(response);
        String status = object.getString("status");
        if (Status.OK.equals(status)) {
            String data = object.getString("communications");
            communications = gson.fromJson(data, new TypeToken<ArrayList<Communication>>() {
            }.getType());
            return communications.size() > 0;
        }
        return false;
    }

    @Override
    protected void hiddenChange(boolean hidden) {
        super.hiddenChange(hidden);
        if (!hidden) {
            if (isInit) {
                Log.d("LoadMessage", "hiddenChange");
            }
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

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ReceiveNewMessageEvent event) {
        String contentType = event.getDataPayload().get("content_type");
        int fromId = Integer.parseInt(event.getDataPayload().get("from_id"));
        if (ContactType.GROUP.equals(contentType)) {
            fromId = -1 * fromId;
        }
        QMUICommonListItemView item = groupListView.findViewById(fromId);
        if (item != null) {
            getActivity().runOnUiThread(() -> {
                item.setDetailText(event.getDataPayload().get("display_msg")
                        + "\n" + TimeUtil.getWhen(event.getDataPayload().get("timestamp")));
                item.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
                item.addAccessoryCustomView(tips);
            });
        } else {
            for (Friend friend : ContactFragment.friends) {
                if (friend.getFriendId() == fromId) {
                    QMUICommonListItemView newItem = groupListView.createItemView(icon, friend.getNickname(),
                            event.getDataPayload().get("display_msg")
                                    + "\n" + TimeUtil.getWhen(event.getDataPayload().get("timestamp")),
                            QMUICommonListItemView.VERTICAL, QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM, size);
                    newItem.addAccessoryCustomView(tips);
                    newItem.setId(fromId);
                    section.addItemView(newItem, v -> {
                        newItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
                        EventBus.getDefault().post(new ClearSignCountEvent(friend.getFriendId()));
                        Intent intent = new Intent(getContext(), ChatActivity.class);
                        intent.putExtra("display", friend.getNickname());
                        intent.putExtra("contactId", friend.getFriendId());
                        intent.putExtra("contactType", event.getDataPayload().get("msg_type"));
                        intent.putExtra("avatar", friend.getFriend().getAvatar());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    });

                    EventBus.getDefault().post(new LoadCommunicationAvatarEvent(newItem, friend.getFriend().getAvatar()));
                    getActivity().runOnUiThread(() -> {
                        groupListView.removeAllViews();
                        section.addTo(groupListView);
                    });
                    break;
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ClearSignCountEvent event) {
        QMUICommonListItemView item = groupListView.findViewById(event.getContactId());
        if (item == null) return;
        getActivity().runOnUiThread(() -> item.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON));
    }
}
