/**
 * Copyright (C) 2010 Jesse Wilson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.clockslider;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A slider around a circle, to select a time between now and 12 hours from now.
 */
final class ClockSlider extends View {
    private static final int INSETS = 6;
    private static final int MINUTES_PER_HALF_DAY = 720;

    private int width;
    private int height;
    private int centerX;
    private int centerY;
    private int diameter;
    private RectF outerCircle;
    private RectF innerCircle;
    private RectF buttonCircle;
    private final Path path = new Path();

    private Paint lightGrey = new Paint();
    private Paint pink = new Paint();
    private Paint white = new Paint();
    private TextPaint duration = new TextPaint();

    private Paint buttonCirclePaint = new Paint();

    private Calendar start = new GregorianCalendar();
    private int startAngle = 0;
    private Calendar end = new GregorianCalendar();
    private String TAG = ClockSlider.class.getName();

    /** minutes to shush. */

    private boolean upPushed;
    private boolean downPushed;

    // Array of quantitie values that the slider iterates through
    private double[] mQuantityValues = new double[3901];
    // Current array index displayed in the slider
    private int mArrayIndex = 0;
    // Count of how many round trips in the slider user had to pick current array index
    private int mRoundTrips = 0;
    // The current angle
    private int mAngle = 0;

    public ClockSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "ClockSlider constructer called");

        lightGrey.setColor(Color.rgb(115, 115, 115));
        lightGrey.setAntiAlias(true);
        pink.setColor(Color.rgb(255, 0, 165));
        pink.setAntiAlias(true);
        white.setColor(Color.WHITE);
        white.setAntiAlias(true);
        duration.setSubpixelText(true);
        duration.setAntiAlias(true);
        duration.setColor(Color.WHITE);
        duration.setTextAlign(Paint.Align.CENTER);
        buttonCirclePaint.setColor(Color.argb(102, 115, 115, 115));
        buttonCirclePaint.setAntiAlias(true);
    }

    public void setColor(int color) {
        Log.d(TAG, "setColor() called");
        pink.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw() called");

        generateQuantityValues();

        if (getWidth() != width || getHeight() != height) {
            width = getWidth();
            height = getHeight();
            centerX = width / 2;
            centerY = height / 2;

            diameter = Math.min(width, height) - (2 * INSETS);
            int thickness = diameter / 15;

            int left = (width - diameter) / 2;
            int top = (height - diameter) / 2;
            int bottom = top + diameter;
            int right = left + diameter;
            outerCircle = new RectF(left, top, right, bottom);

            int innerDiameter = diameter - thickness * 2;
            innerCircle = new RectF(left + thickness, top + thickness, left + thickness
                    + innerDiameter, top + thickness + innerDiameter);

            int offset = thickness * 2;
            int buttonDiameter = diameter - offset * 2;
            buttonCircle = new RectF(left + offset, top + offset, left + offset + buttonDiameter,
                    top + offset + buttonDiameter);

            duration.setTextSize(diameter * 0.20f);
        }

        drawClock(canvas);
        drawClockTextAndButtons(canvas);
    }

    public Date getStart() {
        Log.d(TAG, "getStart() called: " + start.getTime());
        return start.getTime();
    }
    
    public double getQuantityValue(int index)
    {
        if(index < 0 || mQuantityValues.length == 0)
            return 0.00;
        
        return mQuantityValues[index];
    }

    public void setStart(Date now) {
        Log.d(TAG, "setStart() called");

        start.setTime(now);
        int minuteOfHalfDay = start.get(Calendar.HOUR_OF_DAY) * 60 + start.get(Calendar.MINUTE);
        if (minuteOfHalfDay > MINUTES_PER_HALF_DAY) {
            minuteOfHalfDay -= MINUTES_PER_HALF_DAY;
        }
        int angle = minuteOfHalfDay / 2; // 720 minutes per half-day -> 360
                                         // degrees per circle
        angle += 270; // clocks start at 12:00, but our angles start at 3:00
        startAngle = 270;
        postInvalidate();
    }

    public void setAngle(int angle)
    {
        this.mAngle  = angle;
    }
    
    public void setArrayIndex(int index)
    {
        if(index < 0)
            index = 0;
        
        index = index + (mRoundTrips * 48);
        
        if( mArrayIndex == index )
            return;
        
        if(mArrayIndex-index == 47) // one round trip
            mRoundTrips ++;
        else if(mArrayIndex-index == -47 && mRoundTrips != 0)
            mRoundTrips --;
        
        //this.mPreviousArrayIndex = mArrayIndex;
        this.mArrayIndex = index;
        
        Log.d(TAG, "mArrayIndex=" + mArrayIndex + ",index=" + index + ", mRoundTrips=" + mRoundTrips);
        
        postInvalidate();
    }

    public Date getEnd() {
        Log.d(TAG, "getEnd() called: " + end.getTime());
        return end.getTime();
    }

    /**
     * Draw a circle and an arc of the selected duration from start thru end.
     */
    private void drawClock(Canvas canvas) {
        Log.d(TAG, "drawClock() called");

        int sweepDegrees = (mAngle / 2) - 1;

        Log.d(TAG, "drawClock(): sweepDegrees = " + sweepDegrees);
        
        // the colored "filled" part of the circle
        drawArc(canvas, startAngle, sweepDegrees, pink);

        // the white selected part of the circle
        drawArc(canvas, startAngle + sweepDegrees, 2, white);

        // the grey empty part of the circle
        drawArc(canvas, startAngle + sweepDegrees + 2, 360 - sweepDegrees - 2, lightGrey);
    }

    private void drawArc(Canvas canvas, int startAngle, int sweepDegrees, Paint paint) {
        Log.d(TAG, "drawArc() called");

        if (sweepDegrees <= 0) {
            return;
        }

        path.reset();
        path.arcTo(outerCircle, startAngle, sweepDegrees);
        path.arcTo(innerCircle, startAngle + sweepDegrees, -sweepDegrees);
        path.close();
        canvas.drawPath(path, paint);
    }

    /**
     * Write labels in the middle of the circle like so: 2 1/2 hours 10:15 PM
     */
    private void drawClockTextAndButtons(Canvas canvas) {
        Log.d(TAG, "drawClockTextAndButtons() called");

        // up/down button backgrounds
        if (upPushed) {
            canvas.drawArc(buttonCircle, 270, 180, true, buttonCirclePaint);
        }
        if (downPushed) {
            canvas.drawArc(buttonCircle, 90, 180, true, buttonCirclePaint);
        }

        // clock text
        canvas.drawText(getQuantityValue(mArrayIndex) + "", centerX, centerY - (diameter * 0.08f), duration);
        canvas.drawText("kg", centerX, centerY + (diameter * 0.08f), duration);

        // up/down buttons
        Paint downPaint = downPushed ? white : lightGrey;
        canvas.drawRect(centerX - diameter * 0.32f, centerY - diameter * 0.01f, centerX - diameter
                * 0.22f, centerY + diameter * 0.01f, downPaint);
        
        Paint upPaint = upPushed ? white : lightGrey;
        canvas.drawRect(centerX + diameter * 0.22f, centerY - diameter * 0.01f, centerX + diameter
                * 0.32f, centerY + diameter * 0.01f, upPaint);
        canvas.drawRect(centerX + diameter * 0.26f, centerY - diameter * 0.05f, centerX + diameter
                * 0.28f, centerY + diameter * 0.05f, upPaint);
    }

    /**
     * Accept a touches near the circle's edge, translate it to an angle, and
     * update the sweep angle.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent() called");

        if (outerCircle == null) {
            return true; // ignore all events until the canvas is drawn
        }

        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        upPushed = false;
        downPushed = false;
        int distanceFromCenterX = centerX - touchX;
        int distanceFromCenterY = centerY - touchY;
        int distanceFromCenterSquared = distanceFromCenterX * distanceFromCenterX
                + distanceFromCenterY * distanceFromCenterY;
        float maxSlider = (diameter * 1.3f) / 2;
        float maxUpDown = (diameter * 0.8f) / 2;

        // handle increment/decrement
        if (distanceFromCenterSquared < (maxUpDown * maxUpDown)) 
        {
            boolean up = touchX > centerX;

            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_MOVE) {
                if (up) {
                    upPushed = true;
                } else {
                    downPushed = true;
                }
                postInvalidate();
                return true;
            }          
            
            int angle = up ? (15 + mAngle) : (705 + mAngle);
            if (angle > 720) {
                angle -= 720;
            }
            
            Log.d(TAG, "onTouchEvent(): One of the buttons pushed. Is it up button=" + up);
            int index = up ? (mArrayIndex+1) : (mArrayIndex-1);
            Log.d(TAG, "onTouchEvent(): Changing index to " + index);
            setArrayIndex(index);
            
            setAngle(angle);

            return true;

            // if it's on the slider, handle that
        } else if (distanceFromCenterSquared < (maxSlider * maxSlider)) {
            int angle = pointToAngle(touchX, touchY);
            /*
             * Convert the angle into a sweep angle. The sweep angle is a
             * positive angle between the start angle and the touched angle.
             */
            angle = 360 + angle - startAngle;
            int angleX2 = angle * 2;
            angleX2 = roundToNearest15(angleX2);
            
            // ===================
            int index;
            if(angleX2>720)
                index =  (angleX2 - 720) / 15;
            else
                index = angleX2 / 15;
            Log.d(TAG, "onTouchEvent(): Changing index to " + index);
            setArrayIndex(index);
  
            if (angleX2 > 720) {
                angleX2 = angleX2 - 720; // avoid mod because we prefer 720 over
                                         // 0
            }
            setAngle(angleX2);

            return true;

        } else {
            return false;
        }
    }

    private void generateQuantityValues() {
        int arrayIndex = 0;
        double arrayValue = 0;

        while (arrayValue < 10) {
            mQuantityValues[arrayIndex] = (Math.round(arrayValue * 100.0) / 100.0);
            arrayValue += 0.01;
            arrayIndex++;
        }

        mQuantityValues[arrayIndex] = (Math.round(arrayValue * 10.0) / 10.0);

        while (arrayValue < 300) {
            mQuantityValues[arrayIndex] = (Math.round(arrayValue * 10.0) / 10.0);
            arrayValue += 0.1;
            arrayIndex++;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure() called");

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // Don't use the full screen width on tablets!
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        float maxWidthInches = 2.3f;

        width = Math.min(width, (int) (maxWidthInches * metrics.densityDpi));
        height = Math.min(height, (int) (width * 0.7f));

        setMeasuredDimension(width, height);
    }

    /**
     * Returns the number of degrees (0-359) for the given point, such that 3pm
     * is 0 and 9pm is 180.
     */
    private int pointToAngle(int x, int y) {

        /* Get the angle from a triangle by dividing opposite by adjacent
         * and taking the atan. This code is careful not to divide by 0.
         *
         *
         *      adj | opp
         *          |
         * opp +180 | +270 adj
         * _________|_________
         *          |
         * adj  +90 | +0   opp
         *          |
         *      opp | adj
         *
         */

        if (x >= centerX && y < centerY) // [0..90]
        {
            double opp = x - centerX;
            double adj = centerY - y;
            Log.d(TAG, "pointToAngle(): [0..90] called. opp: " + opp + ", adj: " + adj + " = " + 270 + (int) Math.toDegrees(Math.atan(opp / adj)));
            return 270 + (int) Math.toDegrees(Math.atan(opp / adj));
        } else if (x > centerX && y >= centerY) // [90..180]
        {
            double opp = y - centerY;
            double adj = x - centerX;
            Log.d(TAG, "pointToAngle() [90..180] called. opp: " + opp + ", adj: " + adj + " = " + (int) Math.toDegrees(Math.atan(opp / adj)));
            return (int) Math.toDegrees(Math.atan(opp / adj));
        } else if (x <= centerX && y > centerY) // [180..270]
        {
            double opp = centerX - x;
            double adj = y - centerY;
            Log.d(TAG, "pointToAngle() // [180..270] called. opp: " + opp + ", adj: " + adj + " = " +  90 + (int) Math.toDegrees(Math.atan(opp / adj)));
            return 90 + (int) Math.toDegrees(Math.atan(opp / adj));
        } else if (x < centerX && y <= centerY) // [270..360]
        {
            double opp = centerY - y;
            double adj = centerX - x;
            Log.d(TAG, "pointToAngle() // [270..360] called. opp: " + opp + ", adj: " + adj + " = " +  180 + (int) Math.toDegrees(Math.atan(opp / adj)));
            return 180 + (int) Math.toDegrees(Math.atan(opp / adj));
        }

        throw new IllegalArgumentException();
    }

    /**
     * Rounds the angle to the nearest 7.5 degrees, which equals 15 minutes on a
     * clock. Not strictly necessary, but it discourages fat-fingered users from
     * being frustrated when trying to select a fine-grained period.
     */
    private int roundToNearest15(int angleX2) {
        Log.d(TAG, "roundToNearest15() called for angleX2:  " + angleX2 + " = "
                + ((angleX2 + 8) / 15) * 15);
        return ((angleX2 + 8) / 15) * 15;
    }
}
