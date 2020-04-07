package site.fermata.app;

import android.content.SharedPreferences;

import org.json.JSONObject;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import static site.fermata.app.Constants.SERVER_URL;

public class HttpClient {

    static   OkHttpClient client;

    public static  OkHttpClient get() {

        if(client!=null) return client;

        if(BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();
        } else {
            client = new OkHttpClient();
        }

        return client;
    }

    public static  Single login(SharedPreferences prf) {

        return prf.contains("session")?  Single.just(prf.getString("session","")) :Single.create(new SingleOnSubscribe<String>() {


            @Override
            public void subscribe(SingleEmitter<String> emitter) throws Exception {
                String  session;





                Request request = new Request.Builder()
                        //.addHeader("x-api-key", RestTestCommon.API_KEY)
                        .url(SERVER_URL+"/api/user")
                        //.get()
                        .post(RequestBody.create(MediaType.parse("application/json"),    String.format("{\"id\":\"%s\",\"pw\":\"%s\"}", prf.getString("id",""),prf.getString("key",""))))
                        .build();


                Response responses = get().newCall(request).execute();

                String jsonData = responses.body().string();

                JSONObject json = new JSONObject(jsonData);
                String code= json.getString("code");
                if(code.equals("success")) {

                    session= json.getString("sessionID");

                    prf.edit().putString("session",session).apply();


                } else {

                    throw new Exception();
                }

                emitter.onSuccess(session);

            }
        });

    }


 }
