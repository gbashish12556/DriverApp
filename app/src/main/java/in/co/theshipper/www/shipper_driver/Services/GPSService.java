package in.co.theshipper.www.shipper_driver.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import in.co.theshipper.www.shipper_driver.Activities.FullActivity;
import in.co.theshipper.www.shipper_driver.Constants;
import in.co.theshipper.www.shipper_driver.Helper;
import in.co.theshipper.www.shipper_driver.R;

public class GPSService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private Location latestLocation, PreviousLocation;
    private Double totDist = (double) 0,StLineDistance = (double)0;
    private Timer timer;
    private GoogleApiClient mGoogleApiClient;
    private StringRequest stringRequest;
    private RequestQueue requestQueue;
    private String  crn_no = "",ToastString = "";
    private Double distancef;
    private JSONObject jsonObject = null;
    private JSONArray rows = null;
    private JSONObject firstObject = null;
    private JSONArray elements = null;
    private JSONObject elementsFirst = null;
    private JSONObject distanceObject = null;
    private Calendar calendar2;
    private long  diff = 0, diffsec = 0, diffmin = 0, diffHours = 0, i=0;
    private Boolean responseReceived = true;
    private Notification notification;
    private long start_time = 0;

    @Override
    public void onCreate() {

        if(getApplicationContext() != null){
            start_time = Long.parseLong(Helper.getPreference(getApplicationContext(),Constants.Keys.LOADING_START_TIME));
        }


        mGoogleApiClient = new GoogleApiClient.Builder(this)

                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if (mGoogleApiClient != null) {

            mGoogleApiClient.connect();

        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if ((intent != null) && (intent.getExtras() != null)) {

            crn_no = intent.getExtras().getString("crn_no");

        }

        Intent i = new Intent(this, FullActivity.class);
        i.putExtra("menuFragment", "FareCalculator");
        i.putExtra("method", "push");
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, Helper.CheckIntent(i), PendingIntent.FLAG_CANCEL_CURRENT);
        android.support.v7.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(this);

        notification = builder.setContentIntent(pi)
                .setSmallIcon(R.drawable.vehicle_1).setTicker("SHIPPER").setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setContentTitle("SHIPPER")
                .setContentText("Journey in progress").build();

        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(1317, notification);
        Helper.SystemPrintLn("GPS Service start");
        startTracking();
        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {

        if (timer != null) {

            timer.cancel();
            timer = null;

        }

        stopForeground(true);

        if (mGoogleApiClient != null) {

            mGoogleApiClient.disconnect();

        }

        do {

            if (getApplicationContext() != null) {

                Helper.putPreference(getApplicationContext(), Constants.Keys.CRN_NO, crn_no);
                Helper.putPreference(getApplicationContext(), Constants.Keys.TOTAL_DISTANCE_TAVELLED, String.valueOf(totDist));

            }

        }while(getApplicationContext() == null);
 }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startTracking() {

        requestQueue = Volley.newRequestQueue(this);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {

                caculateFare();

            }
        }, Constants.Config.SEND_DISTANCE_REQUEST_DELAY, Constants.Config.SEND_DISTANCE_REQUEST_PERIOD);

    }

    public void caculateFare() {
        ToastString = "";

        if (mGoogleApiClient.isConnected()) {

            do{
                latestLocation = Helper.getAccurateCurrentlocationService(mGoogleApiClient, this);
            }while(latestLocation == null);

            if (latestLocation != null) {

                if (PreviousLocation != null) {

                    StringBuilder urlString = new StringBuilder();
                    final double PreviousLattitde = PreviousLocation.getLatitude();
                    final double PreviousLongitude = PreviousLocation.getLongitude();
                    final double LatestLattitde = latestLocation.getLatitude();
                    final double LatestLongitude = latestLocation.getLongitude();

                    try {

                        urlString.append(" https://maps.googleapis.com/maps/api/distancematrix/json?units=metric");
                        urlString.append("&origins=");
                        urlString.append(String.valueOf(PreviousLattitde) + "," + String.valueOf(PreviousLongitude));
                        urlString.append("&destinations=");
                        urlString.append(String.valueOf(LatestLattitde) + "," + String.valueOf(LatestLongitude));
                        urlString.append("&key=" + URLEncoder.encode(getResources().getString(R.string.server_APIkey1), "UTF-8"));

                    } catch (UnsupportedEncodingException e) {

                        e.printStackTrace();

                    }

                    String url = urlString.toString();

                    stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            responseReceived = true;

                            try {

                                jsonObject = new JSONObject(response);
                                rows = jsonObject.getJSONArray("rows");
                                firstObject = rows.getJSONObject(0);
                                elements = firstObject.getJSONArray("elements");
                                elementsFirst = elements.getJSONObject(0);
                                distanceObject = elementsFirst.getJSONObject("distance");
                                distancef = ((double)(distanceObject.getDouble("value")) / 1000);
                                StLineDistance = Helper.getDistanceFromLatLonInKm(PreviousLattitde,PreviousLongitude,LatestLattitde,LatestLongitude);

                                if(distancef>(Constants.Config.ACURATE_DISTANCE_RATIO_FACTOR*StLineDistance)){

                                    ToastString = "Distance Exceeded its limit";
                                    distancef = StLineDistance;

                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            }

                            totDist = totDist + distancef;
                            i++;
                            calendar2 = Calendar.getInstance();
                            diff = (calendar2.getTimeInMillis() - start_time);
                            diffsec = (diff / (1000) )% 60;
                            diffmin = (diff / (60 * 1000)) % 60;
                            diffHours = diff / (60 * 60 * 1000);

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            if(getApplicationContext() != null) {

                                Helper.ToastShort(getApplicationContext(), "distance mistake-Error: " + String.valueOf(error));
                            }

                        }
                    });

                    volley(stringRequest);

                }

                if(responseReceived) {

                    PreviousLocation = latestLocation;
                    responseReceived = false;
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

                }

                latestLocation = null;

            } else {

                //TODO

            }

        }

    }

    public void volley(StringRequest request)
    {
        requestQueue.add(request);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Helper.SystemPrintLn("service apiclient connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

}