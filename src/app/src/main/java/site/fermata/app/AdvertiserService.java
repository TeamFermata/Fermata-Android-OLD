/*
    Copyright © 2007 Free Software Foundation, Inc. <https://fsf.org/>
    Everyone is permitted to copy and distribute verbatim copies of this license document, but changing it is not allowed.
    Github Repository : https://github.com/TracetogetherKorea/Fermata-Android

    - 페르마타 표준 BLE 송수신 클래스 -
 */

package site.fermata.app;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import site.fermata.app.db.AppDatabase;
import site.fermata.app.db.SignalLog;
import site.fermata.app.db.TempSignal;

import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static site.fermata.app.Constants.CHECH_MINSEC;
import static site.fermata.app.Constants.IS_BETA;
import static site.fermata.app.Constants.Service_UUID;

/**
 * Manages BLE Advertising independent of the main app.
 * If the app goes off screen (or gets killed completely) advertising can continue because this
 * Service is maintaining the necessary Callback in memory.
 */
public class    AdvertiserService extends Service {

    private static final String TAG = AdvertiserService.class.getSimpleName();

    private static final int FOREGROUND_NOTIFICATION_ID = 1;
    private static final long SCAN_PERIOD = 5000*1000;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeScanner mBluetoothLeScanner;

    private ScanCallback mScanCallback;

    private Handler mHandler;
    /**
     * A global variable to let AdvertiserFragment check if the Service is running without needing
     * to start or bind to it.
     * This is the best practice method as defined here:
     * https://groups.google.com/forum/#!topic/android-developers/jEvXMWgbgzE
     */
    public static boolean running = false;
    NotificationHelper mNotificationHelper;
    public static final String ADVERTISING_FAILED =
        "com.example.android.bluetoothadvertisements.advertising_failed";

    public static final String ADVERTISING_FAILED_EXTRA_CODE = "failureCode";

    public static final int ADVERTISING_TIMED_OUT = 6;

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    private AdvertiseCallback mAdvertiseCallback;


    private Runnable timeoutRunnable;

    /**
     * Length of time to allow advertising before automatically shutting off. (10 minutes)
     */
    private long TIMEOUT = TimeUnit.MILLISECONDS.convert(10, TimeUnit.DAYS);
    private Context context;

    @Override
    public void onCreate() {
        running = true;

        mNotificationHelper = new NotificationHelper(this);
        initialize();
        goForeground();
        context=getApplicationContext();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        startScanning();
       // setTimeout();


        super.onCreate();
    }

    @Override
    public void onDestroy() {
        /**
         * Note that onDestroy is not guaranteed to be called quickly or at all. Services exist at
         * the whim of the system, and onDestroy can be delayed or skipped entirely if memory need
         * is critical.
         */
        running = false;
        stopAdvertising();
      //  mHandler.removeCallbacks(timeoutRunnable);
        stopForeground(true);
        super.onDestroy();
    }

    /**
     * Required for extending service, but this will be a Started Service only, so no need for
     * binding.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        /*boolean run=true;
        if(cachedUUID!=null){
            for( Map.Entry<String, Integer> elem : cachedUUID.entrySet() ){
               int g=elem.getValue();
                if(g<0) continue;
                if( now< g+CHECH_MINSEC   ) run=false;
            }



        }
*/
  //      if(run) {
            if (mBluetoothLeAdvertiser != null) stopAdvertising();
            startAdvertising();

            Calendar rightNow = Calendar.getInstance();
            int hour = rightNow.get(Calendar.HOUR_OF_DAY);


