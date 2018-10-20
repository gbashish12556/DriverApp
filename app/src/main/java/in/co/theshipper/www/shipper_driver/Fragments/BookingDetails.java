package in.co.theshipper.www.shipper_driver.Fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import in.co.theshipper.www.shipper_driver.Constants;
import in.co.theshipper.www.shipper_driver.Helper;
import in.co.theshipper.www.shipper_driver.Activities.FullActivity;
import in.co.theshipper.www.shipper_driver.Services.GPSService;
import in.co.theshipper.www.shipper_driver.R;
import in.co.theshipper.www.shipper_driver.Services.TimerService;

import static com.google.android.gms.internal.zzir.runOnUiThread;


public class BookingDetails extends Fragment implements View.OnClickListener {


    public RequestQueue requestQueue;
    private String TAG = BookingDetails.class.getName();
    public View view;
    public Context context;
    private LinearLayout map, start_view, start_journey_view,start_timer_view;
    private TextView location_datetime;
    private Button callButton, start_journey, start_timer;
    private SupportMapFragment mMapFragment;
    public GoogleMap mMap = null;
    // flag for GPS Status
    private double current_lat, current_lng;
    private boolean stopTimer = false,stopTimerForever = false;
    private Timer timer;
    private Location location;
    private String  received_customer_name, received_crn_no, received_customer_token, received_customer_current_lat, received_customer_current_lng;
    private String crn_no = "";
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private ImageView material_image,popup;
    private Dialog dialog;
    private Calendar calendar1,calendar2;
    private long journey_start_time = 0, loading_start_time = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public BookingDetails() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_booking_details, container, false);
        start_view = (LinearLayout) view.findViewById(R.id.start_view);
        start_journey_view = (LinearLayout) view.findViewById(R.id.start_journey_view);
        start_timer_view = (LinearLayout) view.findViewById(R.id.start_timer_view);
        start_journey = (Button) view.findViewById(R.id.start_journey);
        start_timer = (Button) view.findViewById(R.id.start_timer);
        map = (LinearLayout) view.findViewById(R.id.map);
        callButton = (Button) view.findViewById(R.id.customer_mobile_no);
        material_image = (ImageView) view.findViewById(R.id.material_image);

        if(getActivity() != null){

            if ((getActivity().getIntent() != null) && (getActivity().getIntent().getExtras() != null)) {

                Bundle bundle = getActivity().getIntent().getExtras();
                crn_no = Helper.getValueFromBundle(bundle, "crn_no");
                getActivity().getIntent().setData(null);
                getActivity().setIntent(null);

            } else if (this.getArguments() != null) {

                Bundle bundle = this.getArguments();
                crn_no = Helper.getValueFromBundle(bundle, "crn_no");

            }

            dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog);
            dialog.setCancelable(true);
            popup = (ImageView) dialog.findViewById(R.id.image_popup);

       }

        callButton.setOnClickListener(this);

        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        String booking_status_url = Constants.Config.ROOT_PATH + "get_driver_booking_status";
        mMapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().replace(R.id.map, mMapFragment, "MAP_FRAGMENT").commit();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        String user_token = Helper.getPreference(context, "user_token");
        hashMap.put("crn_no", crn_no);
        hashMap.put("user_token", user_token);
        sendVolleyRequest(booking_status_url, Helper.checkParams(hashMap), "booking_status");

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getActivity() != null) {

            if (Helper.isMyServiceRunning(GPSService.class, getActivity()) == true) {

                stopTimerForever = true;
                start_view.setVisibility(View.GONE);

            }else if(Helper.isMyServiceRunning(TimerService.class, getActivity()) == true) {

                start_journey_view.setVisibility(View.VISIBLE);
                start_timer_view.setVisibility(View.GONE);

            }

            start_timer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
                    calendar1 = Calendar.getInstance(tz);
                    loading_start_time = calendar1.getTimeInMillis();
                    Helper.putPreference(getActivity(),Constants.Keys.LOADING_START_TIME,String.valueOf(loading_start_time));
                    start_journey_view.setVisibility(View.VISIBLE);
                    start_timer_view.setVisibility(View.GONE);
                    Helper.logD("start_timer","TimerService Called");
                    Intent intent = new Intent(getActivity(), TimerService.class);
                    getActivity().startService(intent);

                }

            });

            start_journey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
                    calendar2 = Calendar.getInstance(tz);
                    journey_start_time = calendar2.getTimeInMillis();
                    Helper.putPreference(getActivity(),Constants.Keys.JOURNEY_START_TIME,String.valueOf(journey_start_time));
                    String change_driver_status = Constants.Config.ROOT_PATH + "change_driver_status";
                    HashMap<String, String> hashMap = new HashMap<String, String>();
                    hashMap.put("user_token", Helper.getPreference(getActivity(), Constants.Keys.USER_TOKEN));
                    hashMap.put("status", "1");
                    sendVolleyRequest(change_driver_status, Helper.checkParams(hashMap), "change_status");

                }

            });

        }

    }

    public void sendVolleyRequest(String URL, final HashMap<String, String> hMap, final String method) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                if (method.equals("booking_status")) {

                    bookingStatusSuccess(response);

                } else if (method.equals("customer_location")) {

                    vehicleLocationSuccess(response);
                } else if (method.equals("draw_path")) {

                    drawPath(response);

                }else if (method.equals("change_status")) {

                    statusChangeSuccess(response);

                }

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                if(getActivity() != null) {

                    Helper.ToastShort(getActivity(), Constants.Message.NETWORK_ERROR);

                }

            }

        }) {

            @Override
            public HashMap<String, String> getParams() {
                return hMap;
            }

        };

        stringRequest.setTag(TAG);

        if(getActivity() != null) {

            Helper.addToRequestQue(requestQueue, stringRequest, getActivity());

        }

    }

    public void statusChangeSuccess(String response){

        if(getActivity() != null) {

            if(!Helper.CheckJsonError(response)) {

                Intent i = new Intent(getActivity(), TimerService.class);
                getActivity().stopService(i);

                if (FullActivity.mGoogleApiClient.isConnected()) {

                    do {

                        location = Helper.getAccurateCurrentlocation(FullActivity.mGoogleApiClient, getActivity());

                    } while (location == null);

                    Helper.putPreference(getActivity(), Constants.Keys.EXACT_PICKUP_POINT, Helper.getLocationAddress(location.getLatitude(), location.getLongitude(), getActivity()));

                } else {

                    Helper.ToastShort(getActivity(), Constants.Message.NETWORK_ERROR);

                }

                stopTimerForever = true;
                Intent Intent = new Intent(getActivity(), TimerService.class);
                getActivity().stopService(Intent);
                Intent intent = new Intent(getActivity(), GPSService.class);
                intent.putExtra("crn_no", received_crn_no);
                start_view.setVisibility(View.GONE);
                getActivity().startService(Helper.CheckIntent(intent));

            }

        }

    }

    public void bookingStatusSuccess(String response) {
        if (!Helper.CheckJsonError(response)) {

            try {

                JSONObject jsonObject = new JSONObject(response);
                String errFlag = jsonObject.getString("errFlag");

                if (errFlag.equals("0")) {

                    if (jsonObject.has("likes")) {

                        JSONArray jsonArray = jsonObject.getJSONArray("likes");
                        int count = 0;

                        while (count < jsonArray.length()) {

                            JSONObject JO = jsonArray.getJSONObject(count);
                            location_datetime = (TextView) view.findViewById(R.id.location_datetime);
                            TextView customer_mobile_no_view = (Button) view.findViewById(R.id.customer_mobile_no);
                            TextView customer_name_view = (TextView) view.findViewById(R.id.customer_name);
                            received_crn_no = JO.getString("crn_no");
                            received_customer_token = JO.getString("customer_token");
                            received_customer_name = JO.getString("customer_name");
                            String received_customer_mobile_no = JO.getString("customer_mobile_no");
                            String received_location_update_datetime = JO.getString("customer_location_datetime");
                            received_customer_current_lat = JO.getString("customer_location_lat");
                            received_customer_current_lng = JO.getString("customer_location_lng");
                            String material_image_url=JO.getString("material_image_url");
                            String profile_pic_url = Constants.Config.ROOT_PATH+material_image_url;

                            if(material_image_url.length()>0){

                                downloadBitmapFromURL(profile_pic_url);

                            }else{

                                material_image.setImageResource(R.drawable.addcontact);
                                material_image.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        popup.setImageResource(R.drawable.addcontact);
                                        dialog.show();

                                    }
                                });

                            }

                            location_datetime.setText("Last Seen: " + Helper.getDateName(received_location_update_datetime));
                            customer_name_view.setText("Customer: "+received_customer_name);
                            customer_mobile_no_view.setText(received_customer_mobile_no);
                            setUpMapIfNeeded();
                            TimerProgramm();
                            count++;

                        }

                    }

                }

            } catch (JSONException e) {

                e.printStackTrace();

            }

        } else {

            ErrorDialog(Constants.Title.SERVER_ERROR, Constants.Message.SERVER_ERROR);

        }

    }

    public void vehicleLocationSuccess(String response) {

        try {

            if (!Helper.CheckJsonError(response)) {

                JSONObject jsonObject = new JSONObject(response);
                String errFlag = jsonObject.getString("errFlag");
                String errMsg = jsonObject.getString("errMsg");

                if (errFlag.equals("0")) {

                    if (jsonObject.has("likes")) {

                        JSONArray jsonArray = jsonObject.getJSONArray("likes");
                        int count = 0;

                        while (count < jsonArray.length()) {

                            JSONObject JO = jsonArray.getJSONObject(count);
                            received_customer_current_lat = JO.getString("customer_location_lat");
                            received_customer_current_lng = JO.getString("customer_location_lng");
                            String received_location_update_datetime = JO.getString("customer_location_datetime");
                            location_datetime.setText("Last Seen: " + Helper.getDateName(received_location_update_datetime));
                            map.setVisibility(View.VISIBLE);
                            setUpMapIfNeeded();
                            count++;

                        }

                    }

                }

            } else {

                if(getActivity() != null) {

                    Helper.ToastShort(getActivity(), Constants.Message.SERVER_ERROR);

                }

            }

        } catch (JSONException e) {

            e.printStackTrace();

        }

    }

    public void TimerProgramm() {

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {

                runOnUiThread(new Runnable() {
                    public void run() {

                        if (!stopTimer && !stopTimerForever) {

                            String customer_location_url = Constants.Config.ROOT_PATH + "get_customer_location";
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            hashMap.put("customer_token", received_customer_token);
                            sendVolleyRequest(customer_location_url, Helper.checkParams(hashMap), "customer_location");

                        }

                    }
                });

            }
        }, Constants.Config.GET_CUSTOMER_LOCATION_DELAY, Constants.Config.GET_CUSTOMER_LOCATION_PERIOD);

    }

    public void setUpMapIfNeeded() {

        if(getActivity() != null) {

            // Do a null check to confirm that we have not already instantiated the map.
            if (mMap != null) {
                mMap.clear();
                mMap = null;
            }

            if (mMap == null) {

                // Try to obtain the map from the SupportMapFragment.
                mMap = mMapFragment.getMap();
                // Check if we were successful in obtaining the map.

                if (mMap != null) {

                    if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;

                    }

                    mMap.setMyLocationEnabled(true);

                    if (FullActivity.mGoogleApiClient.isConnected()) {

                        do {

                            location = Helper.getAccurateCurrentlocation(FullActivity.mGoogleApiClient, getActivity());

                        } while (location == null);

                        if (location != null) {

                            if (mMap != null) {

                                current_lat = location.getLatitude();
                                current_lng = location.getLongitude();
                                float c = Helper.getBearing(current_lat, current_lng, Double.parseDouble(received_customer_current_lat), Double.parseDouble(received_customer_current_lng));
                                LatLng latlng = new LatLng(current_lat, current_lng);// This methods gets the users current longitude and latitude.
                                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latlng, Constants.Config.MAP_HIGH_ZOOM_LEVEL, 1, c)));

                                try {

                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(Double.parseDouble(received_customer_current_lat), Double.parseDouble(received_customer_current_lng)))
                                            .title(received_customer_name));

                                } catch (NumberFormatException e) {

                                    e.printStackTrace();

                                }

                                String url = makeURL(received_customer_current_lat, received_customer_current_lng, String.valueOf(current_lat), String.valueOf(current_lng));
                                HashMap<String, String> hashMap = new HashMap<String, String>();
                                sendVolleyRequest(url, Helper.checkParams(hashMap), "draw_path");

                            }

                        }

                    }

                }

            }

        }

    }

    public String makeURL(String sourceLat, String sourceLng, String destLat,String destLng){

        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from

        try {

            urlString.append(URLEncoder.encode(sourceLat,"UTF-8"));
            urlString.append(",");
            urlString.append(URLEncoder.encode(sourceLng,"UTF-8"));
            urlString.append("&destination=");// to
            urlString.append(URLEncoder.encode(destLat,"UTF-8"));
            urlString.append(",");
            urlString.append(URLEncoder.encode(destLng,"UTF-8"));
            urlString.append("&sensor=false&mode=driving&alternatives=true");
            urlString.append("&key="+URLEncoder.encode(getResources().getString(R.string.server_APIkey1), "UTF-8"));

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();

        }

        return urlString.toString();

    }

    public void drawPath(String  result) {

        try {

            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);

            if(mMap !=  null) {

                Polyline line = mMap.addPolyline(new PolylineOptions()
                                .addAll(list)
                                .width(12)
                                .color(Color.parseColor("#05b1fb"))//Google maps blue color
                                .geodesic(true)

                );

            }

            JSONArray legsArray = routes.getJSONArray("legs");
            JSONObject legs = legsArray.getJSONObject(0);
            JSONObject distance = legs.getJSONObject("distance");
            String distance_km  = distance.getString("text");
            JSONObject duration = legs.getJSONObject("duration");
            String duration_min  = duration.getString("text");

            if(getActivity() != null){

                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("");
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(duration_min + " ( " + distance_km + " ) ");

            }

        } catch (JSONException e) {

            e.printStackTrace();

        }

    }

    public List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {

            int b, shift = 0, result = 0;

            do {

                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;

            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;

            do {

                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;

            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);

        }
        return poly;

    }

    private void ErrorDialog(String Title,String Message){

        if(getActivity() != null) {

            Helper.showDialog(getActivity(), Title, Message);

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(getActivity() != null) {

            switch (requestCode) {

                case REQUEST_CHECK_SETTINGS:

                    switch (resultCode) {

                        case Activity.RESULT_CANCELED:
                            Helper.showGpsAutoEnableRequest(FullActivity.mGoogleApiClient, getActivity());//keep asking if imp or do whatever
                            break;

                    }

                    break;

            }

        }

    }

    @Override
    public void onClick(View v) {

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + callButton.getText().toString()));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callIntent);

    }

    public void downloadBitmapFromURL(String profile_pic_url){

        if(getActivity() != null) {

            //        RequestQueue requestQueue;
            final Bitmap[] return_param = new Bitmap[1];

            ImageRequest imageRequest = new ImageRequest(profile_pic_url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(final Bitmap response) {

                    material_image.setImageBitmap(response);
                    material_image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            popup.setImageBitmap(response);
                            dialog.show();

                        }
                    });

                }
            }, 0, 0, null, null);

            imageRequest.setTag(TAG);
            Helper.addToRequestQue(requestQueue, imageRequest, getActivity()

            );

        }

    }

    @Override
    public void onResume() {
        super.onResume();

        Helper.startAllVolley(requestQueue);
        stopTimer = false;

    }

    @Override
    public void onPause() {
        super.onPause();

        Helper.stopAllVolley(requestQueue);
        stopTimer = true;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(mMap != null){
            mMap = null;
        }

        if(timer!=null) {

            timer.cancel();
            timer = null;

        }

        Helper.cancelAllRequest(requestQueue, TAG);

    }

}