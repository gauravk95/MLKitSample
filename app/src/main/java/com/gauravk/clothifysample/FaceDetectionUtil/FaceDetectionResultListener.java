package com.gauravk.clothifysample.FaceDetectionUtil;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gauravk.clothifysample.FaceDetectionUtil.common.FrameMetadata;
import com.gauravk.clothifysample.FaceDetectionUtil.common.GraphicOverlay;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.util.List;

/**
 * Created by Gaurav.
 */
public interface FaceDetectionResultListener {
    void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay);

    void onFailure(@NonNull Exception e);
}
