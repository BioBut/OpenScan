package com.ethereal.openscan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.ethereal.openscan/cropper";

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            String methodCalled = call.method;
                            if (methodCalled.equals("cropImage")) {
                                Log.d("onCropImageCalled", "Crop started");
                                Bitmap bitmap = BitmapFactory.decodeFile(call.argument("path").toString());
                                int height = bitmap.getHeight();
                                int width = bitmap.getWidth();
                                double tl_x = call.argument("tl_x");
                                double tl_y = call.argument("tl_y");
                                double tr_x = call.argument("tr_x");
                                double tr_y = call.argument("tr_y");
                                double bl_x = call.argument("bl_x");
                                double bl_y = call.argument("bl_y");
                                double br_x = call.argument("br_x");
                                double br_y = call.argument("br_y");

                                if (OpenCVLoader.initDebug()) {
                                    Log.d("onOpenCVCalled", "OpenCV started");
                                    Mat mat = new Mat();
                                    Utils.bitmapToMat(bitmap, mat);

                                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
                                    Imgproc.GaussianBlur(mat, mat, new Size(5, 5), 0);
                                    Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
                                    Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);
                                    src_mat.put(0, 0, tl_x, tl_y, tr_x, tr_y, bl_x, bl_y, br_x, br_y);
                                    dst_mat.put(0, 0, 0.0, 0.0, width, 0.0, 0.0, height, width, height);
                                    Mat perspectiveTransform = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

                                    Imgproc.warpPerspective(mat, mat, perspectiveTransform, new Size(width, height));

                                    Utils.matToBitmap(map, bitmap);
                                    bitmap = Bitmap.createScaledBitmap(bitmap, 2480, 3508, true);
                                    FileOutputStream stream = null;
                                    try {
                                        stream = new FileOutputStream(new File("/storage/emulated/0/Download/test.jpg"));
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    String temp = stream.toString();
                                    Log.d("onCropOver", temp);
                                    result.success(true);
                                }
                            }
                        }
                );
    }
}