        new Thread(new Runnable() {
            @Override
            public void run() {


                AppDatabase instance = AppDatabase
                        .getInstance(context);
                if( hour ==3) {
                    instance
                            .getTempSignalDao()
                            .deleteOld();
                    instance
                            .getSignalLogDao()
                            .deleteOld();

                }



                int now= (int) (System.currentTimeMillis()/1000);

             String[] records =instance.getSignalLogDao().getUUIDRecord(now-CHECH_MINSEC);

              if(records!=null& records.length>0){

                  context.sendBroadcast(new Intent( context , ScanReceiver.class).putExtra("uuid",records)
                  .putExtra("time",now));


              }



            }
        }) .start() ;





        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Get references to system Bluetooth objects if we don't have them already.
     */
    private void initialize() {
        if (mBluetoothLeAdvertiser == null) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null) {
                 mBluetoothAdapter = mBluetoothManager.getAdapter();

               // mBluetoothAdapter.setName("zhfhskwnrdlrl");
                if (mBluetoothAdapter != null) {
                    mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();


                } else {

                    stopped("블루투스 꺼짐");
                   // Toast.makeText(this, getString(R.string.bt_null), Toast.LENGTH_LONG).show();
                }
            } else {

                stopped("시작오류");
               // Toast.makeText(this, getString(R.string.bt_null), Toast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * Starts a delayed Runnable that will cause the BLE Advertising to timeout and stop after a
     * set amount of time.
     */
    private void setTimeout(){
        mHandler = new Handler();
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "AdvertiserService has reached timeout of "+TIMEOUT+" milliseconds, stopping advertising.");
                sendFailureIntent(ADVERTISING_TIMED_OUT);
                stopSelf();
            }
        };
        mHandler.postDelayed(timeoutRunnable, TIMEOUT);
    }

    /**
     * Starts BLE Advertising.
     */
    private void startAdvertising() {

        Log.d(TAG, "Service: Starting Advertising");

        if (mAdvertiseCallback == null) {
            AdvertiseSettings settings = buildAdvertiseSettings();

            byte[] b = new byte[9];
            new Random().nextBytes(b);


            AdvertiseData data = buildAdvertiseData(b);
            mAdvertiseCallback = new SampleAdvertiseCallback(b);

            if (mBluetoothLeAdvertiser != null) {

                mBluetoothLeAdvertiser.startAdvertising(settings, data,
                    mAdvertiseCallback);


            } else {
                stopped("블루투스 꺼짐");
            }
        }
    }

    /**
     * Move service to the foreground, to avoid execution limits on background processes.
     *
     * Callers should call stopForeground(true) when background work is complete.
     */
    private void goForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
            notificationIntent, 0);


        NotificationCompat.Builder n = mNotificationHelper
                .getNotification("페르마타가 실행중입니다.", "", pendingIntent);


        startForeground(FOREGROUND_NOTIFICATION_ID, n.build());
    }

    private void stopped(String s){

        stopAdvertising();
        stopScanning();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);


        NotificationCompat.Builder n = mNotificationHelper


                .getNotification("페르마타 중지됨-"+s, "터치하면 다시 시작합니다.", pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                ;



        mNotificationHelper.notify(FOREGROUND_NOTIFICATION_ID, n);


    }


