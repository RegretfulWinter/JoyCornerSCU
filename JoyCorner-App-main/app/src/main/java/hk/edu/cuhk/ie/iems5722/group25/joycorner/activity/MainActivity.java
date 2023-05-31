package hk.edu.cuhk.ie.iems5722.group25.joycorner.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.messaging.FirebaseMessaging;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUIPagerAdapter;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.QMUIViewPager;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopups;
import com.qmuiteam.qmui.widget.tab.QMUIBasicTabSegment;
import com.qmuiteam.qmui.widget.tab.QMUITab;
import com.qmuiteam.qmui.widget.tab.QMUITabBuilder;
import com.qmuiteam.qmui.widget.tab.QMUITabSegment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.ContactType;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ClearContactRedDotEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ClearSignCountEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ReceiveFriendConfirmEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ReceiveFriendRequestEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ReceiveNewMessageEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.UpdateTokenEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.fragment.ActivityFragment;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.fragment.ContactFragment;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.fragment.MeFragment;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.fragment.MessageFragment;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.NetworkService;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.top_bar)
    QMUITopBar topBar;
    @BindView(R.id.tab_segment)
    QMUITabSegment tabSegment;
    @BindView(R.id.view_pager)
    QMUIViewPager viewPager;
    private Unbinder unbinder;
    private SimpleAdapter adapter;
    private SimpleAdapter activityAdapter;
    private QMUIPopup popup;
    private QMUIPopup activityPopup;
    private QMUITab tab1;
    private QMUITab tab2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    EventBus.getDefault().post(new UpdateTokenEvent(task.getResult()));
                });

        initTopBar();

        initTabSegment();

        initPagers();

        initPopup();
    }

    private void initPopup() {
        popup = QMUIPopups.listPopup(getApplicationContext(),
                QMUIDisplayHelper.dp2px(getApplicationContext(), 180),
                QMUIDisplayHelper.dp2px(getApplicationContext(), 100),
                adapter, (parent, view, position, id) -> {
                    switch (position) {
                        case 0:
                            Intent intent = new Intent(MainActivity.this, AddFriendActivity.class);
                            startActivity(intent);
                            break;
                        case 1:
                            Intent botIntent = new Intent(this, ChatterbotActivity.class);
                            botIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(botIntent);
                            break;
                    }
                }).animStyle(QMUIPopup.ANIM_GROW_FROM_CENTER)
                .preferredDirection(QMUIPopup.DIRECTION_BOTTOM)
                .edgeProtection(QMUIDisplayHelper.dp2px(getApplicationContext(), 10))
                .offsetYIfBottom(QMUIDisplayHelper.dp2px(getApplicationContext(), 5))
                .skinManager(QMUISkinManager.defaultInstance(getApplicationContext()))
                .radius(50);

        activityPopup = QMUIPopups.listPopup(getApplicationContext(),
                QMUIDisplayHelper.dp2px(getApplicationContext(), 160),
                QMUIDisplayHelper.dp2px(getApplicationContext(), 100),
                activityAdapter, (parent, view, position, id) -> {
                    switch (position) {
                        case 0:
                            Intent intent = new Intent(MainActivity.this, PostActivityInfoActivity.class);
                            startActivity(intent);
                            break;
                        case 1:
                            Intent joinedIntent = new Intent(MainActivity.this, JoinedActivityInfoActivity.class);
                            startActivity(joinedIntent);
                            break;
                    }
                }).animStyle(QMUIPopup.ANIM_GROW_FROM_CENTER)
                .preferredDirection(QMUIPopup.DIRECTION_BOTTOM)
                .edgeProtection(QMUIDisplayHelper.dp2px(getApplicationContext(), 10))
                .offsetYIfBottom(QMUIDisplayHelper.dp2px(getApplicationContext(), 5))
                .skinManager(QMUISkinManager.defaultInstance(getApplicationContext()))
                .radius(50);
    }

    private void initTopBar() {
        topBar.setTitle("Joy Corner");
//        List<String> data = Arrays.asList("Add Contacts", "New Chat", "Scan", "Money", "Chatterbot");
//        List<Integer> imageId = Arrays.asList(R.drawable.friendadd_white, R.drawable.chat_white, R.drawable.scan_white, R.drawable.dollar, R.drawable.robot_white);

        List<String> data = Arrays.asList("Add Contacts", "Chatterbot");
        List<Integer> imageId = Arrays.asList(R.drawable.friendadd_white, R.drawable.robot_white);
        List<Map<String, Object>> listItems = new ArrayList<>();
        for (int i = 0; i < imageId.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("image", imageId.get(i));
            map.put("title", data.get(i));
            listItems.add(map);
        }
        adapter = new SimpleAdapter(getApplicationContext(), listItems, R.layout.simple_list_item, new String[]{"title", "image"}, new int[]{R.id.textview, R.id.imageview});

        List<String> activityData = Arrays.asList("Post Activity", "Joined");
        List<Integer> activityImageId = Arrays.asList(R.drawable.post, R.drawable.date);
        List<Map<String, Object>> activityListItems = new ArrayList<>();
        for (int i = 0; i < activityImageId.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("image", activityImageId.get(i));
            map.put("title", activityData.get(i));
            activityListItems.add(map);
        }
        activityAdapter = new SimpleAdapter(getApplicationContext(), activityListItems, R.layout.simple_list_item, new String[]{"title", "image"}, new int[]{R.id.textview, R.id.imageview});

    }

    private void initTabSegment() {
        int iconShowSize = QMUIDisplayHelper.dp2px(this, 20);
        Drawable message_normal = ContextCompat.getDrawable(this, R.mipmap.message_normal);
        message_normal.setBounds(0, 0, iconShowSize, iconShowSize);
        Drawable message_selected = ContextCompat.getDrawable(this, R.mipmap.message_selected);
        message_selected.setBounds(0, 0, iconShowSize, iconShowSize);

        Drawable contact_normal = ContextCompat.getDrawable(this, R.mipmap.contact_normal);
        contact_normal.setBounds(0, 0, iconShowSize, iconShowSize);
        Drawable contact_selected = ContextCompat.getDrawable(this, R.mipmap.contact_selected);
        contact_selected.setBounds(0, 0, iconShowSize, iconShowSize);

        Drawable activity_normal = ContextCompat.getDrawable(this, R.mipmap.activity_normal);
        activity_normal.setBounds(0, 0, iconShowSize, iconShowSize);
        Drawable activity_selected = ContextCompat.getDrawable(this, R.mipmap.activity_selected);
        activity_selected.setBounds(0, 0, iconShowSize, iconShowSize);

        Drawable me_normal = ContextCompat.getDrawable(this, R.mipmap.me_normal);
        me_normal.setBounds(0, 0, iconShowSize, iconShowSize);
        Drawable me_selected = ContextCompat.getDrawable(this, R.mipmap.me_selected);
        me_selected.setBounds(0, 0, iconShowSize, iconShowSize);

        QMUITabBuilder builder = tabSegment.tabBuilder();
        int normalColor = QMUIResHelper.getAttrColor(this, R.attr.qmui_config_color_gray_6);
        int selectColor = QMUIResHelper.getAttrColor(this, R.attr.qmui_config_color_blue);
        builder.skinChangeWithTintColor(false)
                .setNormalColor(normalColor)
                .setSelectColor(selectColor)
                .setDynamicChangeIconColor(false)
                .setNormalIconSizeInfo(iconShowSize, iconShowSize)
                .setAllowIconDrawOutside(false);

        tab1 = builder.setText("Chats")
                .setNormalDrawable(message_normal)
                .setSelectedDrawable(message_selected).build(this);
        tab2 = builder.setText("Contacts")
                .setNormalDrawable(contact_normal)
                .setSelectedDrawable(contact_selected).build(this);
        QMUITab tab3 = builder.setText("Activity")
                .setNormalDrawable(activity_normal)
                .setSelectedDrawable(activity_selected).build(this);
        QMUITab tab4 = builder.setText("Me")
                .setNormalDrawable(me_normal)
                .setSelectedDrawable(me_selected).build(this);

        tabSegment.addTab(tab1).addTab(tab2).addTab(tab3).addTab(tab4);
        tabSegment.setMode(QMUITabSegment.MODE_FIXED);
        tabSegment.addOnTabSelectedListener(new QMUIBasicTabSegment.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int index) {
                switch (index) {
                    case 1:
                        topBar.removeAllRightViews();
                        topBar.addRightImageButton(R.drawable.friendadd_white_small, R.id.topbar_right).setOnClickListener(v -> {
                            Intent intent = new Intent(MainActivity.this, AddFriendActivity.class);
                            startActivity(intent);
                        });
                        runOnUiThread(() -> {
                            tab2.clearSignCountOrRedPoint();
                            tabSegment.notifyDataChanged();
                        });
                        break;
                    case 2:
                        topBar.removeAllRightViews();
                        topBar.addRightImageButton(R.drawable.add_white_small, R.id.topbar_right).setOnClickListener(v -> {
                            activityPopup.show(v);
                        });
                        break;
                    case 3:
                        topBar.removeAllRightViews();
                        break;
                    default:
                        topBar.removeAllRightViews();
                        topBar.addRightImageButton(R.drawable.add_white_small, R.id.topbar_right).setOnClickListener(v -> {
                            popup.show(v);
                        });
                }

            }

            @Override
            public void onTabUnselected(int index) {
            }

            @Override
            public void onTabReselected(int index) {
            }

            @Override
            public void onDoubleTap(int index) {
            }
        });
    }

    private void initPagers() {
        QMUIPagerAdapter pagerAdapter = new QMUIPagerAdapter() {
            private FragmentTransaction mCurrentTransaction;
            private Fragment mCurrentPrimaryItem = null;

            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public boolean isViewFromObject(@androidx.annotation.NonNull View view, @androidx.annotation.NonNull Object object) {
                return view == ((Fragment) object).getView();
            }

            @androidx.annotation.NonNull
            @Override
            protected Object hydrate(@androidx.annotation.NonNull ViewGroup container, int position) {
                String name = makeFragmentName(container.getId(), position);
                if (mCurrentTransaction == null) {
                    mCurrentTransaction = getSupportFragmentManager()
                            .beginTransaction();
                }
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(name);
                if (fragment != null) {
                    return fragment;
                }
                switch (position) {
                    case 1:
                        return new ContactFragment();
                    case 2:
                        return new ActivityFragment();
                    case 3:
                        return new MeFragment();
                    default:
                        return new MessageFragment();
                }
            }

            @Override
            protected void populate(@androidx.annotation.NonNull ViewGroup container, @androidx.annotation.NonNull Object item, int position) {
                String name = makeFragmentName(container.getId(), position);
                if (mCurrentTransaction == null) {
                    mCurrentTransaction = getSupportFragmentManager()
                            .beginTransaction();
                }
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(name);
                if (fragment != null) {
                    mCurrentTransaction.attach(fragment);
                    if (fragment.getView() != null && fragment.getView().getWidth() == 0) {
                        fragment.getView().requestLayout();
                    }
                } else {
                    fragment = (Fragment) item;
                    mCurrentTransaction.add(container.getId(), fragment, name);
                }
                if (fragment != mCurrentPrimaryItem) {
                    fragment.setMenuVisibility(false);
                    fragment.setUserVisibleHint(false);
                }
            }

            @Override
            protected void destroy(@androidx.annotation.NonNull ViewGroup container, int position, @androidx.annotation.NonNull Object object) {
                if (mCurrentTransaction == null) {
                    mCurrentTransaction = getSupportFragmentManager()
                            .beginTransaction();
                }
                mCurrentTransaction.detach((Fragment) object);
            }

            @Override
            public void startUpdate(ViewGroup container) {
                if (container.getId() == View.NO_ID) {
                    throw new IllegalStateException("ViewPager with adapter " + this
                            + " requires a view id");
                }
            }

            @Override
            public void finishUpdate(ViewGroup container) {
                if (mCurrentTransaction != null) {
                    mCurrentTransaction.commitNowAllowingStateLoss();
                    mCurrentTransaction = null;
                }
            }

            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                Fragment fragment = (Fragment) object;
                if (fragment != mCurrentPrimaryItem) {
                    if (mCurrentPrimaryItem != null) {
                        mCurrentPrimaryItem.setMenuVisibility(false);
                        mCurrentPrimaryItem.setUserVisibleHint(false);
                    }
                    if (fragment != null) {
                        fragment.setMenuVisibility(true);
                        fragment.setUserVisibleHint(true);
                    }
                    mCurrentPrimaryItem = fragment;
                }
            }

            private String makeFragmentName(int viewId, long id) {
                return MainActivity.class.getSimpleName() + ":" + viewId + ":" + id;
            }
        };
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(pageChangeListener);
        tabSegment.setupWithViewPager(viewPager, false);
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        popup.dismiss();
        activityPopup.dismiss();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ClearSignCountEvent event) {
        Integer count = counter.get(event.getContactId());
        if (null == count) return;
        counter.put(event.getContactId(), 0);
        runOnUiThread(() -> {
            tab1.setSignCount(tab1.getSignCount() - count);
            tabSegment.notifyDataChanged();
        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ClearContactRedDotEvent event) {
        runOnUiThread(() -> {
            tab2.clearSignCountOrRedPoint();
            tabSegment.notifyDataChanged();
        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(UpdateTokenEvent event) {
        RequestBody body = new FormBody.Builder()
                .add("user_id", String.valueOf(LoginActivity.currentUser.getId()))
                .add("token", String.valueOf(event.getToken()))
                .build();
        try {
            NetworkService.post(URL.POST_FCM_TOKEN, body);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<Integer, Integer> counter = new HashMap<>();

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ReceiveNewMessageEvent event) {
        String contentType = event.getDataPayload().get("content_type");
        int fromId = Integer.parseInt(event.getDataPayload().get("from_id"));
        if (ContactType.GROUP.equals(contentType)) {
            fromId = -1 * fromId;
        }
        Integer count = counter.get(fromId);
        if (null == count) {
            counter.put(fromId, 1);
        } else {
            counter.put(fromId, count + 1);
        }
        runOnUiThread(() -> {
            tab1.setSignCount(tab1.getSignCount() + 1);
            tabSegment.notifyDataChanged();
        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ReceiveFriendRequestEvent event) {
        runOnUiThread(() -> {
            tab2.setRedPoint();
            tabSegment.notifyDataChanged();
        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ReceiveFriendConfirmEvent event) {
        runOnUiThread(() -> {
            tab2.setRedPoint();
            tabSegment.notifyDataChanged();
        });
    }
}
