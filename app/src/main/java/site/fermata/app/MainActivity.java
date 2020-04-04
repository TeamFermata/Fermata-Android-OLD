/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.tedpark.tedpermission.rx2.TedRx2Permission;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static site.fermata.app.Constants.ACTION_RERUN;
import static site.fermata.app.Constants.SERVER_URL;

/**
 * Setup display fragments and ensure the device supports Bluetooth.
 */
public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private BluetoothAdapter mBluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);


        SharedPreferences prf = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());



        if(prf.contains("id")&&prf.contains("key")   ) {






        } else {

            Intent i = new Intent( this, SignUpActivity.class);
            finish();  //Kill the activity from which you will go to next activity
            startActivity(i);


            return ;
        }



        if (savedInstanceState == null) {

            mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                    .getAdapter();

            // Is Bluetooth supported on this device?
            if (mBluetoothAdapter != null) {

                // Is Bluetooth turned on?
                if (mBluetoothAdapter.isEnabled()) {

                    // Are Bluetooth Advertisements supported on this device?
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {

                        // Everything is supported and enabled, load the fragments.

                        start();




                    } else {

                        // Bluetooth Advertisements are not supported.
                        toast(R.string.bt_ads_not_supported);
                        finish();
                    }
                } else {

                    // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
                }
            } else {

                // Bluetooth is not supported.
                toast(R.string.bt_not_supported);
                finish();
            }
        }


        setContentView(R.layout.activity_main);
        setTitle(R.string.activity_main_title);


        ( (Button) findViewById(R.id.new_signal_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this,AdvertiserService.class));
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

    private void toast(int s) {
        Toast.makeText(this,s,
                Toast.LENGTH_SHORT).show();

    }

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