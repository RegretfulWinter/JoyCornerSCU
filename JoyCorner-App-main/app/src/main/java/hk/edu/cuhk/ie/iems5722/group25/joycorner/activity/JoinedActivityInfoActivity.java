package hk.edu.cuhk.ie.iems5722.group25.joycorner.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.ActivityGameName;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.Status;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.ActivityInfo;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.ImageUtil;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.NetworkService;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class JoinedActivityInfoActivity extends AppCompatActivity {

    private Unbinder unbinder;
    private final Gson gson = new Gson();
    private QMUIGroupListView.Section section;
    @BindView(R.id.top_bar)
    QMUITopBar topBar;

    @BindView(R.id.group_list_view)
    QMUIGroupListView groupListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joined_activity);
        unbinder = ButterKnife.bind(this);
        initTopBar();
        section = QMUIGroupListView.newSection(this);
        initGroupListView();
    }

    private void initGroupListView() {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            emitter.onNext(loadJoinedActivity());
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onNext(@NonNull Boolean success) {
                if (success) {
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

    private Boolean loadJoinedActivity() {
        try {
            String response = NetworkService.get(String.format(URL.GET_ACTIVITY_JOINED, LoginActivity.currentUser.getId()));
            JSONObject object = new JSONObject(response);
            String status = object.getString("status");
            if (Status.OK.equals(status)) {
                List<ActivityInfo> activities = gson.fromJson(object.getString("activities"), new TypeToken<List<ActivityInfo>>() {
                }.getType());
                int size = QMUIDisplayHelper.dp2px(this, 80);
                int padding = QMUIDisplayHelper.dp2px(this, 10);
                for (ActivityInfo activityInfo : activities) {
                    QMUICommonListItemView newItem = groupListView.createItemView(ImageUtil.zoomDrawable(getDrawable(ActivityGameName.getGameDrawable(activityInfo.getActivityType())), size), activityInfo.getActivityType(),
                            activityInfo.getStartTime().substring(0, 10), QMUICommonListItemView.HORIZONTAL, QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON, size);
                    newItem.setPadding(newItem.getPaddingLeft(), newItem.getPaddingTop(), newItem.getPaddingRight(), padding);
                    section.addItemView(newItem, v -> {
                        Intent intent = new Intent(this, DetailActivityInfoActivity.class);
                        intent.putExtra("activityInfo", gson.toJson(activityInfo));
                        startActivity(intent);
                    });
                }
                return true;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initTopBar() {
        topBar.setTitle("Joined Activity");
        topBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
