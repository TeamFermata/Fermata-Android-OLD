package site.fermata.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import androidx.preference.PreferenceManager;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import site.fermata.app.db.AppDatabase;

import static site.fermata.app.Constants.IS_BETA;
import static site.fermata.app.Constants.PREF_ID;
import static site.fermata.app.Constants.SERVER_URL;

public class ScanReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        PendingResult p = goAsync();
        SharedPreferences prf = PreferenceManager.getDefaultSharedPreferences(context);




        if(intent.hasExtra("uuid")) {
            OkHttpClient client;  client=HttpClient.get();
            String[] uuid=intent.getStringArrayExtra("uuid");
            final int time=intent.getIntExtra("time",0);
            JSONObject jsonObject=new JSONObject();
            try {

                jsonObject.put("myID", prf.getString(PREF_ID,""));
                jsonObject.put("record",  new JSONArray(uuid));

            } catch (Exception e){
                Log.d("",e.toString());
            }



         HttpClient.checkAuth(prf,context)

                 .flatMap(

                         new Function<String, SingleSource<String>>() {
                             @Override
                             public SingleSource<String> apply(String ss) throws Exception {
                                 jsonObject.put("sessionID",ss);
                                 jsonObject.put("path","records")
                                         .put("method","put");

                                 Request request = new Request.Builder()
                                         .url(SERVER_URL)
                                         //.get()
                                         .post(RequestBody.create( jsonObject.toString(),MediaType.parse("application/json")))
                                         .build();
                                    if(BuildConfig.DEBUG){
                                        Log.d("d", jsonObject.toString());

                                    }

                                 Response responses = client.newCall(request).execute();

                                 String jsonData = responses.body().string();

                                 JSONObject json = new JSONObject(jsonData);
                                 String code= json.getString("code");
                                 if(code.equals("success")) {

                                     String session= json.getString("sessionID");

                                     prf.edit().putString("session",session).apply();


                                     AppDatabase instance = AppDatabase
                                             .getInstance(context);

                                         instance
                                                 .getSignalLogDao()
                                                 .flagUploaded(time);

                                     return Single.just("");
                                 } else {

                                     if(code.equals("fail_auth")) {

                                         prf.edit().remove("session").apply();
                                     }

                                     return  Single.timer(3, TimeUnit.SECONDS).flatMap(s->HttpClient.checkAuth(prf,context)) .flatMap(this);

                                 }

                             }
                         }

                         ) .timeout(10, TimeUnit.SECONDS).subscribeOn(Schedulers.io())

                  .  subscribe(new SingleObserver<String>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onSuccess(String s)

                {

                if(IS_BETA)  {
                    Toast.makeText(context,"저장성공",Toast.LENGTH_LONG).show();
                }
                    p.finish();
                }

                @Override
                public void onError(Throwable e) {
                    Log.d("Scanreciver:err" ,e.toString());

                    if(IS_BETA)  {
                        Toast.makeText(context,"저장실패,재시도",Toast.LENGTH_LONG).show();
                    }

                    p.finish();
                }
            });



        }

        else {
            // Set the alarm here.

            boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                    new Intent(context, AdvertiserService.class),
                    PendingIntent.FLAG_NO_CREATE) != null);
            if(alarmUp) return;

            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pendingIntent = PendingIntent.getForegroundService(context, 0 ,new Intent(context, AdvertiserService.class),0);
                context.startForegroundService(new Intent(context, AdvertiserService.class));
            } else {
                pendingIntent = PendingIntent.getService(context, 0 ,new Intent(context, AdvertiserService.class),0);
                context.startService(new Intent(context, AdvertiserService.class));

            }


            AlarmManager alarmManager = (AlarmManager)  context.getSystemService(context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                    AlarmManager.INTERVAL_HOUR, pendingIntent);



                p.finish();
        }
    }
}
