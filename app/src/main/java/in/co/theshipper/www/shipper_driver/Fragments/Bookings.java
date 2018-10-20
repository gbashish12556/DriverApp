package in.co.theshipper.www.shipper_driver.Fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import in.co.theshipper.www.shipper_driver.Adapter.StatePagerAdapter;
import in.co.theshipper.www.shipper_driver.Helper;
import in.co.theshipper.www.shipper_driver.Activities.FullActivity;
import in.co.theshipper.www.shipper_driver.R;

public class Bookings extends Fragment implements ActionBar.TabListener{

    private View view;
    private ViewPager view_pager;
    private ActionBar actionBar;

    public Bookings() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_bookings, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        view_pager = (ViewPager) view.findViewById(R.id.view_pager);
        view_pager.setAdapter(new StatePagerAdapter(FullActivity.fragmentManager));
        view_pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

        });

        if(getActivity() != null) {

            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        }

        actionBar.removeAllTabs();

        if(actionBar.getTabCount()==0){

            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            ActionBar.Tab tab1 = actionBar.newTab();
            tab1.setText("Current Booking");
            ActionBar.Tab tab2 = actionBar.newTab();
            tab2.setText("Future Booking");
            ActionBar.Tab tab3 = actionBar.newTab();
            tab3.setText("Completed Booking");
            tab1.setTabListener(this);
            tab2.setTabListener(this);
            tab3.setTabListener(this);
            actionBar.addTab(tab1);
            actionBar.addTab(tab2);
            actionBar.addTab(tab3);

        }

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

        view_pager.setCurrentItem(tab.getPosition());

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        List<Fragment> fragments = FullActivity.fragmentManager.getFragments();

        if (fragments != null) {

            FragmentTransaction ft = FullActivity.fragmentManager.beginTransaction();

            for (Fragment f : fragments) {

                //You can perform additional check to remove some (not all) fragments:
                if ((f instanceof CurrentBooking)||(f instanceof FutureBooking)||(f instanceof FinishedBooking)) {

                    ft.remove(f);

                }

            }

            ft.commitAllowingStateLoss();

        }

    }
}
