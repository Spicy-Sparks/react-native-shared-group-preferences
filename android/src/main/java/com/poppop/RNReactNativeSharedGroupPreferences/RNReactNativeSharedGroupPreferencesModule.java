
package com.poppop.RNReactNativeSharedGroupPreferences;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import java.io.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;



public class RNReactNativeSharedGroupPreferencesModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public static LocalBroadcastManager localBroadcastManager;

  public RNReactNativeSharedGroupPreferencesModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    localBroadcastManager = LocalBroadcastManager.getInstance(reactContext);
  }

  private SharedPreferences getSharedPreferences(String appGroup) {
    return reactContext.getApplicationContext().getSharedPreferences(appGroup, Context.MODE_PRIVATE);
  }

  @Override
  public String getName() {
    return "RNReactNativeSharedGroupPreferences";
  }

  @ReactMethod
  public void isAppInstalledAndroid(String packageName, final Callback callback) {
    PackageManager pm = reactContext.getPackageManager();
    try {
      pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
      callback.invoke(true);
    } catch (Exception e) {
      callback.invoke(false);
    }
  }

  @ReactMethod
  public void setItem(String key, String value, String appGroup, ReadableMap options, final Callback callback) {
    boolean useAndroidSharedPreferences = false;
    if (options.hasKey("useAndroidSharedPreferences")) {
      useAndroidSharedPreferences = options.getBoolean("useAndroidSharedPreferences");
    }

    if (useAndroidSharedPreferences) {
      SharedPreferences preferences = getSharedPreferences(appGroup);
      SharedPreferences.Editor editor = preferences.edit();
      editor.putString(key, value);
      editor.apply();
      callback.invoke(null, "");
    } else {
      int apiVersion = android.os.Build.VERSION.SDK_INT;
      /*
      if (apiVersion >= 30) {

        // kjellhere /////////
        String URL = "content://" + appGroup + "/data";
        Uri CONTENT_URI = Uri.parse(URL);

        ContentValues values = new ContentValues();
        values.put(key, value);
        Uri uri = getContentResolver().insert(CONTENT_URI, values);
////////////////////////
      } else {
        */
        File extStore = Environment.getExternalStorageDirectory();
        String fileName = "data.json";

        try {
          File dir = new File(extStore.getAbsolutePath() + "/" + appGroup + "/");
          dir.mkdir();
          File myFile = new File(dir, fileName);
          myFile.createNewFile();
          FileOutputStream fOut = new FileOutputStream(myFile);
          OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
          myOutWriter.append(value);
          myOutWriter.close();
          fOut.close();
          callback.invoke(null, "");
        } catch (Exception e) {
          e.printStackTrace();
          callback.invoke(0, null);
        }
        //Toast.makeText(this.reactContext, "HELLO! v" + apiVersion, Toast.LENGTH_LONG).show();
      //}
    }
  }

  @ReactMethod
  public void getItem(String key, String appGroup, ReadableMap options, final Callback callback) {
    boolean useAndroidSharedPreferences = false;
    if (options.hasKey("useAndroidSharedPreferences")) {
      useAndroidSharedPreferences = options.getBoolean("useAndroidSharedPreferences");
    }

    if (useAndroidSharedPreferences) {
      SharedPreferences preferences = getSharedPreferences(appGroup);
      String value = preferences.getString(key, null);
      callback.invoke(null, value);
    } else {
      int apiVersion = android.os.Build.VERSION.SDK_INT;
      /*
      if (apiVersion >= 30) {
        // kjellhere/////////
        String URL = "content://" + appGroup + "/data";
        Uri CONTENT_URI = Uri.parse(URL);
        Cursor c = getContentResolver().query(CONTENT_URI, null, null, null, null);

        String jsonString = "";
        int index = c.getColumnIndex(key);
        while (c.moveToNext()) {
          jsonString = jsonString + c.getString(index);
        }

        Toast.makeText(this.reactContext, "HELLO!", Toast.LENGTH_LONG).show();
//////////////
      } else {
        */
        File extStore = Environment.getExternalStorageDirectory();
        String fileName = "data.json";
        String path = extStore.getAbsolutePath() + "/" + appGroup + "/" + fileName;

        String s = "";
        String fileContent = "";
        try {

           File myFile = new File(path);
           FileInputStream fIn = new FileInputStream(myFile);
           BufferedReader myReader = new BufferedReader(
                   new InputStreamReader(fIn));

           while ((s = myReader.readLine()) != null) {
               fileContent += s + "";
           }
           myReader.close();
           callback.invoke(null, fileContent);
        } catch (IOException e) {
           callback.invoke(0, null);
        }
      //}
    }
  }

  @ReactMethod
  public void registerEvents() {
    BroadcastReceiver receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        // Handle the broadcast intent here
        String data = intent.getStringExtra("data");
        getReactApplicationContext()
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
          .emit("onReceive", data);
      }
    };

    IntentFilter filter = new IntentFilter("com.poppop.RNReactNativeSharedGroupPreferences.event");
    localBroadcastManager.registerReceiver(receiver, filter);
  }
}

/*
class RNReactNativeSharedGroupPreferencesProvider extends ContentProvider {

}
*/
