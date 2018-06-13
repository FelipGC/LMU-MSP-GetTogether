package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Organizes fragments to (later) display them as tabs
 */
public class TabPageAdapter extends FragmentPagerAdapter {

    /**
     * A list storing all fragments / tabs
     */
    private final ArrayList<Fragment> fragmentList = new ArrayList<>();
    /**
     * A list storing the titles of our fragments (tabs)
     */
    private final ArrayList<String> fragmentTitleList = new ArrayList<>();

    public TabPageAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment f,String tabName){
        fragmentList.add(f);
        fragmentTitleList.add(tabName);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitleList.get(position);
    }
}
