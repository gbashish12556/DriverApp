package in.co.theshipper.www.shipper_driver.Services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import in.co.theshipper.www.shipper_driver.Constants;
import in.co.theshipper.www.shipper_driver.Helper;

public class GpsTracker extends Service implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    public RequestQueue requestQueue;
    private String TAG = GpsTracker.class.getName();
    private Location location;
    private String user_token;
    private Timer timer;
    private GoogleApiClient mGoogleApiClient;

    public GpsTracker() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        user_token = Helper.getPreference(this,"user_token");

        if (mGoogleApiClient == null) {

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleApiClient.connect();

        TimerProgramm();
        return (START_NOT_STICKY);

    }

    @Override
    public void onDestroy() {

        if(mGoogleApiClient.isConnected()){

            mGoogleApiClient.disconnect();
        }

        stopForeground(true);
        stopForeground(true);

        if(timer != null) {

            timer.cancel();
            timer = null;

        }

    }

    public void TimerProgramm() {

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {

                getLocation();

            }
        }, Constants.Config.UPDATE_DRIVER_LOCATION_DELAY, Constants.Config.UPDATE_DRIVER_LOCATION_PERIOD);

    }

    /**
     * Try to get my current location by GPS or Network Provider
     */

    public void getLocation() {

        if(mGoogleApiClient.isConnected()) {

            do{
                location = Helper.getAccurateCurrentlocationService(mGoogleApiClient, this);
            }while(location == null);

        }

    }

    public void updateGPSCoordinates() {

        if (location != null) {

            String update_location_url = Constants.Config.ROOT_PATH+"update_driver_location";
            String lattitude = String.valueOf(location.getLatitude());
            String longitude = String.valueOf(location.getLongitude());
            HashMap<String,String> hashMap = new HashMap<String,String>();
            hashMap.put("location_lat", lattitude);
            hashMap.put("location_lng", longitude);
            hashMap.put("user_token", user_token);
            Helper.logD("latitude", lattitude);
            Helper.logD("longitude",longitude);
            sendVolleyRequest(update_location_url,Helper.checkParams(hashMap));

        }

    }

    public void sendVolleyRequest(String URL, final HashMap<String,String> hMap){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                if(getApplicationContext() != null) {

                    Helper.ToastShort(getApplicationContext(), Constants.Message.TRACKING_ERROR);

                }


            }
        }){

            @Override
            public HashMap<String,String> getParams(){
                return hMap;
            }

        };

        stringRequest.setTag(TAG);
        Helper.addToRequestQue(requestQueue, stringRequest, this);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }
}