package hk.edu.cuhk.ie.iems5722.group25.joycorner.activity;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.fragment.ChatFragment;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.ActivityUtils;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.content_frame)
    FrameLayout contentFrame;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        unbinder = ButterKnife.bind(this);

        ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), new ChatFragment(), contentFrame.getId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
