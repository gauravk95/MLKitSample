package com.gauravk.clothifysample.FaceDetectionUtil;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.gauravk.clothifysample.FaceDetectionUtil.common.GraphicOverlay;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.util.List;


/**
 * Graphic instance for rendering face contours graphic overlay view.
 */
public class FaceContourGraphic extends GraphicOverlay.Graphic {

    private static final float FACE_POSITION_RADIUS = 4.0f;
    private static final float ID_TEXT_SIZE = 30.0f;

    private final Paint facePositionPaint;
    private final Paint idPaint;

    private volatile FirebaseVisionFace firebaseVisionFace;
    private final Bitmap overlayImage;

    public FaceContourGraphic(GraphicOverlay overlay, FirebaseVisionFace face, Bitmap overlayImage) {
        super(overlay);

        this.firebaseVisionFace = face;
        this.overlayImage = overlayImage;
        final int selectedColor = Color.WHITE;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        idPaint = new Paint();
        idPaint.setColor(selectedColor);
        idPaint.setTextSize(ID_TEXT_SIZE);
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        FirebaseVisionFace face = firebaseVisionFace;
        if (face == null) {
            return;
        }

        FirebaseVisionFaceContour contour = face.getContour(FirebaseVisionFaceContour.ALL_POINTS);
        for (com.google.firebase.ml.vision.common.FirebaseVisionPoint point : contour.getPoints()) {
            float px = translateX(point.getX());
            float py = translateY(point.getY());
            canvas.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint);
        }

        drawSunglasses(canvas, face);

    }

    private void drawSunglasses(Canvas canvas, FirebaseVisionFace face) {
        float xOffset = scaleX(face.getBoundingBox().width() / 2.0f);
        float imageAspect = (float) overlayImage.getHeight() / overlayImage.getWidth();
        float yOffset = scaleY((face.getBoundingBox().width() * imageAspect) / 2.0f);
        float centerX = 0, centerY = 0;

        float rotate = 0;

        List<FirebaseVisionPoint> nose =
                face.getContour(FirebaseVisionFaceContour.NOSE_BRIDGE).getPoints();
        if (!nose.isEmpty() && nose.size() >= 2) {
            FirebaseVisionPoint topNose = nose.get(0);
            FirebaseVisionPoint bottomNose = nose.get(1);
            centerX = translateX(topNose.getX());
            centerY = translateY(topNose.getY());

            float centerX2 = translateX(bottomNose.getX());
            float centerY2 = translateY(bottomNose.getY());

            float temp = scaleY(Math.abs((bottomNose.getY() - topNose.getY())));
            yOffset = Math.min(yOffset, temp);

            rotate = (float) calculateAngle(
                    centerX, centerY, centerX2, centerY2);
        }

        float left = centerX - xOffset;
        float top = centerY - yOffset;
        float right = centerX + xOffset;
        float bottom = centerY + yOffset;

        //form the rect in which the sunglasses need to show
        RectF rect = new RectF(left, top, right, bottom);

        canvas.save();
        canvas.rotate(-rotate, centerX, centerY);
        canvas.drawBitmap(overlayImage,
                null, rect, facePositionPaint);
        canvas.restore();

    }

    public static double calculateAngle(double x1, double y1, double x2, double y2) {
        double angle = Math.toDegrees(Math.atan2(x2 - x1, y2 - y1));
        // Keep angle between 0 and 360
        angle = angle + Math.ceil(-angle / 360) * 360;

        return angle;
    }
}
