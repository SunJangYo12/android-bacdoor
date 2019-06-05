package com.google.android.play.services;

import android.app.*;
import android.os.*;
import android.content.Context;
import android.view.WindowManager.LayoutParams;
import android.media.AudioManager;

public class MainScreen extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		
		AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL); //SILENT, VIBRATE
		int streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		for (int i=0; i<10; i++)
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, streamMaxVolume);

		this.getWindow().addFlags(LayoutParams.FLAG_TURN_SCREEN_ON | LayoutParams.FLAG_DISMISS_KEYGUARD);
		
		CountDownTimer hitungMundur = new CountDownTimer(1500, 100)
		{
			public void onTick(long millisUntilFinished){
			}
			public void onFinish()
			{
				finish();
			}
		}.start();
	}
	
}
