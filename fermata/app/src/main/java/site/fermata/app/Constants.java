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

import android.os.ParcelUuid;

/**
 * Constants for use in the Bluetooth Advertisements sample
 */
public class Constants {




    public static final String ACTION_RERUN = "site.fermata.app.RERUN";

    public static  final int CHECK_SPAN_DAY= 21;

    public static final ParcelUuid Service_UUID = ParcelUuid
            .fromString("40e30d2c-17a2-4bbe-83a9-5b2cbf10ee1b");

    public static final int REQUEST_ENABLE_BT = 1;


    public  static  final  String SERVER_URL="https://fermataserver.herokuapp.com";

}
