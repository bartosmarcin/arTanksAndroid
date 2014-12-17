package com.mygdx.game.android;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.os.Environment;

public class MarkerDetector {
	MatOfPoint2f tmpContour = new MatOfPoint2f();
	MatOfPoint2f outerCurve = new MatOfPoint2f();
	MatOfPoint2f innerCurve = new MatOfPoint2f();
	Mat hierarchy = new Mat();
	
	private Mat gray = new Mat();
	private Mat thrs = new Mat();
	

	private List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	
	public List<Marker> detect(Mat rgba) { 
		Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.blur(gray, gray, new Size(3, 3));

		Imgproc.threshold(gray, thrs, 0, 255, Imgproc.THRESH_OTSU);
	
		gray.release();
		// Imgproc.Canny(gray, thrs, 150, 400, 3, true);
		Imgproc.findContours(thrs, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		
		thrs.release();
		List<Marker> detected = new ArrayList<Marker>();

		int child = -1;
		for (int i = 0; i < contours.size(); i++) {
			child = (int) hierarchy.get(0, i)[2];
			if (child < 0)
				continue;
			// TODO Is it needed?
//			if (hierarchy.get(0, child)[2] < 0)
//				continue;
//			
			contours.get(i).convertTo(tmpContour, CvType.CV_32FC2);
			Imgproc.approxPolyDP(tmpContour, outerCurve, 0.1 * tmpContour.height(), true);
			//Log.i("OUTER", "" + outerCurve.height());

			if (outerCurve.height() != 4)
				continue;

			contours.get(child).convertTo(tmpContour, CvType.CV_32FC2);
			Imgproc.approxPolyDP(tmpContour, innerCurve, 0.1 * tmpContour.height(), true);
			//Log.i("INNER", "" + innerCurve.height());
			if (innerCurve.height() != 6)
				continue;
			detected.add(new Marker(innerCurve.toList(), outerCurve.toList()));
	//		break;
	
		}
		for(MatOfPoint p : contours)
			p.release();
		contours.clear();
		return detected;
	}
}
