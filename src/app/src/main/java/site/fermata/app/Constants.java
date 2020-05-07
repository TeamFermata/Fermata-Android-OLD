/*
    Copyright © 2007 Free Software Foundation, Inc. <https://fsf.org/>
    Everyone is permitted to copy and distribute verbatim copies of this license document, but changing it is not allowed.
    Github Repository : https://github.com/TracetogetherKorea/Fermata-Android

    - BLE 통신 설정 -
 */

package site.fermata.app;

import android.os.ParcelUuid;

/**
 * Constants for use in the Bluetooth Advertisements sample
 */
public class Constants {


    public static final String PREF_ID = "ID_beta";
    public static final boolean IS_BETA=true;

    public static final String ACTION_RERUN = "site.fermata.app.RERUN";

    public static  final int CHECK_SPAN_DAY= 21;

    public static final int CHECH_MINSEC = 20;

    public static final ParcelUuid Service_UUID = ParcelUuid
            .fromString("40e30d2c-17a2-4bbe-83a9-5b2cbf10ee1b");
   // public static final byte[] maskbit = {32,23,99,47,12,-32,45,23,11};

    public static final int REQUEST_ENABLE_BT = 1;



    //public  static  final  String SERVER_URL="https://fermataserver.herokuapp.com";

  //  public  static  final  String SERVER_URL="https://nopd4pibsh.apigw.ntruss.com/API/prod/rivykMwR3e/json";
  public  static  final  String SERVER_URL="https://nopd4pibsh.apigw.ntruss.com/API/prod/rivykMwR3e/json";



}
