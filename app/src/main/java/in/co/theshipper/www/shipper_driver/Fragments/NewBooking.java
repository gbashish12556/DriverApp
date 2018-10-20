package in.co.theshipper.www.shipper_driver.Fragments;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import in.co.theshipper.www.shipper_driver.Activities.FullActivity;
import in.co.theshipper.www.shipper_driver.Constants;
import in.co.theshipper.www.shipper_driver.Helper;
import in.co.theshipper.www.shipper_driver.R;


public class NewBooking extends Fragment {

    private String TAG = NewBooking.class.getName();
    private View view;
    private TextView crn_no,booking_datetime,vehicle_type,customer_mobile_no,material_weight,pickup_point,dropoff_point;
    private String user_token,received_crn_no;
    private Button Accept,Reject;
    private int accept_flag;
    private RequestQueue requestQueue;
    private ImageView material_image,popup;
    private Dialog dialog;

    public NewBooking() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_new_booking, container, false);
        crn_no = (TextView) view.findViewById(R.id.crn_no);
        booking_datetime = (TextView) view.findViewById(R.id.booking_datetime);
        vehicle_type = (TextView) view.findViewById(R.id.vehicle_type);
        customer_mobile_no = (TextView) view.findViewById(R.id.customer_mobile_no);
        material_weight = (TextView) view.findViewById(R.id.material_weight);
        pickup_point = (TextView) view.findViewById(R.id.pickup_point);
        dropoff_point = (TextView) view.findViewById(R.id.dropoff_point);
        material_image = (ImageView) view.findViewById(R.id.material_image);
        Accept = (Button) view.findViewById(R.id.Accept);
        Reject = (Button) view.findViewById(R.id.Reject);
        Accept.setEnabled(false);
        Reject.setEnabled(false);

        if(getActivity() != null) {

            dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog);
            dialog.setCancelable(true);
            popup = (ImageView) dialog.findViewById(R.id.image_popup);

            if ((getActivity().getIntent() != null) && (getActivity().getIntent().getExtras() != null)) {

                Bundle bundle = getActivity().getIntent().getExtras();
                String crn_no = Helper.getValueFromBundle(bundle, "crn_no");
                getActivity().getIntent().removeExtra("crn_no");
                Helper.SystemPrintLn("CRN_NO received" + crn_no);
                getActivity().getIntent().setData(null);
                getActivity().setIntent(null);
                HashMap<String, String> hashMap = new HashMap<String, String>();
                final String get_new_booking_url = Constants.Config.ROOT_PATH + "get_new_booking";
                hashMap.put("crn_no", crn_no);
                hashMap.put("user_token", user_token);
                sendVolleyRequest(get_new_booking_url, Helper.checkParams(hashMap), "get_new_booking");

            }

        }

        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if(getActivity() != null) {

            user_token = Helper.getPreference(getActivity(), Constants.Keys.USER_TOKEN);
            final String accept_booking_url = Constants.Config.ROOT_PATH + "accept_booking";
            Accept.setEnabled(true);
            Reject.setEnabled(true);

            Accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    accept_flag = 1;
                    final HashMap<String, String> hmap = new HashMap<String, String>();
                    hmap.put("crn_no", received_crn_no);
                    hmap.put("accept_flag", String.valueOf(accept_flag));
                    hmap.put("user_token", user_token);
                    sendVolleyRequest(accept_booking_url, Helper.checkParams(hmap), "accept_booking");

                }
            });

            Reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    accept_flag = 0;
                    final HashMap<String, String> hmap = new HashMap<String, String>();
                    hmap.put("crn_no", received_crn_no);
                    hmap.put("accept_flag", String.valueOf(accept_flag));
                    hmap.put("user_token", user_token);
                    sendVolleyRequest(accept_booking_url, Helper.checkParams(hmap), "accept_booking");

                }
            });

            customer_mobile_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + customer_mobile_no.getText().toString()));
                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(callIntent);

                }
            });

        }

    }

    public void sendVolleyRequest(String url, final HashMap<String,String> hashMap, final String method){

        if(getActivity() != null) {

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {

                    if (method.equals("get_new_booking")) {

                        uiUpdate(response);

                    } else if (method.equals("accept_booking")) {

                        acceptSuccess(response);

                    }

                }

            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                }

            }) {

                @Override
                public HashMap<String, String> getParams() {
                    return hashMap;
                }

            };

            stringRequest.setTag(TAG);
            Helper.addToRequestQue(requestQueue, stringRequest, getActivity());

        }

    }

    public void uiUpdate(String response)
    {

        if(getActivity() != null) {

            try {

                JSONObject jsonObject = new JSONObject(response);
                String errFlag = jsonObject.getString("errFlag");

                if (errFlag.equals("1")) {

                    ErrorDialog(Constants.Title.SERVER_ERROR, Constants.Message.SERVER_ERROR);

                } else if (errFlag.equals("0")) {

                    if (jsonObject.has("likes")) {

                        JSONArray jsonArray = jsonObject.getJSONArray("likes");
                        int count = 0;

                        while (count < jsonArray.length()) {

                            JSONObject JO = jsonArray.getJSONObject(count);
                            received_crn_no = JO.getString("crn_no");
                            String received_booking_datetime = JO.getString("booking_datetime");
                            String received_vehicletype_id = JO.getString("vehicletype_id");
                            String received_customer_mobile_no = JO.getString("customer_mobile_no");
                            String received_material_weight = JO.getString("material_weight");
                            String received_pickup_point = JO.getString("pickup_point");
                            String received_dropoff_point = JO.getString("dropoff_point");
                            crn_no.setText(received_crn_no);
                            booking_datetime.setText("Booking DateTime: " + Helper.getDateName(received_booking_datetime));
                            vehicle_type.setText("Truck: " + Helper.VehicleName(received_vehicletype_id, getActivity()));
                            customer_mobile_no.setText(received_customer_mobile_no);
                            material_weight.setText("Material Weight: " + received_material_weight);
                            pickup_point.setText(received_pickup_point);
                            dropoff_point.setText(received_dropoff_point);
                            String received_material_image_url = JO.getString("material_image_url");
                            String download_received_material_image_url = Constants.Config.ROOT_PATH + received_material_image_url;
                            Helper.logD("download_received_material_image_url", download_received_material_image_url);

                            if (received_material_image_url.length() > 0) {

                                downloadBitmapFromURL(download_received_material_image_url);

                            } else {

                                material_image.setImageResource(R.drawable.addcontact);
                                material_image.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        popup.setImageResource(R.drawable.addcontact);
                                        dialog.show();

                                    }
                                });

                            }

                            count++;

                        }

                    } else {

                        Helper.Toast(getActivity(), Constants.Message.NEW_USER_ENTER_DETAILS);

                    }

                }

            } catch (JSONException e) {

                e.printStackTrace();

            }

        }

    }

    public void acceptSuccess(String response){

        if(getActivity() != null) {

            if (Helper.CheckJsonError(response) == false) {

                Fragment fragment = new Fragment();
                fragment = new BookingDetails();
                Bundle bundle = new Bundle();
                bundle.putString("crn_no", received_crn_no);
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = FullActivity.fragmentManager;
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.main_content, fragment, Constants.Config.CURRENT_FRAG_TAG);

                if ((FullActivity.homeFragmentIndentifier == -5)) {

                    transaction.addToBackStack(null);
                    FullActivity.homeFragmentIndentifier = transaction.commit();

                } else {

                    transaction.commit();

                }

                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_bill_detail_fragment);

            }

        }

    }

    public void downloadBitmapFromURL(String profile_pic_url){

        if(getActivity() != null) {

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
            Helper.addToRequestQue(requestQueue, imageRequest, getActivity());

        }

    }

    private void ErrorDialog(String Title,String Message){

        if(getActivity() != null) {

            Helper.showDialog(getActivity(), Title, Message);

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