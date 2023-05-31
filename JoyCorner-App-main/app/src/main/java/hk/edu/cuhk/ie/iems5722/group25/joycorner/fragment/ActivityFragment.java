package hk.edu.cuhk.ie.iems5722.group25.joycorner.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.footer.BallPulseView;
import com.lcodecore.tkrefreshlayout.header.GoogleDotView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.activity.DetailActivityInfoActivity;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.adapter.ActivityAdapter;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.Status;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.ActivityInfo;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.LoadMoreActivitiesEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.PostActivityInfoSuccessEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.RefreshActivitiesEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.NetworkService;


public class ActivityFragment extends BaseFragment {

    private Unbinder unbinder;
    private final Gson gson = new Gson();
    private ActivityAdapter mAdapter;
    private List<ActivityInfo> mActivityInfoList = new ArrayList<>();
    private final int PAGE_SIZE = 8;

    @BindView(R.id.refreshLayout)
    TwinklingRefreshLayout refreshLayout;

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_view, container, false);

        unbinder = ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);

        initRefreshLayout();
        initRecyclerView();

        return view;
    }

    private void initRecyclerView() {
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mAdapter = new ActivityAdapter(getContext(), mActivityInfoList);
        mAdapter.setOnItemClickListener(position -> {
            ActivityInfo activityInfo = mActivityInfoList.get(position);
            Intent intent = new Intent(getContext(), DetailActivityInfoActivity.class);
            intent.putExtra("activityInfo", gson.toJson(activityInfo));
            startActivity(intent);
        });
        recyclerView.setAdapter(mAdapter);
    }

    private void initRefreshLayout() {
        refreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(final TwinklingRefreshLayout refreshLayout) {
                new Handler().postDelayed(() -> {
                    EventBus.getDefault().post(new RefreshActivitiesEvent());
                }, 2000);
            }

            @Override
            public void onLoadMore(final TwinklingRefreshLayout refreshLayout) {
                new Handler().postDelayed(() -> {
                    EventBus.getDefault().post(new LoadMoreActivitiesEvent());
                }, 2000);
            }
        });

        refreshLayout.setHeaderView(new GoogleDotView(getContext()));
        refreshLayout.setBottomView(new BallPulseView(getContext()));
        refreshLayout.setEnableOverScroll(true);
        refreshLayout.setTargetView(recyclerView);

        refreshLayout.startRefresh();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(PostActivityInfoSuccessEvent event) {
        mActivityInfoList.add(0, event.getActivityInfo());
        getActivity().runOnUiThread(() -> {
            mAdapter.notifyItemInserted(0);
            mAdapter.notifyItemRangeChanged(0, mActivityInfoList.size());
            recyclerView.smoothScrollToPosition(0);
        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(LoadMoreActivitiesEvent event) {
        int minIndex = mActivityInfoList.size() > 0 ? mActivityInfoList.get(mActivityInfoList.size() - 1).getId() : 0;
        try {
            String response = NetworkService.get(String.format(URL.GET_ACTIVITY_HISTORY, minIndex, PAGE_SIZE));
            JSONObject object = new JSONObject(response);
            if (Status.OK.equals(object.getString("status"))) {
                List<ActivityInfo> activities = gson.fromJson(object.getString("activities"), new TypeToken<List<ActivityInfo>>() {
                }.getType());
                if (null != activities && activities.size() > 0) {
                    int cap = mActivityInfoList.size();
                    mActivityInfoList.addAll(activities);
                    getActivity().runOnUiThread(() -> {
                        mAdapter.notifyItemRangeInserted(cap, mActivityInfoList.size());
                        refreshLayout.finishLoadmore();
                    });
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            getActivity().runOnUiThread(() -> refreshLayout.finishLoadmore());
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(RefreshActivitiesEvent event) {
        int maxIndex = mActivityInfoList.size() > 0 ? mActivityInfoList.get(0).getId() : 0;
        try {
            String response = NetworkService.get(String.format(URL.GET_ACTIVITY_NEW, maxIndex, PAGE_SIZE));
            JSONObject object = new JSONObject(response);
            if (Status.OK.equals(object.getString("status"))) {
                List<ActivityInfo> activities = gson.fromJson(object.getString("activities"), new TypeToken<List<ActivityInfo>>() {
                }.getType());
                if (null != activities && activities.size() > 0) {
                    int newItemCount = activities.size();
                    int oldItemCount = mActivityInfoList.size();
                    activities.addAll(mActivityInfoList);
                    mActivityInfoList.clear();
                    mActivityInfoList.addAll(activities);
                    getActivity().runOnUiThread(() -> {
                        mAdapter.notifyItemRangeInserted(0, newItemCount);
                        mAdapter.notifyItemRangeChanged(newItemCount, oldItemCount);
                        refreshLayout.finishRefreshing();
                        recyclerView.smoothScrollToPosition(0);
                    });
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            getActivity().runOnUiThread(() -> refreshLayout.finishRefreshing());
        }
    }
}
