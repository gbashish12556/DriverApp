package in.co.theshipper.www.shipper_driver;

public final class Constants {
    public static final class Config{

        public static final String ROOT_PATH = "http://theshipper.ml/loader_mobile/";
        public static final int UPDATE_NEW_LOCATION_DELAY = 0*1000;
        public static final int UPDATE_NEW_LOCATION_PERIOD = 10*1000;
        public static final int UPDATE_DRIVER_LOCATION_DELAY = 0*1000;
        public static final int UPDATE_DRIVER_LOCATION_PERIOD = 30*1000;
        public static final int GET_CUSTOMER_LOCATION_DELAY = 0*1000;
        public static final int GET_CUSTOMER_LOCATION_PERIOD = 30*1000;
        public static final int SEND_DISTANCE_REQUEST_DELAY = 0*1000;
        public static final int SEND_DISTANCE_REQUEST_PERIOD = 120*1000;
        public static final long MIN_DATE_DURATION = 1*1000;
        public static final long MAX_DATE_DURATION = 6*24*60*60*1000;
        public static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
        public static final long MIN_TIME_BW_UPDATES = 10000 * 1 * 1;
        public static final String SUPPORT_CONTACT = "08276097972";
        public static final int NAME_FIELD_LENGTH = 50;
        public static final int ADDRESS_FIELD_LENGTH = 50;
        public static final String CURRENT_FRAG_TAG = "current_fragment";
        public static final float MAP_HIGH_ZOOM_LEVEL = 17;
        public static final float MAP_MID_ZOOM_LEVEL = 15;
        public static final float MAP_SMALL_ZOOM_LEVEL = 13;
        public static final int DELAY_LOCATION_CHECK = 1000*2*1;
        public static final int FLASH_TO_MAIN_DELAY = 3*1000;
        public static final int GPS_INTERVAL = 10*1000;
        public static final int GPS_FASTEST_INTERVAL = 5*1000;
        public static final int PROGRESSBAR_DELAY = 2*1000;
        public static final double ACURATE_DISTANCE_RATIO_FACTOR = 1.5;
        public static final int IMAGE_WIDTH = 500;
        public static final int IMAGE_HEIGHT = 500;

    }

    public static final class Message{

        public static final String NEW_USER_ENTER_DETAILS = "Please enter your details";
        public static final String NO_CURRENT_BOOKING = "No Current Booking";
        public static final String VEHICLE_ALLOCATION_PENDING = "Vehicle Allocation Pending";
        public static final String DRIVER_FOUND = "Driver Found";
        public static final String NETWORK_ERROR = "Unable to connect to server.Check your Internet Connection";
        public static final String SERVER_ERROR = "Server not responding to request";
        public static final String FIELD_MISSING = "Some fields are missing...Retry";
        public static final String INV_CRED = "Invalid Credentials!!";
        public static final String GPS_NOT_ENABLED = "GPS not enabled !!";
        public static final String INTERNET_NOT_ENABLED = "Internet not enabled!!";
        public static final String CONNECTING = "Connecting...";
        public static final String LOADING = "Loading...";
        public static final String OTP_VERIFICATION_ERROR = "OTP could not be verified";
        public static final String FORM_ERROR = "Form contains error";
        public static final String TRACKING_ERROR = "Error while updating location";
        public static final String EMPTY_IMAGE = "Upload your Image";

    }

    public static final class Title{

        public static final String NETWORK_ERROR = "NETWORK ERROR";
        public static final String SERVER_ERROR = "SERVER ERROR";
        public static final String OTP_VERIFICATION_ERROR = "VERIFICATION ERROR";

    }

    public static final class Keys{

        public static final String VEHICLETYPE_ID = "vehicletype_id";
        public static final String USER_TOKEN = "user_token";
        public static final String CRN_NO = "crn_no";
        public static final String USER_ID = "user_id";
        public static final String CITY_ID = "city_id";
        public static final String EXACT_PICKUP_POINT = "exact_pickup_point";
        public static final String EXACT_DROPOFF_POINT = "exact_dropoff_point";
        public static final String LOADING_START_TIME = "loading_start_time";
        public static final String UNLOADING_STOP_TIME = "unloading_stop_time";
        public static final String JOURNEY_START_TIME = "journey_start_time";
        public static final String JOURNEY_STOP_TIME = "journey_stop_time";
        public static final String TOTAL_DISTANCE_TAVELLED = "total_distance_travelled";

    }

}