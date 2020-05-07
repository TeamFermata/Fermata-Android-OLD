/*
    Copyright © 2007 Free Software Foundation, Inc. <https://fsf.org/>
    Everyone is permitted to copy and distribute verbatim copies of this license document, but changing it is not allowed.
    Github Repository : https://github.com/TracetogetherKorea/Fermata-Android

    - RESTFUL API HTTPClient -
 */

package site.fermata.app;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import static site.fermata.app.Constants.PREF_ID;
import static site.fermata.app.Constants.SERVER_URL;

public class HttpClient {

    static   OkHttpClient client;

    public static  OkHttpClient get() {

        if(client!=null) return client;

        if(BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .build();
        } else {
            client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
            .build();
        }

        return client;
    }

    public static  Single checkAuth(SharedPreferences prf, Context context) {

        return prf.contains("session")?  Single.just(prf.getString("session","")) :Single.create(new SingleOnSubscribe<String>() {


            @Override
            public void subscribe(SingleEmitter<String> emitter) throws Exception {
                String  session;


                JSONObject jsonObject = new JSONObject();
                jsonObject.put("path","user")
                        .put("method","post")

                .put("id",  prf.getString(PREF_ID,""))
                        .put("password", prf.getString("key",""));


                Request request = new Request.Builder()
                        //.addHeader("x-api-key", RestTestCommon.API_KEY)
                        .url(SERVER_URL)
                        //.get()
                        .post(RequestBody.create(jsonObject.toString(),MediaType.parse("application/json")

                                 ))
                        .build();


                Response responses = get().newCall(request).execute();

                String jsonData = responses.body().string();

                JSONObject json = new JSONObject(jsonData);
                String code= json.getString("code");
                if(code.equals("success")) {

                    session = json.getString("sessionID");

                    prf.edit().putString("session", session).apply();
                 if(!emitter.isDisposed())   emitter.onSuccess(session);

                }else if(code.equals("fail_not_found")||code.equals("fail_invalidpw")){

                    if(!emitter.isDisposed())  emitter.onError(new Exception());

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,"사용자 정보를 확인하지 못했습니다. 다시 시작합니다.",
                                    Toast.LENGTH_SHORT).show();

                            Intent i = new Intent( context, SignUpActivity.class);

                            context. startActivity(i);
                        }
                    });

                } else {
                    if(!emitter.isDisposed())  emitter.onError(new Exception());
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,"사용자 정보를 확인하지 못했습니다. 잠시 후 다시 시도해주세요",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    //
                    //  throw ;
                }


            }
        });

    }


 }
