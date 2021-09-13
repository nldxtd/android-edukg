package com.lapluma.knowledg.util;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jakewharton.disklrucache.DiskLruCache;
import com.lapluma.knowledg.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.Buffer;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class Network {
    /** everything about network here.
     */

    public static final String BASE_URL = "http://api-java.definition.work";
    public static final String URL = "42.193.124.185";

    @NoArgsConstructor
    @AllArgsConstructor
    @Setter
    @Getter
    public static class RestResponse<T> {
        /** generic class for rest responses.
         * always deserialize json response through this.
         * if is pure message response, set T as anything, e.g. Object.
         */
        private int code;
        private String message;
        private T data;
        public boolean isSuccessful() {
            return code < 300 && code >= 200;
        }
    }

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();   // universal exclusive http client (suggested in official docs)

    private static final int MAX_RELOAD_TIME = 2;
    private static int reloadTime = 0;
    private static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");
    private static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static class Api {
        public static final String POST_SIGN_IN = BASE_URL + "/users/login";
        public static final String POST_SIGN_UP = BASE_URL + "/users/register";
        public static final String POST_SIGN_OUT = BASE_URL + "/users/logout";
        public static final String POST_CHAT = BASE_URL + "/query";
        public static final String POST_INFO_RELATION = BASE_URL + "/info/relation";
        public static final String POST_REFRESH = BASE_URL + "/refresh";
        public static final String POST_GET_MORE = BASE_URL + "/getmore";
        public static final String POST_INFO_PROPERTY = BASE_URL + "/info/property";
        public static final String POST_IS_STARRED = BASE_URL + "/isstarred";
        public static final String POST_STAR = BASE_URL + "/star";
        public static final String POST_UNSTAR = BASE_URL + "/unstar";
        public static final String POST_SEARCH = BASE_URL + "/search";
        public static final String POST_SEARCH_HISTORY = BASE_URL + "/search/history";
        public static final String POST_CLEAR = BASE_URL + "/search/history/clear";
        public static final String POST_HISTORY = BASE_URL + "/info/history";
        public static final String POST_COLLECTION = BASE_URL + "/favorite";
        public static final String POST_DISCOVER = BASE_URL + "/explore";
        public static final String POST_QUESTION = BASE_URL + "/question";
    }


    private static void doOnFailure(Call call, IOException e, Activity activity, Callback callback) {
        /** default behavior when callback onFailure() is called within okhttp.
         * that is, judge exception type and rise an error snackbar. */
        if (e instanceof SocketTimeoutException && reloadTime <= MAX_RELOAD_TIME) { // timeout and still have to reload
            reloadTime++;
            client.newCall(call.request()).enqueue(callback);
        } else if (e instanceof SocketTimeoutException) {
            e.printStackTrace();
            Tool.makeSnackBar(activity, activity.getResources().getString(R.string.error_time_out));
        } else if (e instanceof ConnectException) {
            e.printStackTrace();
            Tool.makeSnackBar(activity, activity.getResources().getString(R.string.error_connection_fail));
        }
        reloadTime = 0;
    }

    private static <T> void doOnResponse(Call call, Response response, Activity activity, TypeReference<RestResponse<T>> typeRef, CallbackOnResponse<T> callback) throws IOException {
        /** default behavior when callback onResponse() is called within okhttp.
         * that is, deserialize json and load data setTargetView specific class T. */
        ResponseBody body = response.body();
        if (body != null) {
            try {
                RestResponse<T> restResponse = mapper.readValue(body.string(), typeRef);
                if (restResponse.isSuccessful()) {
                    callback.processResponse(restResponse);
                } else {
                    Tool.makeSnackBar(activity, restResponse.message);
                }
            } catch (JacksonException e) {
                e.printStackTrace();
                Tool.makeSnackBar(activity, activity.getResources().getString(R.string.error_json_deserialization_fail));
            }
        } else {
            Tool.makeSnackBar(activity, activity.getResources().getString(R.string.error_empty_body));
        }
    }

    public interface CallbackOnResponse<T> {
        /** a generic interface.
         * the structure of class T should match the json structure of response field "data". */
        default void processResponse(RestResponse<T> restResponse) {}
    }

    public static abstract class Postman {
        protected String url;
        protected final Activity activity;
        public Postman(Activity activity, String url) {
            /** Postman in between activity and remote url */
            this.activity = activity;
            this.url = url;
        };
        public abstract <T> void post(TypeReference<RestResponse<T>> typeRef, CallbackOnResponse<T> callback);
    }

    public static class JsonPostman extends Postman {
        private final JSONObject json;
        public JsonPostman(Activity activity, String url) {
            super(activity, url);
            json = new JSONObject();
        }
        public void put(String key, String value) {
            try {
                json.put(key, value);
            } catch (JSONException e) {
                Tool.makeSnackBar(activity, activity.getResources().getString(R.string.error_json_serialization_fail));
            }
        }
        public void put(String key, int value) {
            try {
                json.put(key, value);
            } catch (JSONException e) {
                Tool.makeSnackBar(activity, activity.getResources().getString(R.string.error_json_serialization_fail));
            }
        }
        public <T> void post(TypeReference<RestResponse<T>> typeRef, CallbackOnResponse<T> callback) {
            RequestBody requestBody = RequestBody.create(String.valueOf(json), JSON);
            Request request = new Request.Builder().url(url).post(requestBody).build();
            client.newCall(request).enqueue(new Callback(){
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    doOnFailure(call, e, activity, this);
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    doOnResponse(call, response, activity, typeRef, callback);
                }
            });
        }
        public void setUrl(String newUrl) {
            url = newUrl;
        }
    }

    public static abstract class Loader {
        /** load some remote resource setTargetView specific view */
        protected final Activity owner;
        protected String url;
        protected View view;
        public Loader(Activity owner) {
            this.owner = owner;
        }
        public void setUrl(String url) {
            this.url = url;
        }
        public void setTargetView(View view) {
            this.view = view;
        }
    }

    public static class ImageLoader extends Loader{
        private Cache.BitmapCacheHelper cacheHelper;
        private final String baseUrl = "https://edukg.psmoe.com/";
        public ImageLoader(Activity owner) {
            super(owner);
            cacheHelper = new Cache.BitmapCacheHelper(owner);
        }
        public void load(String keyword) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        if (cacheHelper.loadBitmap(keyword, (ImageView) view)) {
                            return;
                        }
                        URL url = new URL(ImageLoader.this.baseUrl + keyword + ".jpg");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(10000);
                        connection.setDoInput(true);
                        int code = connection.getResponseCode();
                        if (code >= 200 && code < 400) {
                            InputStream inputStream = connection.getInputStream();
                            Bitmap image = BitmapFactory.decodeStream(inputStream);
                            inputStream.close();
                            if (view != null) {
                                owner.runOnUiThread(() -> {
                                    ((ImageView) view).setImageBitmap(image);
                                });
                            }
                            cacheHelper.saveBitmap(keyword, image);
                        }
                        else {
                            Tool.makeSnackBar(owner, owner.getString(R.string.error_resource_server_error));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Tool.makeSnackBar(owner, owner.getString(R.string.error_connection_fail));
                    }
                }
            }.start();
        }
    }

    public static boolean isNetworkAvailable() {
        String result = null;
        try {
            Process p = Runtime.getRuntime().exec("ping -c 2 -W 200 " + URL);
            int status = p.waitFor();
            if (status == 0) {
                return true;
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            System.out.println(result);
        }
        return false;
    }
}
