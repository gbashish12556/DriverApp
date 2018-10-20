package in.co.theshipper.www.shipper_driver.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.HashMap;

import in.co.theshipper.www.shipper_driver.Constants;
import in.co.theshipper.www.shipper_driver.Helper;
import in.co.theshipper.www.shipper_driver.R;

public class RegistrationIntentService extends IntentService {

    // abbreviated tag name
    public RequestQueue requestQueue;
    public HashMap<String,String> hashMap;
    private static final String TAG = RegistrationIntentService.class.getName();
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String GCM_TOKEN = "gcmToken";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    public void onHandleIntent(Intent intent) {

        // Make a call to Instance API
        String token = "";
        InstanceID instanceID = InstanceID.getInstance(this);
        String senderId = getResources().getString(R.string.gcm_senderID);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {

            // request token that will be used by the server to send push notifications
            token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);

            // pass along this data
            sendRegistrationToServer(token);

        } catch (IOException e) {
            e.printStackTrace();

            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();

        }

        // save token
        sharedPreferences.edit().putString(GCM_TOKEN, token).apply();

        // pass along this data
        sendRegistrationToServer(token);

    }
    public void sendRegistrationToServer(String token) {

        // send network request
        // if registration sent was successful, store a boolean that indicates whether the generated token has been sent to server
        String update_device_id_url = Constants.Config.ROOT_PATH+"update_driver_device";
        String user_token = Helper.getPreference(this,"user_token");
        hashMap = new HashMap<String,String>();
        hashMap.put("gcm_regid",token);
        hashMap.put("user_token", user_token);
        sendVolleyRequest(update_device_id_url, hashMap);

    }
    public void sendVolleyRequest(String URL, final HashMap<String,String> hMap){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                UpdateDeviceSuccess(response);

            }

            }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

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

    public void UpdateDeviceSuccess(String response){

        if(!Helper.CheckJsonError(response)){

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();

        }

    }

}
