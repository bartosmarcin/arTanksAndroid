package com.mygdx.game.android;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import ARCameraControler.FrameProcessor;
import android.app.ProgressDialog;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mygdx.game.ILoading;
import com.mygdx.game.MyGdxGame;

public class AndroidLauncher extends AndroidApplication implements CvCameraViewListener2, ILoading {

	private ProgressDialog pDialog;
	private CameraBridgeViewBase mOpenCvCameraView;
	private BaseLoaderCallback mLoaderCallback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_manipulations_surface_view);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		pDialog = ProgressDialog.show(this, "Proszę czekać", "Trwa ładowanie modelu 3D");
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.r = config.g = config.b = config.a = 8;
		try {

			View glView = initializeForView(new MyGdxGame(480, 800, this), config);

			 if (graphics.getView() instanceof SurfaceView) {
			 SurfaceView surfaceView = (SurfaceView) graphics.getView();
			 surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
			 surfaceView.setZOrderOnTop(true);
			 }

			FrameLayout layout = (FrameLayout) findViewById(R.id.frame_layout);
			layout.addView(glView);
			mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
			mOpenCvCameraView.setCvCameraViewListener(this);
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private FrameProcessor frameProcessor;
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Mat frame = inputFrame.rgba();
		return frameProcessor.onFrameCaptured(frame);
		
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoadingComplete() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				pDialog.setMessage("Trwa ładowanie opencv");
				mLoaderCallback = new BaseLoaderCallback(AndroidLauncher.this) {
					@Override
					public void onManagerConnected(int status) {
						switch (status) {
						case LoaderCallbackInterface.SUCCESS: {		
							mOpenCvCameraView.enableFpsMeter();
							frameProcessor = new FrameProcessor();
							mOpenCvCameraView.enableView();
							pDialog.dismiss();
						}
							break;
						default: {
							super.onManagerConnected(status);
						}
							break;
						}
					}
				};
				OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, AndroidLauncher.this, mLoaderCallback);

			}
		});

	}
}
