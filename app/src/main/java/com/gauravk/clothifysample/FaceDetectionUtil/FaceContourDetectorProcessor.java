package com.gauravk.clothifysample.FaceDetectionUtil;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.graphics.BitmapFactory;
import android.util.Log;

import com.gauravk.clothifysample.FaceDetectionUtil.common.CameraImageGraphic;
import com.gauravk.clothifysample.FaceDetectionUtil.common.FrameMetadata;
import com.gauravk.clothifysample.FaceDetectionUtil.common.GraphicOverlay;
import com.gauravk.clothifysample.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.util.List;

/**
 * Face Contour Demo.
 */
public class FaceContourDetectorProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    private static final String TAG = "FaceContourDetectorProc";

    private final FirebaseVisionFaceDetector detector;

    FaceDetectionResultListener faceDetectionResultListener;

    private Bitmap overlayImage;

    public FaceContourDetectorProcessor(Context mContext) {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .enableTracking()
                        .build();

        overlayImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.sunglasses);

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
    }

    public FaceContourDetectorProcessor(FirebaseVisionFaceDetector detector) {
        this.detector = detector;
    }

    public FaceDetectionResultListener getFaceDetectionResultListener() {
        return faceDetectionResultListener;
    }

    public void setFaceDetectionResultListener(FaceDetectionResultListener faceDetectionResultListener) {
        this.faceDetectionResultListener = faceDetectionResultListener;
    }

    @Override
    public void stop() {
        try {
            detector.close();
            if (overlayImage != null && !overlayImage.isRecycled()) {
                overlayImage.recycle();
                overlayImage = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Contour Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        for (int i = 0; i < faces.size(); ++i) {
            FirebaseVisionFace face = faces.get(i);
            FaceContourGraphic faceGraphic = new FaceContourGraphic(graphicOverlay, face, overlayImage);
            graphicOverlay.add(faceGraphic);
        }
        graphicOverlay.postInvalidate();

        if (faceDetectionResultListener != null)
            faceDetectionResultListener.onSuccess(originalCameraImage, faces, frameMetadata, graphicOverlay);
    }

    @Override
    protected void onFailure(@NonNull Exception e) {

        if (faceDetectionResultListener != null)
            faceDetectionResultListener.onFailure(e);

        Log.e(TAG, "Face detection failed " + e);
    }
}