/*    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("페르마타가 실행중입니다.")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }*/

    /**
     * Stops BLE Advertising.
     */
    private void stopAdvertising() {
        Log.d(TAG, "Service: Stopping Advertising");
        if (mBluetoothLeAdvertiser != null) {
         if(mAdvertiseCallback!=null)   mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mAdvertiseCallback = null;
        }
    }

    /**
     * Returns an AdvertiseData object which includes the Service UUID and Device Name.
     */
    public static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }

    public static UUID getUUIDFromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        Long high = byteBuffer.getLong();
        Long low = byteBuffer.getLong();

        return new UUID(high, low);
    }
    private AdvertiseData buildAdvertiseData( byte[] signal) {

        /**
         * Note: There is a strict limit of 31 Bytes on packets sent over BLE Advertisements.
         *  This includes everything put into AdvertiseData including UUIDs, device info, &
         *  arbitrary service or manufacturer data.
         *  Attempting to send packets over this limit will result in a failure with error code
         *  AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE. Catch this error in the
         *  onStartFailure() method of an AdvertiseCallback implementation.
         */

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(Service_UUID);
        //ParcelUuid.fromString(uuid.toString())

        //dataBuilder.addManufacturerData("");
        //ByteBuffer mManufacturerData = ByteBuffer.allocate(12);
        //  byte[] uuidb = new ObjectId().toByteArray();
      //  mManufacturerData.put(0, (byte)0xBE); // Beacon Identifier
      //  mManufacturerData.put(1, (byte)0xAC); // Beacon Identifier

        dataBuilder.addManufacturerData(1023, signal); // using google's company ID

        dataBuilder.setIncludeDeviceName(false);
        dataBuilder.setIncludeTxPowerLevel(false);
        //dataBuilder.setIncludeDeviceName()
        //dataBuilder.
        /* For example - this will cause advertising to fail (exceeds size limit) */
        //String failureData = "asdghkajsghalkxcjhfa;sghtalksjcfhalskfjhasldkjfhdskf";
        //dataBuilder.addServiceData(Constants.Service_UUID, failureData.getBytes());

        return dataBuilder.build();
    }

    /**
     * Returns an AdvertiseSettings object set to use low power (to help preserve battery life)
     * and disable the built-in timeout since this code uses its own timeout runnable.
     */
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        settingsBuilder.setConnectable(false);

        settingsBuilder.setTimeout(0);
        return settingsBuilder.build();
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Custom callback after Advertising succeeds or fails to start. Broadcasts the error code
     * in an Intent to be picked up by AdvertiserFragment and stops this Service.
     */
    private class SampleAdvertiseCallback extends AdvertiseCallback {
        byte[] uuid;

        public SampleAdvertiseCallback(byte[] uuid) {
            this.uuid=uuid;
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            Log.d(TAG, "Advertising failed"+ errorCode);
            sendFailureIntent(errorCode);
            stopSelf();

        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);


            class NewRunnable implements Runnable {

                @Override
                public void run() {

                    AppDatabase
                            .getInstance(context)
                            .getTempSignalDao()
                            .insert(new TempSignal( bytesToHex(uuid)  ));

                }
            }

            NewRunnable nr = new NewRunnable() ;
            Thread t = new Thread(nr) ;
            t.start() ;


            Log.d(TAG, "Advertising successfully started");
        }
    }

    /**
     * Builds and sends a broadcast intent indicating Advertising has failed. Includes the error
     * code as an extra. This is intended to be picked up by the {@code AdvertiserFragment}.
     */
    private void sendFailureIntent(int errorCode){
        Intent failureIntent = new Intent();
        failureIntent.setAction(ADVERTISING_FAILED);
        failureIntent.putExtra(ADVERTISING_FAILED_EXTRA_CODE, errorCode);
        sendBroadcast(failureIntent);
    }


    public void setBluetoothAdapter(BluetoothAdapter btAdapter) {
        this.mBluetoothAdapter = btAdapter;

    }






    /**
     * Start scanning for BLE Advertisements (& set it up to stop after a set period of time).
     */
    public void startScanning() {
        if (  mScanCallback == null) {
            Log.d(TAG, "Starting Scanning");

            // Will stop the scanning after a set time.
         /*   mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScanning();
                }
            }, SCAN_PERIOD);
*/
            // Kick off a new scan.
            mScanCallback = new SampleScanCallback();

            if(mBluetoothLeScanner==null) {

                stopped("블루투스 꺼짐");
                return; }
            mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);


        } else {
           // Toast.makeText(getApplicationContext(), R.string.already_scanning, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Stop scanning for BLE Advertisements.
     */
    public void stopScanning() {
        Log.d(TAG, "Stopping Scanning");

        // Stop the scan, wipe the callback.
        mBluetoothLeScanner.stopScan(mScanCallback);
        mScanCallback = null;

        // Even if no new results, update 'last seen' times.

    }

    /**
     * Return a List of {@link ScanFilter} objects to filter by Service UUID.
     */


    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        // Comment out the below line to see all BLE devices around you
         builder.setServiceUuid(Constants.Service_UUID);

        // builder.setManufacturerData(1023, maskbit );
         //builder.setServiceData(Constants.Service_UUID, mask   , mak);
       // builder.setServiceData(new byte[3]);

        //builder.setDeviceName("lskdjfalsfd");
        //builder.setDeviceName("zhfhskwnrdlrl");
        //builder.setManufacturerData()
        scanFilters.add(builder.build());



        return scanFilters;
    }

    /**
     * Return a {@link ScanSettings} object set to use low power (to preserve battery life).
     */
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        return builder.build();
    }

    /**
     * Custom ScanCallback object - adds to adapter on success, displays error on failure.
     */


    private HashMap<String,Integer> cachedUUID= new HashMap<>();

    private  void  checkScan(ScanResult result ){


       final int now=(int) (System.currentTimeMillis()/1000);

        //;

        byte[] signal = result.getScanRecord().getManufacturerSpecificData(1023);


            String uuid=bytesToHex(signal);
            Integer h = cachedUUID.get(uuid);

        if(h==null) {

            cachedUUID.put(uuid,now);
        } else

            if( now> Math.abs(h)+ 60*60 ) {
                //cachedUUID.remove(uuid);
                cachedUUID.remove(uuid);
            } else  if(h<0){
                int f=-h;

                new Thread(new Runnable() {
                    @Override
                    public void run() {


                        AppDatabase
                                .getInstance(context)
                                .getSignalLogDao()

                                .updateTimeSpan(uuid,f,  now-f,result.getRssi());


                    }
                }) .start() ;


            }
                else if( now-h> CHECH_MINSEC)
            {


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase
                                .getInstance(context)
                                .getSignalLogDao()
                                .insert(new SignalLog(uuid,result.getRssi(),true, CHECH_MINSEC , now));




                        cachedUUID.put(uuid,-now);

                    }
                }) .start() ;


                if(IS_BETA ||BuildConfig.DEBUG ) {



                    Notification n = new Notification.Builder(this)
                            .setContentTitle("scanced")
                            .setContentText(uuid.toString())
                            .setSmallIcon(R.drawable.ic_stat_name)
                            .build();

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                    // notificationId is a unique int for each notification that you must define
                    notificationManager.notify(now % 2323, n);


                    NotificationCompat.Builder mBuilder = mNotificationHelper
                            .getNotification("알파버젼테스트용 알림-접근발견",
                                    uuid.toString(), null)
                            //  .setSmallIcon(android.R.drawable.stat_notify_error)
                            ;
                    // attached Retry action in notification

                    // Notify notification
                    mNotificationHelper.notify(5, mBuilder);


                    new Thread(new Runnable() {
                        @Override
                        public void run() {


                            AppDatabase instance = AppDatabase
                                    .getInstance(getApplicationContext());




                            int now= (int) (System.currentTimeMillis()/1000);

                            String[] records =instance.getSignalLogDao().getUUIDRecord(now-CHECH_MINSEC);

                            if(records!=null& records.length>0){

                                getApplicationContext().sendBroadcast(new Intent(  getApplicationContext() , ScanReceiver.class).putExtra("uuid",records)
                                        .putExtra("time",now));


                            } else {



                            }



                        }
                    }) .start() ;
                }

            }









    }

    private class SampleScanCallback extends ScanCallback {

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d("onScanResult","");
            for (ScanResult result : results) {
              //  mAdapter.add(result);

                checkScan(result);
                //Toast.makeText(getApplicationContext(), result.toString() , Toast.LENGTH_LONG).show();
            }
            //mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            checkScan(result);
            //        Toast.makeText(getApplicationContext(), result.toString() , Toast.LENGTH_LONG).show();
         //   mAdapter.add(result);
          //  mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(getApplicationContext(), "Scan failed with error: " + errorCode, Toast.LENGTH_LONG)
                    .show();
        }
    }
}
