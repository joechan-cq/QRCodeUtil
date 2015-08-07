/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.google.zxing.clent.adnroid.R;
import com.google.zxing.client.android.camera.CameraManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 80L;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 6;

    private CameraManager cameraManager;
    private final Paint paint;
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    private final int laserColor;
    private final int resultPointColor;
    private int scannerAlpha;
    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;
    private boolean laserLinePortrait = true;
    Rect mRect;
    int i = 0;
    GradientDrawable mDrawable;
    Paint textPaint;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRect = new Rect();
        int left = Color.parseColor("#CACACA");
        int center = Color.RED;
        int right = Color.parseColor("#CACACA");
        mDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{left, center, right});
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
        laserColor = resources.getColor(R.color.viewfinder_laser);
        resultPointColor = resources.getColor(R.color.possible_result_points);
        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<>(5);
        lastPossibleResultPoints = null;
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    public void setCornerRadii(GradientDrawable drawable, float r0, float r1, float r2, float r3) {
        drawable.setCornerRadii(new float[]{r0, r0, r1, r1, r2, r2, r3, r3});
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }
        Rect frame = cameraManager.getFramingRect();
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            paint.setColor(Color.GREEN);
            //画出四个角
            canvas.drawRect(frame.left, frame.top, frame.left + 30, frame.top + 10, paint);
            canvas.drawRect(frame.left, frame.top, frame.left + 10, frame.top + 30, paint);
            canvas.drawRect(frame.right - 30, frame.top, frame.right, frame.top + 10, paint);
            canvas.drawRect(frame.right - 10, frame.top, frame.right, frame.top + 30, paint);

            canvas.drawRect(frame.left, frame.bottom - 10, frame.left + 30, frame.bottom, paint);
            canvas.drawRect(frame.left, frame.bottom - 30, frame.left + 10, frame.bottom, paint);
            canvas.drawRect(frame.right - 30, frame.bottom - 10, frame.right, frame.bottom, paint);
            canvas.drawRect(frame.right - 10, frame.bottom - 30, frame.right, frame.bottom, paint);

            int middle = frame.width() / 2;
            textPaint.setTextSize(20);
            textPaint.setColor(Color.WHITE);

            // Draw a red "laser scanner" line through the middle to show
            // decoding is active
            paint.setColor(Color.RED);
            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;

            //上下走的线
            if (laserLinePortrait) {

                if ((i += 5) < frame.bottom - frame.top) {
                /*
                 * canvas.drawRect(frame.left + 2, frame.top - 2 + i, frame.right - 1, frame.top + 2 + i,
				 * paint);
				 */
                    int r = 8;
                    mDrawable.setShape(GradientDrawable.RECTANGLE);
                    mDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                    setCornerRadii(mDrawable, r, r, r, r);
                    mRect.set(frame.left + 2, frame.top - 3 + i, frame.right - 1, frame.top + 3 + i);
                    mDrawable.setBounds(mRect);
                    mDrawable.draw(canvas);
                    invalidate();
                } else {
                    i = 0;
                }

            } else {
                float left = frame.left + (frame.right - frame.left) / 2 - 2;
                canvas.drawRect(left, frame.top, left + 2, frame.bottom - 2, paint);
            }

            List<ResultPoint> currentPossible = possibleResultPoints;
            List<ResultPoint> currentLast = lastPossibleResultPoints;
            int frameLeft = frame.left;
            int frameTop = frame.top;
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null;
            } else {
                possibleResultPoints = new ArrayList<>(5);
                lastPossibleResultPoints = currentPossible;
                paint.setAlpha(CURRENT_POINT_OPACITY);
                paint.setColor(resultPointColor);
                synchronized (currentPossible) {
                    for (ResultPoint point : currentPossible) {
//                        canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
//                                frameTop + (int) (point.getY() * scaleY),
//                                POINT_SIZE, paint);
                    }
                }
            }
            if (currentLast != null) {
                paint.setAlpha(CURRENT_POINT_OPACITY / 2);
                paint.setColor(resultPointColor);
                synchronized (currentLast) {
                    float radius = POINT_SIZE / 2.0f;
                    for (ResultPoint point : currentLast) {
//                        canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
//                                frameTop + (int) (point.getY() * scaleY),
//                                radius, paint);
                    }
                }
            }

            // Request another update at the animation interval, but only repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(ANIMATION_DELAY,
                    frame.left - POINT_SIZE,
                    frame.top - POINT_SIZE,
                    frame.right + POINT_SIZE,
                    frame.bottom + POINT_SIZE);
        }
    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }

}
