package com.gauravk.clothifysample.FaceDetectionUtil;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gauravk.clothifysample.FaceDetectionUtil.common.CameraSource;
import com.gauravk.clothifysample.FaceDetectionUtil.common.CameraSourcePreview;
import com.gauravk.clothifysample.FaceDetectionUtil.common.FrameMetadata;
import com.gauravk.clothifysample.FaceDetectionUtil.common.GraphicOverlay;
import com.gauravk.clothifysample.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.io.IOException;
import java.util.List;

import static com.gauravk.clothifysample.Utils.FaceDetectionScanner.Constants.KEY_CAMERA_PERMISSION_GRANTED;
import static com.gauravk.clothifysample.Utils.FaceDetectionScanner.Constants.PERMISSION_REQUEST_CAMERA;

public class ScannerActivity extends AppCompatActivity {

    String TAG = "ScannerActivity";

    GraphicOverlay barcodeOverlay;
    CameraSourcePreview preview;
    TextView textDetected;

    private CameraSource mCameraSource = null;

    FaceContourDetectorProcessor faceDetectionProcessor;
    FaceDetectionResultListener faceDetectionResultListener = null;

    Bitmap bmpCapturedImage;
    List<FirebaseVisionFace> capturedFaces;

    boolean isComplete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getWindow() != null) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            Log.e(TAG, "Barcode scanner could not go into fullscreen mode!");
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        initView();

        if (preview != null)
            if (preview.isPermissionGranted(true, mMessageSender))
                new Thread(mMessageSender).start();
    }

    private void initView() {
        barcodeOverlay = findViewById(R.id.barcodeOverlay);
        preview = findViewById(R.id.preview);
        textDetected = findViewById(R.id.detection_text);
    }

    private void createCameraSource() {

        // To connect the camera resource with the detector

        mCameraSource = new CameraSource(this, barcodeOverlay);
        mCameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);


        // FaceContourDetectorProcessor faceDetectionProcessor = new FaceContourDetectorProcessor(detector);

        faceDetectionProcessor = new FaceContourDetectorProcessor(this);
        faceDetectionProcessor.setFaceDetectionResultListener(getFaceDetectionListener());

        mCameraSource.setMachineLearningFrameProcessor(faceDetectionProcessor);

        startCameraSource();
    }

    private FaceDetectionResultListener getFaceDetectionListener() {
        if (faceDetectionResultListener == null)
            faceDetectionResultListener = new FaceDetectionResultListener() {
                @Override
                public void onSuccess(@Nullable Bitmap originalCameraImage, @NonNull List<FirebaseVisionFace> faces, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {
                       boolean isEnable;
                       isEnable = faces.size() > 0;

                       for (FirebaseVisionFace face : faces)
                       {

                           // To get the results

                           Log.d(TAG, "Face bounds : " + face.getBoundingBox());

                           // To get this, we have to set the ClassificationMode attribute as ALL_CLASSIFICATIONS

                           Log.d(TAG, "Left eye open probability : " + face.getLeftEyeOpenProbability());
                           Log.d(TAG, "Right eye open probability : " + face.getRightEyeOpenProbability());
                           Log.d(TAG, "Smiling probability : " + face.getSmilingProbability());

                           // To get this, we have to enableTracking

                           Log.d(TAG, "Face ID : " + face.getTrackingId());

                       }

                       runOnUiThread(() -> {
                           Log.d(TAG, "button enable true ");
                           bmpCapturedImage = originalCameraImage;
                           capturedFaces = faces;
                           setDetectionText(isEnable);
                       });
                }

                @Override
                public void onFailure(@NonNull Exception e) {

                }
            };

        return faceDetectionResultListener;
    }

    private void setDetectionText(boolean isEnable) {
        textDetected.setText(isEnable ? R.string.face_detected : R.string.no_face_detected);
    }

    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());

        Log.d(TAG, "startCameraSource: " + code);

        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, PERMISSION_REQUEST_CAMERA);
            dlg.show();
        }

        if (mCameraSource != null && preview != null && barcodeOverlay != null) {
            try {
                Log.d(TAG, "startCameraSource: ");
                preview.start(mCameraSource, barcodeOverlay);
            } catch (IOException e) {
                Log.d(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        } else
            Log.d(TAG, "startCameraSource: not started");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult: " + requestCode);
        preview.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null)
            preview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Log.d(TAG, "handleMessage: ");

            if (preview != null)
                createCameraSource();

        }
    };

    private final Runnable mMessageSender = () -> {
        Log.d(TAG, "mMessageSender: ");
        Message msg = mHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_CAMERA_PERMISSION_GRANTED, false);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    };
}
