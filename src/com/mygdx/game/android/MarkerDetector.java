package com.mygdx.game.android;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import ARCameraControler.Marker;
import android.os.Environment;

public class MarkerDetector {
	private MatOfPoint2f tmpContour;
	private MatOfPoint2f outerCurve;
	private MatOfPoint2f innerCurve;
	private Mat hierarchy;
	private double scaleDown;
	private Size scaleDownSize;
	private Mat thrs;

	private List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

	public MarkerDetector() {
		this(1);
	}

	public MarkerDetector(double scaleDown) {
		tmpContour = new MatOfPoint2f();
		outerCurve = new MatOfPoint2f();
		innerCurve = new MatOfPoint2f();
		thrs = new Mat();
		this.hierarchy = new Mat();
		this.scaleDown = scaleDown;
	}

	public Marker detect(Mat rgba) {
		Marker marker = null;
		// if(scaleDown != 1)
		// Imgproc.resize(thrs, thrs, );

		writeFile("rgba.png", rgba);

		Imgproc.cvtColor(rgba, thrs, Imgproc.COLOR_RGB2GRAY);

		writeFile("gray.png", thrs);
		Imgproc.blur(thrs, thrs, new Size(3, 3));

		writeFile("blur.png", thrs);
		// Imgproc.medianBlur(thrs, thrs, 17);
		Imgproc.threshold(thrs, thrs, 50, 255, Imgproc.THRESH_OTSU);

		writeFile("thrs.png", thrs);

		// Imgproc.adaptiveThreshold(thrs, thrs,255,
		// Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 2);
		// Imgproc.Canny(thrs, thrs, 150, 400, 3, true);
		Imgproc.findContours(thrs, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

		writeFile("cont.png", thrs);
		// thrs.release();

		int child = -1;
		for (int i = 0; i < contours.size(); i++) {
			child = (int) hierarchy.get(0, i)[2];
			if (child < 0)
				continue;
			// TODO Is it needed?
			// if (hierarchy.get(0, child)[2] < 0)
			// continue;
			//
			contours.get(i).convertTo(tmpContour, CvType.CV_32FC2);
			Imgproc.approxPolyDP(tmpContour, outerCurve, 0.01 * tmpContour.height(), true);

			if (outerCurve.height() != 4)
				continue;

			contours.get(child).convertTo(tmpContour, CvType.CV_32FC2);
			Imgproc.approxPolyDP(tmpContour, innerCurve, 0.01 * tmpContour.height(), true);
			if (innerCurve.height() != 6)
				continue;
			marker = new Marker(innerCurve.toList(), outerCurve.toList());
			for (Point p : marker.getSortedPoints()) {
				Core.circle(rgba, p, 3, new Scalar(255, 255, 0));
			}

			writeFile("poly.png", rgba);
			break;
		}
		// for(MatOfPoint p : contours)
		// p.release();
		contours.clear();
		return marker;
	}

	private void writeFile(String name, Mat mat) {
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File rgbaFile = new File(path, name);
		Highgui.imwrite(rgbaFile.toString(), mat);
	}
}
