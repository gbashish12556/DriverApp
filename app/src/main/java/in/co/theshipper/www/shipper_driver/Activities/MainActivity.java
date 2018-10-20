package in.co.theshipper.www.shipper_driver.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Random;

import in.co.theshipper.www.shipper_driver.Constants;
import in.co.theshipper.www.shipper_driver.Helper;
import in.co.theshipper.www.shipper_driver.R;
import in.co.theshipper.www.shipper_driver.Utils.FormValidation;

public class MainActivity extends AppCompatActivity
{
    private  String TAG = MainActivity.class.getName();
    private  EditText MOBILE_NO;
    public  RequestQueue requestQueue;
    private int otp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        MOBILE_NO = (EditText) findViewById(R.id.editText);

    }
    public void userReg(View view){

        if ( checkValidation () ) {

            String reg_url = Constants.Config.ROOT_PATH + "driver_registration";
            Random ran = new Random();
            otp = (100000 + ran.nextInt(900000));
            String mobile_no = MOBILE_NO.getText().toString();
            HashMap<String,String> hashMap = new HashMap<String, String>();
            hashMap.put("mobile_no", mobile_no);
            hashMap.put("OTP", String.valueOf(otp));
            Helper.putPreference(this, "OTP", String.valueOf(otp));
            Helper.putPreference(this, "mobile_no", mobile_no);
            Helper.logD("OTP", Helper.getPreference(this, "OTP"));
            sendVolleyRequest(reg_url, Helper.checkParams(hashMap));

        }
        else {

            Toast.makeText(MainActivity.this, "Form contains error", Toast.LENGTH_LONG).show();
        }

    }

    private boolean checkValidation() {
        boolean ret = true;

        if (!FormValidation.isPhoneNumber(MOBILE_NO, true)) ret = false;

        return ret;
    }

    public void sendVolleyRequest(String URL, final HashMap<String,String> hMap){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                String trimmed_response = response.substring(response.indexOf("{"));
                registerSuccess(trimmed_response);

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                ErrorDialog(Constants.Title.NETWORK_ERROR,Constants.Message.NETWORK_ERROR);

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

    public void registerSuccess(String response){

        if(!Helper.CheckJsonError(response)) {

            Intent intent = new Intent(this, OtpVerification.class);
            intent.putExtra("OTP", otp);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }else{

            ErrorDialog(Constants.Title.SERVER_ERROR, Constants.Message.SERVER_ERROR);

        }

    }

    private void ErrorDialog(String Title,String Message){

        Helper.showDialog(this, Title, Message);

    }

    @Override
    public void onRestart() {
        super.onRestart();

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
    public void onDestroy() {
        super.onDestroy();

        Helper.cancelAllRequest(requestQueue,TAG);

     }

}
