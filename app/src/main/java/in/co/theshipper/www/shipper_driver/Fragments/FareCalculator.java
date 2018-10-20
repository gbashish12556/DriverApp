package in.co.theshipper.www.shipper_driver.Fragments;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import in.co.theshipper.www.shipper_driver.Constants;
import in.co.theshipper.www.shipper_driver.Controller.DBController;
import in.co.theshipper.www.shipper_driver.Helper;
import in.co.theshipper.www.shipper_driver.Activities.FullActivity;
import in.co.theshipper.www.shipper_driver.Services.GPSService;
import in.co.theshipper.www.shipper_driver.R;
import in.co.theshipper.www.shipper_driver.Services.TimerService;

public class FareCalculator extends Fragment {

    public RequestQueue requestQueue;
    private String TAG = FareCalculator.class.getName();
    private Button  stop_journey,stop_timer;
    private LinearLayout stop_view,timer_view;
    public DBController controller;
    private Location location;
    private long loading_stop_time = 0,journey_stop_time = 0;
    private Calendar calendar2,calendar3;
    public FareCalculator() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        controller = new DBController(getActivity());
        View view = inflater.inflate(R.layout.fragment_calculator, container, false);
        stop_view = (LinearLayout) view.findViewById(R.id.stop_view);
        timer_view = (LinearLayout) view.findViewById(R.id.timer_view);
        stop_journey = (Button) view.findViewById(R.id.stop_journey);
        stop_timer = (Button) view.findViewById(R.id.stop_timer);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if(getActivity() != null) {

            if (Helper.isMyServiceRunning(GPSService.class, getActivity()) == false) {

                stop_view.setVisibility(View.GONE);
                timer_view.setVisibility(View.GONE);

            }

            final String user_token = Helper.getPreference(getActivity(), Constants.Keys.USER_TOKEN);

            stop_timer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
                    calendar2 = Calendar.getInstance(tz);
                    loading_stop_time = calendar2.getTimeInMillis();
                    Helper.putPreference(getActivity(), Constants.Keys.UNLOADING_STOP_TIME, String.valueOf(loading_stop_time));
                    String change_driver_status = Constants.Config.ROOT_PATH + "change_driver_status";
                    HashMap<String, String> hashMap = new HashMap<String, String>();
                    hashMap.put("user_token", user_token);
                    hashMap.put("status", "0");
                    sendVolleyRequest(change_driver_status, Helper.checkParams(hashMap), "change_status_free");

                }
            });

            stop_journey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
                    calendar3 = Calendar.getInstance(tz);
                    journey_stop_time = calendar3.getTimeInMillis();
                    Helper.putPreference(getActivity(), Constants.Keys.JOURNEY_STOP_TIME, String.valueOf(journey_stop_time));
                    timer_view.setVisibility(View.VISIBLE);
                    stop_view.setVisibility(View.GONE);
                    final Intent intent = new Intent(getActivity(), GPSService.class);
                    getActivity().stopService(intent);
                    Intent i = new Intent(getActivity(), TimerService.class);
                    getActivity().startService(i);

                }
            });

        }

    }

    public void sendVolleyRequest(String URL, final HashMap<String, String> hMap, final String method) {

        if(getActivity() != null) {

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {

                    if (method.equals("change_status_free")) {

                        bookingStatusFreeSuccess(response);

                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                        Helper.ToastShort(getActivity(), Constants.Message.NETWORK_ERROR);

                }

            }) {

                @Override
                public HashMap<String, String> getParams() {
                    return hMap;
                }

            };

            stringRequest.setTag(TAG);
            Helper.addToRequestQue(requestQueue, stringRequest, getActivity());

        }

    }

    public void bookingStatusFreeSuccess(String response){

        if(getActivity() != null) {

            if (!Helper.CheckJsonError(response)) {

                Intent Intent = new Intent(getActivity(), TimerService.class);
                getActivity().stopService(Intent);

                if (FullActivity.mGoogleApiClient.isConnected()) {

                    do {

                        location = Helper.getAccurateCurrentlocation(FullActivity.mGoogleApiClient, getActivity());

                    } while (location == null);

                    Helper.putPreference(getActivity(), Constants.Keys.EXACT_DROPOFF_POINT, Helper.getLocationAddress(location.getLatitude(), location.getLongitude(), getActivity()));

                }

                Intent i = new Intent(getActivity(), FullActivity.class);
                i.putExtra("menuFragment", "BillDetails");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(Helper.CheckIntent(i));

            }

        }

    }

    @Override
    public void onResume() {
        super.onResume();

        Helper.startAllVolley(requestQueue);

    }

    @Override
    public void onPause() {
        super.onPause();

        Helper.stopAllVolley(requestQueue);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Helper.cancelAllRequest(requestQueue, TAG);

    }

}
