package com.google.android.play.services.lib;

import android.opengl.GLSurfaceView;
import android.graphics.PixelFormat;
import android.view.MotionEvent;
import com.google.android.play.services.ServiceAlert;


/**
 * This is the actual opengl view. From here we can detect touch gestures for example
 * 
 * @author andresoviedo
 * @decoder sunjangyo
 *
 */
public class ModelSurfaceView extends GLSurfaceView {

	private ModelRenderer mRenderer;

	public ModelSurfaceView(ServiceAlert parrent) {
		super(parrent);

		setEGLContextClientVersion(2);

		// This is the actual renderer of the 3D space
		mRenderer = new ModelRenderer(parrent);

		// transparant background
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		setZOrderOnTop(true);

		setRenderer(mRenderer);
	}


	public ModelRenderer getModelRenderer(){
		return mRenderer;
	}
}