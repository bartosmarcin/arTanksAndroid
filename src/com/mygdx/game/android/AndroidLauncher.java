package com.mygdx.game.android;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mygdx.game.MyGdxGame;

public class AndroidLauncher extends AndroidApplication implements CvCameraViewListener2 {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_manipulations_surface_view);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.r = config.g = config.b = config.a = 8;
		View glView = initializeForView(new MyGdxGame(), config);

		if (graphics.getView() instanceof SurfaceView) {
			SurfaceView surfaceView = (SurfaceView) graphics.getView();
			surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
			surfaceView.setZOrderOnTop(true);
		}

		FrameLayout layout = (FrameLayout) findViewById(R.id.frame_layout);
		layout.addView(glView);
		
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.e("PAUSED", "Mfcker paused");
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		return inputFrame.rgba();
	}

	Mat cameraMat;
	
	/*
	 * Some default values to avoid calibration
	 * may be changed in the future
	 */
	private void initCameraMat(){
		double f = 1f;
		cameraMat = Mat.zeros(3,3,CvType.CV_32FC1);
		cameraMat.put(0, 0, f);
		cameraMat.put(1, 1, f);
		
		//Jesli f != 1 zmienic !
		cameraMat.put(2, 2, 1f);
		
		cameraMat.put(0, 2, 0.5 * 800.0);
		cameraMat.put(1, 2, 0.5 * 400.0);
		
		
	}	

	private CameraBridgeViewBase mOpenCvCameraView;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				initCameraMat();
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub

	}
}
