package com.google.play.plugin.compat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.util.Log;
import android.content.res.AssetManager;
import android.os.*;
import android.widget.*;
import android.content.SharedPreferences;
import java.io.*;
import java.util.zip.*;
import android.util.*;

/**
 * @author Naik
 */
public class Installer extends AsyncTask<String, String, Boolean> implements DialogInterface.OnClickListener {

    private static final String tag = Installer.class.getName();
    private static final long STOCK_LOCAL_MEMORY = 1024000;//1000 Kb про запас
    private Context context;
    private Handler ui;
    //private static final int MAX_ERR = 100;
    private String DOC_FOLDER;
    private String err = "";
    private long contentLength;
    private ProgressDialog dialog;
    //private Loader loader;
    private boolean setRights;
    private long currProgress;
    private SharedPreferences settings;
    private SharedPreferences.Editor seteditor;

    public Installer() {
        
    }

    public Installer(Context context, boolean setRights) {
        this.context = context;
        this.setRights = setRights;
        settings = context.getSharedPreferences("Settings", 0);
        seteditor = settings.edit();
    }

    public void setErr(String err) {
        this.err = err;
    }

    public void setErr(int resid) {
        this.err = context.getResources().getString(resid);
    }

    public void setErr(int resid, String strAdd) {
        this.err = context.getResources().getString(resid) + " " + strAdd;
    }

    public String getErr() {
        return err;
    }

    public void update(int add) {
        //Log.i("Installer", "update: " + add + "bytes (" + (add / 1024) + "Kbytes)");
        publishProgress(String.valueOf(add));
    }

    public static long calcUnzipped(InputStream is) {
        ZipInputStream zip = new ZipInputStream(is);
        ZipEntry ze;
        long realSize = 0;
        try {
            while ((ze = zip.getNextEntry()) != null) {
                realSize += ze.getSize();
                zip.closeEntry();
            }
        } catch (IOException e) {
        }
        //L.write(tag, "calcUnzipped return = " + (int)realSize);
        return realSize;
    }

    public void assetToSdcard(Context context, String nasset, String path) {
        try {
            InputStream in = context.getAssets().open(nasset);
            String fileName = path+"/"+nasset;

            OutputStream out = new FileOutputStream(fileName);
            byte[] buffer = new byte[1024];
            int read;

            while ((read=in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            out.close();
            in.close();

        }catch(Exception e){
        }
    }


    //bug
    public static void zipFolder(String inputFolderPath, String outZipPath) {
        try {
            FileOutputStream fos = new FileOutputStream(outZipPath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            File srcFile = new File(inputFolderPath);
            File[] files = srcFile.listFiles();

            for (int i=0; i<files.length; i++) {
                byte[] buffer = new byte[1024];
                FileInputStream fis = new FileInputStream(files[i]);
                zos.putNextEntry(new ZipEntry(files[i].getName()));

                int length;
                while ((length=fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
        
        } catch(Exception e) {}
    }

    //ex: zipPath("/sdcard/oke", "/sdcard/oke.zip");
    public static boolean zipPath(String sourcePath, String toLocation) {
        final int BUFFER = 2048;
        File sourceFile = new File(sourcePath);
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());

            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                entry.setTime(sourceFile.lastModified());
                out.putNextEntry(entry);

                int count;
                while((count=origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
            
        } catch(Exception e) {
            Log.i("ttt", ""+e);
            return false;
        }
        return true;
    }
    private static void zipSubFolder(ZipOutputStream out, File folder, int basePathLength) throws IOException {
        final int BUFFER = 2048;
        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);

            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath.substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(file.lastModified());
                out.putNextEntry(entry);

                int count;
                while((count=origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }
    //ex: getLastPathComponent("/sdcard/Android/data/oke");
    //res: "oke";
    public static String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0) 
            return "";

        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }

    public void compressFiles(String src, String dstName) {
        CompressFiles mCompressFiles = new CompressFiles(src, dstName);
        mCompressFiles.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class CompressFiles extends AsyncTask<Void, Integer, Boolean> {
        private String src;
        private String dstName;

        public CompressFiles(String src, String dstName) {
            this.src = src;
            this.dstName = dstName;
        }
        @Override
        protected void onPreExecute() {
        }

        protected Boolean doInBackground(Void... urls) {
            return zipPath(src, dstName);
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Boolean flag) {
            Log.i("lll", "sukses");
        }
    }

    /** USAGE:
      * String[] s = new String[2];
      * s[0] = inputPath + "/image.jpg";
      * s[1] = inputPath + "/text.txt";
      *
      * zip(s, inputPath+inputFile);
      */

    public void zip(String[] _files, String zipFileName) {
        int BUFFER = 2048;
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            for (int i=0; i<_files.length; i++) {
                FileInputStream fi = new FileInputStream(_files[i]);

                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
            
                int count;
                while((count=origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    public static String readFile(String path) {
        StringBuilder sb = new StringBuilder();
        try {
            FileReader fr = new FileReader(path);
            char buff[] = new char[1024];
            int c;
            while ((c = fr.read(buff)) != -1) {
                sb.append(buff, 0, c);
            }
        } catch (FileNotFoundException e) {
            return "File not found (TODO)";
        } catch (IOException ioe) {
            return "IOException (TODO)";
        }
        return sb.toString();
    }

    public static void saveCode(String code, String charset, String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), charset);
        osw.append(code).flush();
        osw.close();
    }

    protected Boolean doInBackground(String[] p1) {
        final String nameInAssets = p1[0];
        final String urlToInstall = p1[1];
        DOC_FOLDER = p1[2];

        File folderInstall = new File(urlToInstall);
        File fileDocFolder = new File(DOC_FOLDER);

        if (!folderInstall.exists()) {
            folderInstall.mkdirs();
        }
        if (!fileDocFolder.exists()) {
            fileDocFolder.mkdirs();
        }
       
        return false;
    }

    @Override
    public void onProgressUpdate(String... s) {
        currProgress += Integer.parseInt(s[0]);
        dialog.setProgress((int) (currProgress / 1024L));
    }

    @Override
    public void onPreExecute() {
        //Log.i("Installer", "onPreExecute");
        ui = new Handler();

        dialog = new ProgressDialog(context);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.setTitle("downloading_wait");
        dialog.setMessage("meedasdf");
        dialog.setIndeterminate(true);
        //dialog.show();
    }

    @Override
    public void onPostExecute(Boolean result) {
        //Log.i("Installer", "onPostExecute with: " + result);
        if (result) {
            Toast t = Toast.makeText(context, "install_complete", Toast.LENGTH_LONG);
            //t.show();

            seteditor.putBoolean("server install", true);    
            seteditor.commit();
            dialog.dismiss();
            if (!new File(DOC_FOLDER + "/index.php").exists()) {
                try {
                    saveCode("<?php phpinfo(); ?>", "utf-8", DOC_FOLDER + "/index.php");
                } catch (IOException e) {
                }
            }
            //h.sendEmptyMessage(MainActivity.INSTALL_OK);
        } else {
            Toast t = Toast.makeText(context, getErr().replace("annimon", "pentagon"), Toast.LENGTH_LONG);
            t.show();
            dialog.dismiss();
            setErr("");
            //h.sendEmptyMessage(MainActivity.INSTALL_ERR);
        }
        //L.write("Installer", "onPostExecuted");
    }

    public void onClick(DialogInterface p1, int p2) {
        //L.write("Installer", "calcel task in onClick()");
        setErr("install_calcel");
        this.cancel(false);
        onPostExecute(false);

    }
}
