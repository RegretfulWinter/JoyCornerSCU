package hk.edu.cuhk.ie.iems5722.group25.joycorner.fragment;

import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {
    boolean isVisible;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        hiddenChange(hidden);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            isVisible = true;
            onHiddenChanged(false);
        } else {
            isVisible = false;
            onHiddenChanged(true);
        }
    }

    protected void hiddenChange(boolean hidden) {

    }

}
