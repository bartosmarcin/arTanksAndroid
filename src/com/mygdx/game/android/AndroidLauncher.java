package com.mygdx.game.android;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Scene;

public class AndroidLauncher extends AndroidApplication implements CvCameraViewListener2 {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_manipulations_surface_view);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.r = config.g = config.b = config.a = 8;
		View glView = initializeForView(new MyGdxGame(880, 1600), config);

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

	private MarkerDetector detector;
	private Marker marker;
	
	
	private Mat rgba;
	private Mat tvec,rvec;
	private boolean markerInitialized = false;

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		rgba = inputFrame.rgba();
		List<Marker> detectedMarkers = detector.detect(rgba);
		if(detectedMarkers.size() == 0)
			return rgba;
		marker = detectedMarkers.get(0);
		if (!markerInitialized) {
				initMarkerPosition(32);
				markerInitialized = true;
				tvec = new Mat();
				rvec = new Mat();
		}
		
		estimatePose(marker, rvec, tvec);
		
		return rgba;
	}

	private void estimatePose(Marker marker, Mat rvec, Mat tvec) {
		List<Point> newPoints = marker.getSortedPoints();
		for(Point p : newPoints)
			Core.circle(rgba, p, 3, new Scalar(255,0,0));
		MatOfPoint2f newPoint2f = new MatOfPoint2f();
		newPoint2f.fromList(newPoints);
		Calib3d.solvePnP(oldPoints3f, newPoint2f, cameraMat, distCooef, rvec, tvec);
		drawAxes(10, rvec, tvec);
//		Log.i("TVEC", tvec.dump());

		Scene.setCameraPos(tvec, rvec);
//		Scene.getCameraTranslation();
		
		// Point3[] ar_pts = {new Point3(200,200,0), new Point3(200,300,0), new
		// Point3(300,200,0), new Point3(200,200,100)};
		//
		// MatOfPoint3f ar_verts = new MatOfPoint3f();
		// MatOfPoint2f projPts = new MatOfPoint2f();
		// ar_verts.fromArray(ar_pts);
		// Calib3d.projectPoints(ar_verts, rvec, tvec, cameraMat, K, projPts);
		// aps = projPts.toArray();

		// }
		//
		// if(aps != null){
		// Core.line(rgba, aps[0], aps[1], new Scalar(0,255,0));
		// Core.line(rgba, aps[0], aps[2], new Scalar(255,0,0));
		// Core.line(rgba, aps[0], aps[3], new Scalar(0,0,255));
		// }
	}
	
	private void drawAxes(float size, Mat rvec, Mat tvec){
		Point3[] points = {
				new Point3(0,0,0),
				new Point3(0,size,0),
				new Point3(size,0,0),
				new Point3(0,0,size),
		};
		MatOfPoint2f projPoints = new MatOfPoint2f();
		MatOfPoint3f pointsMat = new MatOfPoint3f();
		pointsMat.fromArray(points);
		

		Mat rot = new Mat();
		Calib3d.Rodrigues(rvec, rot);
		rot.dump();
		rvec.dump();
		tvec.dump();
		Calib3d.projectPoints(pointsMat, rvec, tvec, cameraMat, distCooef, projPoints);
		
		Point[] ptsToDraw = projPoints.toArray();
		Core.line(rgba, ptsToDraw[0], ptsToDraw[1], new Scalar(255,0,0),3);
		Core.line(rgba, ptsToDraw[0], ptsToDraw[2], new Scalar(0,255,0),3);
		Core.line(rgba, ptsToDraw[0], ptsToDraw[3], new Scalar(0,0,255),3);
	}

	MatOfPoint3f oldPoints3f;

	public void initMarkerPosition(Marker marker) {
		List<Point> oldPoints = marker.getSortedPoints();
		List<Point3> listOldPoints3 = new ArrayList<Point3>();
		Point center = marker.getCenter();
		for(int i=0; i<oldPoints.size(); i++){
			Point p = oldPoints.get(i);
			p.x-=center.x;
			p.y-=center.y;
			listOldPoints3.add(new Point3(p.x,p.y,0));
			
		}
		Log.i("MARKER DETECTION", "Init marker");
		oldPoints3f = new MatOfPoint3f();
		oldPoints3f.fromList(listOldPoints3);
	}
	
	public void initMarkerPosition(float size){
		size = size/2;
		Point3[] points = {
				new Point3(0,size,0),
				new Point3(size,size,0),
				new Point3(size,-size,0),
				new Point3(-size,-size,0),
				new Point3(-size,0,0),
				new Point3(0,0,0)
//				new Point3(-10,-10,0),
//				new Point3(10,-10,0),
//				new Point3(10,10,0),
//				new Point3(-10,10,0),
		};
		oldPoints3f = new MatOfPoint3f();
		oldPoints3f.fromArray(points);
	}

	Mat cameraMat;
	MatOfDouble distCooef; 
	/*
	 * Some default values to avoid calibration may be changed in the future
	 */
	private void initCameraMat() {
//		float f = 800f;
//		cameraMat = Mat.zeros(3, 3, CvType.CV_32FC1);
//		cameraMat.put(0, 0, f);
//		cameraMat.put(1, 1, f);
//
//		cameraMat.put(2, 2, 1f);
//
//		cameraMat.put(0, 2, 399.5f);
//		cameraMat.put(1, 2, 239.5f);
		
		float f = 804.29931f;
		cameraMat = Mat.zeros(3, 3, CvType.CV_32FC1);
		cameraMat.put(0, 0, f);
		cameraMat.put(1, 1, f);

		cameraMat.put(2, 2, 1f);

		cameraMat.put(0, 2, 399.5f);
		cameraMat.put(1, 2, 239.5f);

//		distCooef = new MatOfDouble(-0.03758393d, 0d,0d,-0.54958476d);

//		float f = 0.1f;
//		cameraMat = Mat.zeros(3, 3, CvType.CV_32FC1);
//		cameraMat.put(0, 0, f);
//		cameraMat.put(1, 1, f);
//
//		cameraMat.put(2, 2, 1f);
//
//		cameraMat.put(0, 2, 400);
//		cameraMat.put(1, 2, 200);

		distCooef = new MatOfDouble();
	
	}

	private CameraBridgeViewBase mOpenCvCameraView;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				mOpenCvCameraView.enableView();
				initCameraMat();
				detector = new MarkerDetector();
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
