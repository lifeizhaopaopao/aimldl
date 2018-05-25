package com.acgist.face;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.HOGDescriptor;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class CaptureBasic extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final String DDL_PATH = "/opencv/opencv_java341.dll";
	private static final String DDL2_PATH = "/opencv/opencv_ffmpeg341_64.dll";
	private static final String XML_PATH = "/opencv/haarcascade_frontalface_alt.xml";

	private BufferedImage image;

	private BufferedImage mat2BI(Mat mat) {
		int dataSize = mat.cols() * mat.rows() * (int) mat.elemSize();
		byte[] data = new byte[dataSize];
		mat.get(0, 0, data);
		int type = mat.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;
		if (type == BufferedImage.TYPE_3BYTE_BGR) {
			for (int index = 0; index < dataSize; index += 3) {
				byte blue = data[index + 0];
				data[index + 0] = data[index + 2];
				data[index + 2] = blue;
			}
		}
		BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
		image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
		return image;
	}

	public void paintComponent(Graphics graphics) {
		if (image != null) {
			graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), this);
		}
	}

	public static final String localPath(String name) {
		return Face.class.getResource(name).getPath().substring(1);
	}

	public static void main(String[] args) {
		try {
			System.load(localPath(DDL_PATH));
			System.load(localPath(DDL2_PATH));
			Mat mat = new Mat();
			VideoCapture capture = new VideoCapture(localPath("/video/mayun.mp4"));
//			VideoCapture capture = new VideoCapture(0); // 摄像头
			int height = (int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
			int width = (int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
			if (height == 0 || width == 0) {
				throw new Exception("没有获取到摄像头！");
			}
			JFrame frame = new JFrame("camera");
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			CaptureBasic panel = new CaptureBasic();
			panel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent event) {
					System.out.println("mouseClicked");
				}
				@Override
				public void mouseMoved(MouseEvent event) {
					System.out.println("mouseMoved");
				}
				@Override
				public void mouseReleased(MouseEvent event) {
					System.out.println("mouseReleased");
				}
				@Override
				public void mousePressed(MouseEvent event) {
					System.out.println("mousePressed");
				}
				@Override
				public void mouseExited(MouseEvent event) {
					System.out.println("mouseExited");
				}
				@Override
				public void mouseDragged(MouseEvent event) {
					System.out.println("mouseDragged");
				}
			});
			frame.setContentPane(panel);
			frame.setVisible(true);
			frame.setSize(width + frame.getInsets().left + frame.getInsets().right, height + frame.getInsets().top + frame.getInsets().bottom);
			int index = 0;
			Mat temp = new Mat();
			while (frame.isShowing() && index < 500) {
				capture.read(mat);
				Imgproc.cvtColor(mat, temp, Imgproc.COLOR_RGB2GRAY);
				panel.image = panel.mat2BI(detectFace(mat));
				panel.repaint();
			}
			capture.release();
			frame.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 人脸识别
	 */
	public static Mat detectFace(Mat mat) throws Exception {
		CascadeClassifier faceDetector = new CascadeClassifier(localPath(XML_PATH));
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(mat, faceDetections);
		Rect[] rects = faceDetections.toArray();
		if (rects != null && rects.length >= 1) {
			for (Rect rect : rects) {
				Imgproc.rectangle(mat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 2);
			}
		}
		return mat;
	}

	/**
	 * 人型识别
	 */
	public static Mat detectPeople(Mat mat) {
		HOGDescriptor hog = new HOGDescriptor();
		hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());
		MatOfRect regions = new MatOfRect();
		MatOfDouble foundWeights = new MatOfDouble();
		hog.detectMultiScale(mat, regions, foundWeights);
		for (Rect rect : regions.toArray()) {
			Imgproc.rectangle(mat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(0, 0, 255), 2);
		}
		return mat;
	}

}
