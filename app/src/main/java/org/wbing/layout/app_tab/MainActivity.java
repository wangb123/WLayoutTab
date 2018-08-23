package org.wbing.layout.app_tab;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.wbing.base.ui.impl.WAct;
import org.wbing.layout.app_tab.databinding.ActivityMainBinding;

public class MainActivity extends WAct<ActivityMainBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int layoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void loadData() {

        getBinding().content.setAdapter(new Adapter(getSupportFragmentManager()));
        getBinding().tab.attachViewPager(getBinding().content);
    }

    @Override
    public void recycle() {

    }

    class Adapter extends FragmentPagerAdapter {

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return MainFragment.newInstance("fragment" + i);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return "tabtab" + position;
        }

        @Override
        public int getCount() {
            return 100;
        }
    }

}
