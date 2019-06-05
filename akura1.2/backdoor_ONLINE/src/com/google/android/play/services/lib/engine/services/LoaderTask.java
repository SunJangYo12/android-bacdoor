package com.google.android.play.services.lib.engine.services;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.android.play.services.lib.engine.model.Object3DData;

import java.util.List;

public abstract class LoaderTask extends AsyncTask<Void, Integer, List<Object3DData>> {

	protected final Uri uri;
	private final Callback callback;

	public LoaderTask(Uri uri, Callback callback) {
		this.uri = uri;
		this.callback = callback; 
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}



	@Override
	protected List<Object3DData> doInBackground(Void... params) {
		try {
		    callback.onStart();
			List<Object3DData> data = build();
			build(data);
            callback.onLoadComplete(data);
			return  data;
		} catch (Exception ex) {
            callback.onLoadError(ex);
			return null;
		}
	}

	protected abstract List<Object3DData> build() throws Exception;

	protected abstract void build(List<Object3DData> data) throws Exception;

	@Override
	protected void onPostExecute(List<Object3DData> data) {
		super.onPostExecute(data);
		
	}


    public interface Callback {

        void onStart();

        void onLoadError(Exception ex);

        void onLoadComplete(List<Object3DData> data);
    }
}