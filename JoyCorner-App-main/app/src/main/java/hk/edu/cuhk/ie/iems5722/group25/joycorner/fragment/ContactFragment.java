package hk.edu.cuhk.ie.iems5722.group25.joycorner.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.activity.FriendInfoActivity;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.activity.FriendRequestActivity;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.activity.LoginActivity;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.ContactType;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.Status;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.Friend;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ClearContactRedDotEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.LoadCommunicationAvatarEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ReceiveFriendConfirmEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ReceiveFriendRequestEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ResetCommunicationDisplayEvent;
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

public class ContactFragment extends BaseFragment {

    private Unbinder unbinder;
    public static List<Friend> friends;
    final Gson gson = new Gson();
    private final ArrayList<String> requestUserIdList = new ArrayList<>();

    QMUICommonListItemView friendRequestItem;
    private ImageView tips;
    @BindView(R.id.group_list_view)
    QMUIGroupListView groupListView;
    private int size;
    private QMUIGroupListView.Section section;
    private Drawable icon;
    private QMUIGroupListView.Section friendRequestSection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_view, container, false);
        unbinder = ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        initUI();
        doInBackground();
        return view;
    }

    private void initUI() {
        initNewFriendRequestItem();
        tips = new ImageView(getContext());
        tips.setImageResource(R.drawable.tipnew);
        size = QMUIDisplayHelper.dp2px(getContext(), 80);
        section = QMUIGroupListView.newSection(getContext());
        icon = ContextCompat.getDrawable(getContext(), R.drawable.default_avatar);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    private void initNewFriendRequestItem() {
        friendRequestItem = groupListView.createItemView("New friend request");
        friendRequestItem.setImageDrawable(getActivity().getDrawable(R.mipmap.friend_add_yellow));
        friendRequestItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        friendRequestSection = QMUIGroupListView.newSection(getContext());
        friendRequestSection.addItemView(friendRequestItem, v -> {
            friendRequestItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
            Intent intent = new Intent(getActivity(), FriendRequestActivity.class);
            intent.putStringArrayListExtra("userIdList", requestUserIdList);
            startActivity(intent);
        }).addTo(groupListView);
    }

    private void doInBackground() {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            emitter.onNext(fetchUserRelationship());
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onNext(@NonNull Boolean isOK) {
                if (isOK) {
                    for (Friend friend : friends) {
                        QMUICommonListItemView item = groupListView.createItemView(icon, friend.getFriend().getUsername(), friend.getNickname(),
                                QMUICommonListItemView.HORIZONTAL, QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON, size);
                        item.setId(friend.getFriendId());
                        Bitmap avatar = ImageUtil.bitmapContainer.get(friend.getFriend().getAvatar());
                        if (avatar != null) {
                            item.setImageDrawable(new BitmapDrawable(getResources(), avatar));
                        } else {
                            EventBus.getDefault().post(new LoadCommunicationAvatarEvent(item, friend.getFriend().getAvatar()));
                        }

                        section.addItemView(item, v -> {
                            Intent intent = new Intent(getActivity(), FriendInfoActivity.class);
                            intent.putExtra("contactId", friend.getFriendId());
                            intent.putExtra("contactType", ContactType.P2P);
                            intent.putExtra("avatar", friend.getFriend().getAvatar());
                            intent.putExtra("display", friend.getNickname());
                            intent.putExtra("username", friend.getFriend().getUsername());
                            intent.putExtra("nickname", friend.getFriend().getNickname());
                            intent.putExtra("location", friend.getFriend().getLocation());
                            intent.putExtra("birth", friend.getFriend().getBirth());
                            intent.putExtra("sex", friend.getFriend().getSex());
                            intent.putExtra("signature", friend.getFriend().getSignature());
                            startActivity(intent);
                        });
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

    private Boolean fetchUserRelationship() throws IOException, JSONException {
        String response = NetworkService.get(URL.GET_USER_FRIENDS + LoginActivity.currentUser.getId());
        JSONObject object = new JSONObject(response);
        if (Status.OK.equals(object.getString("status"))) {
            friends = gson.fromJson(object.getString("friends"), new TypeToken<List<Friend>>() {
            }.getType());
            return friends.size() > 0;
        }
        return false;
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
    public void onEvent(ResetCommunicationDisplayEvent event) {
        if (event.getContactId() > 0) {
            for (Friend friend : friends) {
                if (friend.getFriendId() == event.getContactId()) {
                    friend.setNickname(event.getDisplay());
                    getActivity().runOnUiThread(() -> {
                        QMUICommonListItemView item = groupListView.findViewById(event.getContactId());
                        item.setDetailText(event.getDisplay());
                    });
                    break;
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ReceiveFriendRequestEvent event) {
        requestUserIdList.add(event.getFrom_id());
        getActivity().runOnUiThread(() -> {
            friendRequestItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
            friendRequestItem.addAccessoryCustomView(tips);
        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ReceiveFriendConfirmEvent event) {
        loadNewFriend(event.getFriend(), QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ClearContactRedDotEvent event) {
        requestUserIdList.remove(event.getUserId());
        Friend friend = Friend.builder()
                .friend(event.getUser())
                .userId(LoginActivity.currentUser.getId())
                .friendId(event.getUser().getId())
                .nickname(event.getUser().getNickname())
                .build();
        loadNewFriend(friend, QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
    }

    private void loadNewFriend(Friend friend, int accessoryType) {
        friends.add(friend);
        QMUICommonListItemView item = groupListView.createItemView(icon, friend.getFriend().getUsername(), friend.getNickname(),
                QMUICommonListItemView.HORIZONTAL, accessoryType, size);
        if (accessoryType == QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM) {
            item.addAccessoryCustomView(tips);
        }
        item.setId(friend.getFriendId());
        Bitmap avatar = ImageUtil.bitmapContainer.get(friend.getFriend().getAvatar());
        if (avatar != null) {
            item.setImageDrawable(new BitmapDrawable(getResources(), avatar));
        } else {
            EventBus.getDefault().post(new LoadCommunicationAvatarEvent(item, friend.getFriend().getAvatar()));
        }

        section.addItemView(item, v -> {
            item.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
            Intent intent = new Intent(getActivity(), FriendInfoActivity.class);
            intent.putExtra("contactId", friend.getFriendId());
            intent.putExtra("contactType", ContactType.P2P);
            intent.putExtra("avatar", friend.getFriend().getAvatar());
            intent.putExtra("display", friend.getNickname());
            intent.putExtra("username", friend.getFriend().getUsername());
            intent.putExtra("nickname", friend.getFriend().getNickname());
            intent.putExtra("location", friend.getFriend().getLocation());
            intent.putExtra("birth", friend.getFriend().getBirth());
            intent.putExtra("sex", friend.getFriend().getSex());
            intent.putExtra("signature", friend.getFriend().getSignature());
            startActivity(intent);
        });

        getActivity().runOnUiThread(() -> {
            groupListView.removeAllViews();
            friendRequestSection.addTo(groupListView);
            section.addTo(groupListView);
        });
    }
}
