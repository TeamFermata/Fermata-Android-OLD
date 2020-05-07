/*
    Copyright © 2007 Free Software Foundation, Inc. <https://fsf.org/>
    Everyone is permitted to copy and distribute verbatim copies of this license document, but changing it is not allowed.
    Github Repository : https://github.com/TracetogetherKorea/Fermata-Android

    - 메인 액티비티 -
 */

package site.fermata.app;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gun0912.tedpermission.BuildConfig;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.tedpark.tedpermission.rx2.TedRx2Permission;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import site.fermata.app.db.AppDatabase;

import static site.fermata.app.Constants.CHECH_MINSEC;
import static site.fermata.app.Constants.PREF_ID;


/**
 * Setup display fragments and ensure the device supports Bluetooth.
 */
public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private BluetoothAdapter mBluetoothAdapter;

    //앱 시작시
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prf = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        if(prf.contains(PREF_ID)&&prf.contains("key")) {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
        } else {
            super.onCreate(savedInstanceState);
            Intent i = new Intent( this, SignUpActivity.class);
            finish();  //Kill the activity from which you will go to next activity
            startActivity(i);
            return ;
        }

        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        setTitle(R.string.activity_main_title);

        if (savedInstanceState == null) {
            mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                    .getAdapter();
            // 블루투스 지원여부
            if (mBluetoothAdapter != null) {
                // 블루투스 켜짐 여부
                if (mBluetoothAdapter.isEnabled()) {
                    // BLE 송수신 지원여부
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                        // 모든게 지원할 시 앱 시작
                        start();
                    } else {
                        // BLE 송수신 미지원
                        toast(R.string.bt_ads_not_supported);
                        finish();
                    }
                } else {
                    // 블루투스 켜기 요구하기
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
                }
            } else {
                // 블루투스 미지원
                toast(R.string.bt_not_supported);
                finish();
            }

        }

        //베타버전 안내(추후 메인버전에선 앱 주요안내)
        findViewById(R.id.HowToUse).setOnClickListener(new View.OnClickListener() { //구 R.id.about_btn
            @Override
            public void onClick(View v) {
              //  toast("준비중입니다.");

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://blog.fermata.site/1"));
                startActivity(browserIntent);
            }
        });

        //뭐하는 기능인지 추가바람
        findViewById(R.id.manual_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              //  toast("준비중입니다.");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/e/2PACX-1vTqkxxRmJ-EIUzQb533qR_n_pDVLizbuUpfUz3UCuDv4DhAIPdy8eIIaXUa06KFnyUakha3ViFIKQdz/pub"));
                startActivity(browserIntent);
            }
        });

        //확진여부 확인버튼
        findViewById(R.id.ScanInfection).setOnClickListener(new View.OnClickListener() { //구 R.id.query_btn
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),AskActivity.class));
            }
        });

        //확진자 등록버튼
        findViewById(R.id.InsertInfection).setOnClickListener(new View.OnClickListener() { //구 R.id.insert_btn
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),InsertActivity.class));
            }
        });

        //깃허브 URL 아이콘
        findViewById(R.id.GithubICON).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TracetogetherKorea"));
                startActivity(browserIntent);
            }
        });

        //티스토리 기술블로그 아이콘
        findViewById(R.id.TistoryTechBlogICON).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://blog.fermata.site/"));
                startActivity(browserIntent);
            }
        });

     /*   ( (Button) findViewById(R.id.new_signal_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this,AdvertiserService.class));
            }
        });
      */

       if(!BuildConfig.DEBUG) ((Button)findViewById(R.id.send_button)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.send_button)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //데이터베이스 로드
                        AppDatabase instance = AppDatabase
                                .getInstance(getApplicationContext());
                        int now= (int) (System.currentTimeMillis()/1000);

                        //접촉 기록 불러오기
                        String[] records =instance.getSignalLogDao().getUUIDRecord(now-CHECH_MINSEC);
                        if(records!=null& records.length>0){ //전송할 접촉기록이 있다면
                            getApplicationContext().sendBroadcast(new Intent(getApplicationContext(), ScanReceiver.class).putExtra("uuid", records)
                                    .putExtra("time",now));
                        } else { //전송할 접촉기록이 없다면
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toast("전송할 로그가 없습니다. 다른 기기에 설치해서 신호를 주고 받아야 저장됩니다.");
                                }
                            });
                        }

                    }
                }) .start() ;
            }
        });
    }

    private void start() {
        TedRx2Permission.with(this)
                .setRationaleTitle(R.string.rationale_title)
                .setRationaleMessage(R.string.rationale_message) // "we need permission for read contact and find your location"
                .setDeniedMessage("사용하시려면  [설정] > [앱] >[권한] 에서 권한을 직접 변경해주셔야 합니다.")

                .setPermissions( Manifest.permission.ACCESS_FINE_LOCATION)
                .request()
                .subscribe(tedPermissionResult -> {
                    if (tedPermissionResult.isGranted()) {
                       //toast("권한이 수락되었습니다.");
                        MainActivity.this.sendBroadcast(new Intent( MainActivity.this , ScanReceiver.class));
                    } else {
                        toast("권한이 거부되었습니다. 사용하시려면 다시 시작해주세요");
                        finish();
                    }
                }, throwable -> {
                });
    }

    //Toast 띄우기(스트링 리소스)
    private void toast(int s) {
        Toast.makeText(this,s,
                Toast.LENGTH_SHORT).show();
    }

    //Toast 띄우기(스트링)
    private void toast(String s) {
                Toast.makeText(this,s,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:

                if (resultCode == RESULT_OK) {

                    // Bluetooth is now Enabled, are Bluetooth Advertisements supported on
                    // this device?

                    // Bluetooth Advertisements are not supported.
                    start();
                } else {
                    // User declined to enable Bluetooth,
                    toast(R.string.bt_not_enabled_leaving);
                    finish();
                }

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

}