package in.co.theshipper.www.shipper_driver.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import in.co.theshipper.www.shipper_driver.Constants;
import in.co.theshipper.www.shipper_driver.Fragments.About;
import in.co.theshipper.www.shipper_driver.Fragments.BillDetails;
import in.co.theshipper.www.shipper_driver.Fragments.Bookings;
import in.co.theshipper.www.shipper_driver.Fragments.EmergencyContact;
import in.co.theshipper.www.shipper_driver.Fragments.FareCalculator;
import in.co.theshipper.www.shipper_driver.Services.GpsTracker;
import in.co.theshipper.www.shipper_driver.Helper;
import in.co.theshipper.www.shipper_driver.Fragments.NewBooking;
import in.co.theshipper.www.shipper_driver.R;
import in.co.theshipper.www.shipper_driver.Fragments.RateCard;

public class FullActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, AdapterView.OnItemClickListener {

    public String TAG = FullActivity.class.getName();
    private DrawerLayout drawerLayout;
    private ListView listView;
    private String[] NavList;
    private ActionBarDrawerToggle drawerListener;
    public static FragmentManager fragmentManager;
    private Fragment fragment;
    public static int homeFragmentIndentifier = -5;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static GoogleApiClient mGoogleApiClient;
    private String method = "";
    private String fragment_title = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        fragmentManager = getSupportFragmentManager();
        int item = 0;

        if((getIntent() != null)&&(getIntent().getExtras() != null)) {

            Bundle bundle = getIntent().getExtras();
            fragment_title = Helper.getValueFromBundle(bundle,"menuFragment");
            method = Helper.getValueFromBundle(bundle, "method");

        }

        if (fragment_title.equals("FareCalculator")) {

            item = 2;

        } else if (fragment_title.equals("BillDetails")) {

            item = 10;

        } else if (fragment_title.equals("NewBooking")) {

            item = 11;

        }

        if (null == savedInstanceState) {

            selectItem(item);

        }

        getSupportActionBar().setLogo( R.drawable.vehicle_1);
        getSupportActionBar().setHomeButtonEnabled(true);

        try {

            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        } catch (Exception e) {

            e.printStackTrace();

        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        listView = (ListView) findViewById(R.id.nav_menu);
        NavList = getResources().getStringArray(R.array.nav_menu);
        listView.setAdapter(new ArrayAdapter<>(this, R.layout.activity_list_item, R.id.rowTextView, NavList));
        drawerListener = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                invalidateOptionsMenu();

            }

        };

        drawerLayout.setDrawerListener(drawerListener);
        listView.setOnItemClickListener(this);

    }


    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
     }

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        drawerListener.syncState();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_full, menu);
        MenuItem item = menu.findItem(R.id.location_switch);
        RelativeLayout relativeLayout = (RelativeLayout) MenuItemCompat.getActionView(item);
        SwitchCompat mySwitch = (SwitchCompat) relativeLayout.findViewById(R.id.switchForActionBar);

        if (Helper.isMyServiceRunning(GpsTracker.class, this))

            mySwitch.setChecked(true);

        else

            mySwitch.setChecked(false);

            mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    trackerStatus(isChecked);

                    }
            });
            return true;

    }

    private void trackerStatus(boolean v) {

        Intent i = new Intent(this, GpsTracker.class);

        if (v) {

            this.startService(i);
            Toast.makeText(this, "GPS Tracking ON", Toast.LENGTH_SHORT).show();

        } else {

            this.stopService(i);
            Toast.makeText(this, "GPS Tracking OFF", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerListener.onOptionsItemSelected(item)) {

            return true;

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        //super.onConfigurationChanged(newConfig);
        drawerListener.onConfigurationChanged(newConfig);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        drawerLayout.closeDrawers();

        if (position == 4) {

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + Constants.Config.SUPPORT_CONTACT));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;

            }

            startActivity(callIntent);

        }else if(position == 6)
        {

           showLogoutDialog();

        }
        else {

            selectItem(position);
            setTitle(position);

        }

    }

    public void selectItem(int position){

        // listView.setItemChecked(position,true);
        //FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();// For AppCompat use getSupportFragmentManager

        switch(position) {

            default:
                fragment = new RateCard();
                break;

            case 0:
                fragment = new RateCard();
                break;

            case 1:
                fragment = new Bookings();
                break;

            case 2:
                fragment = new FareCalculator();
                break;

            case 3:
                fragment = new EmergencyContact();
                break;

            case 5:
                fragment = new About();
                break;

            case 10:
                fragment = new BillDetails();
                break;

            case 11:
                fragment = new NewBooking();
                break;

        }

        transaction.replace(R.id.main_content, fragment, Constants.Config.CURRENT_FRAG_TAG);
        if((homeFragmentIndentifier == -5)&&(!(fragment instanceof  RateCard))){

            if(method.equals("push")) {

                transaction.commit();
                method = "";

            }else{

                transaction.addToBackStack(null);
                homeFragmentIndentifier =  transaction.commit();

            }

        }else{

            transaction.commit();
        }

    }
    @Override
    public void onBackPressed() {

        fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag(Constants.Config.CURRENT_FRAG_TAG)).commit();
        super.onBackPressed();

        if(homeFragmentIndentifier != -5) {

            getSupportActionBar().setTitle(R.string.title_rate_card_fragment);
            fragmentManager.popBackStack(homeFragmentIndentifier, 0);

        }

        homeFragmentIndentifier = -5;

    }

    public void setTitle(int position){

        getSupportActionBar().setTitle(NavList[position]);

    }

    public void showLogoutDialog(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        //Setting Dialog Title
        alertDialog.setTitle(R.string.LogoutAlertDialogTitle);
        //Setting Dialog Message
        alertDialog.setMessage(R.string.LogoutAlertDialogMessage);
        //On Pressing Setting button

        alertDialog.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                logout();

            }
        });

        //On pressing cancel button
        alertDialog.setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();

            }
        });

        alertDialog.show();

    }

    public void logout(){

        Helper.putPreference(this, "user_token", "defaultStringIfNothingFound");
        Helper.putPreference(this, "vehicletype_id", "defaultStringIfNothingFound");
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {

            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {

                e.printStackTrace();

            }

        } else {


        }

    }
    @Override
    public void onLocationChanged(Location location) {

    }
}

