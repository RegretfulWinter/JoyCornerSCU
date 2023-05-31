package hk.edu.cuhk.ie.iems5722.group25.joycorner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;

import java.util.List;

import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.ActivityGameName;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.ActivityInfo;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.ImageUtil;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    private Context context;
    private List<ActivityInfo> activityInfoList;
    private int size;
    private OnItemClickListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final QMUICommonListItemView infoView;

        public ViewHolder(View view) {
            super(view);
            infoView = view.findViewById(R.id.activity_info);
        }


        public QMUICommonListItemView getInfoView() {
            return infoView;
        }
    }

    public ActivityAdapter(Context context, List<ActivityInfo> activityInfoList) {
        this.context = context;
        this.activityInfoList = activityInfoList;
        this.size = QMUIDisplayHelper.dp2px(context, 80);
    }

    @NonNull
    @Override
    public ActivityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.activity_item_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityAdapter.ViewHolder viewHolder, int position) {
        ActivityInfo info = activityInfoList.get(position);
        QMUICommonListItemView infoView = viewHolder.getInfoView();
        infoView.setText(info.getActivityType());
        infoView.setImageDrawable(ImageUtil.zoomDrawable(context.getDrawable(ActivityGameName.getGameDrawable(info.getActivityType())), size));
        infoView.setDetailText(info.getStartTime().substring(0, 10));
        infoView.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        infoView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return activityInfoList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
