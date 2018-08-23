package org.wbing.layout.app_tab;

import android.os.Bundle;

import org.wbing.base.ui.impl.WFrag;
import org.wbing.layout.app_tab.databinding.FragmentMainBinding;

/**
 * @author wangbing
 * @date 2018/8/23
 */
public class MainFragment extends WFrag<FragmentMainBinding> {
    public static MainFragment newInstance(String text) {

        Bundle args = new Bundle();
        args.putString("text", text);
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    String text;

    @Override
    public int layoutId() {
        return R.layout.fragment_main;
    }

    @Override
    public void loadData() {
        getBinding().text.setText(text);
    }

    @Override
    public void recycle() {

    }

    @Override
    public void getParams(Bundle args) {
        super.getParams(args);
        text = args.getString("text");
    }
}
