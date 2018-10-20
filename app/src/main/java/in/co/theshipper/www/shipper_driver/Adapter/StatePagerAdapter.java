package in.co.theshipper.www.shipper_driver.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import in.co.theshipper.www.shipper_driver.Fragments.CurrentBooking;
import in.co.theshipper.www.shipper_driver.Fragments.FinishedBooking;
import in.co.theshipper.www.shipper_driver.Fragments.FutureBooking;
import in.co.theshipper.www.shipper_driver.R;

public class StatePagerAdapter extends FragmentStatePagerAdapter {

    public StatePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment = null;

        switch(position){

            case 0:
                fragment = new CurrentBooking();
                break;

            case 1:
                fragment = new FutureBooking();
                break;

            case 2:
                fragment = new FinishedBooking();
                break;

        }

        return fragment;

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";

        switch(position){

            case 0:

                title =  String.valueOf(R.string.title_current_booking_fragment);
                break;

            case 1:
                title =  String.valueOf(R.string.title_future_booking_fragment);
                break;

            case 2:
                title =  String.valueOf(R.string.title_finished_booking_fragment);
                break;

        }

        return title;

    }

}