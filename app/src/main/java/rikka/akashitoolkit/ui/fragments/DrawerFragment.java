package rikka.akashitoolkit.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.util.List;

import rikka.akashitoolkit.MainActivity;
import rikka.akashitoolkit.ui.widget.IconSwitchCompat;

/**
 * Created by Rikka on 2016/3/10.
 *
 * MainActivity 中 Drawer 中的项目使用的 Fragment
 */

public abstract class DrawerFragment extends SaveVisibilityFragment {

    private static final String SWITCH_CHECKED = "SWITCH_CHECKED";

    protected MainActivity mActivity;
    private boolean mSwitchChecked;
    private boolean mCallListener;
    private View mDrawerContent;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mDrawerContent == null) {
            mDrawerContent = onCreateRightDrawerContentView(savedInstanceState);
        }

        setDrawerContent();
    }

    private void setDrawerContent() {
        if (mDrawerContent != null) {
            mActivity.getRightDrawerContainer().removeAllViews();
            if (mDrawerContent.getParent() != null && mDrawerContent.getParent() instanceof ViewGroup) {
                ((ViewGroup) mDrawerContent.getParent()).removeView(mDrawerContent);
            }
            mActivity.getRightDrawerContainer().addView(mDrawerContent);
        }
    }

    public void onShow() {
        super.onShow();

        mActivity.getTabLayout().setVisibility(onSetTabLayout(mActivity.getTabLayout()) ? View.VISIBLE : View.GONE);
        mActivity.getSwitch().setVisibility(onSetSwitch(mActivity.getSwitch()) ? View.VISIBLE : View.GONE);
        mActivity.getSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSwitchChecked = isChecked;
                callListener(DrawerFragment.this, isChecked);

                callChildListener(getChildFragmentManager(), isChecked);
            }

            private void callChildListener(FragmentManager fm, boolean isChecked) {
                List<Fragment> fragments = fm.getFragments();
                if (fragments != null && fragments.size() > 0) {
                    for (Fragment f : fragments) {
                        if (f != null) {
                            callChildListener(f.getChildFragmentManager(), isChecked);
                            callListener(f, isChecked);
                        }
                    }
                }
            }

            private void callListener(Fragment f, boolean isChecked) {
                if (f instanceof ISwitchFragment) {
                    ISwitchFragment _f = (ISwitchFragment) f;
                    _f.onSwitchCheckedChanged(isChecked);
                }
            }
        });

        mActivity.getSwitch().setChecked(mSwitchChecked, mCallListener);

        mActivity.setRightDrawerLocked(mDrawerContent == null);
        setDrawerContent();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            mSwitchChecked = savedInstanceState.getBoolean(SWITCH_CHECKED);
        }

        mCallListener = savedInstanceState == null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SWITCH_CHECKED, mActivity.getSwitch().isChecked());
    }

    /**
     * 把设置 MainActivity 的 TabLayout 的代码写到这里
     *
     * @param tabLayout TabLayout
     * @return 是否显示 TabLayout
     */
    protected boolean onSetTabLayout(TabLayout tabLayout) {
        return false;
    }

    /**
     * 如果返回不为 null 则表示有右侧 drawer
     *
     * @return 右侧 Drawer 内 View
     */
    protected View onCreateRightDrawerContentView(@Nullable Bundle savedInstanceState) {
        return null;
    }

    /**
     * 把设置 MainActivity 的 Switch 的代码写到这里
     *
     * @param switchView IconSwitchCompat
     * @return 是否显示 switchView
     */
    protected boolean onSetSwitch(IconSwitchCompat switchView) {
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        mActivity = null;
        mDrawerContent = null;

        super.onDetach();
    }

    /*public void setTabLayoutVisibleWithAnim() {
        mActivity.getTabLayout().setLayoutTransition(new LayoutTransition());
        mActivity.getTabLayout().setVisibility(? View.VISIBLE : View.GONE);
    }*/

    public void setToolbarTitle(@StringRes int resId) {
        setToolbarTitle(getString(resId));
    }

    public void setToolbarTitle(String title) {
        if (mActivity.getSupportActionBar() != null) {
            mActivity.getSupportActionBar().setTitle(title);
        }
    }
}
