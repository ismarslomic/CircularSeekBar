/**
 * @Author: Ismar Slomic (ismar@slomic.no)
 */

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

package no.slomic.circularseekbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

final class CircularSeekBar extends View {
    /** Dimensions and graphical shapes of the circle and buttons **/
    private int mWidth;
    private int mHeight;
    private int mCenterX;
    private int mCenterY;
    private int mDiameter;
    private RectF mOuterCircle;
    private RectF mInnerCircle;
    private RectF mButtonCircle;
    private final Path mPath = new Path();
    private static final int INSETS = 6;
    private int mStepThumbTickness = 2;
    
    /** Circle colors **/
    private Paint mEmptyCircleColor = new Paint();
    private Paint mThumbColor = new Paint();
    private Paint mSelectedCircleColor = new Paint();

    /** Text syle for the text in the midle of the circle **/
    private Paint mTextStyle = new Paint();
    
    /** Buttons **/
    private boolean mIsIncreasePushed;
    private boolean mIsDecreasePushed;
    private Paint mButtonPushedColor = new Paint();
    private int mButtonChangeInterval = 5;
    
    /** Angles **/
    private int mStartAngle = 270; // 360 in path.arcTo
    private int mAngleIncrement = 10;
    
    /** Steps **/
    private int mSelectedStep = 0;
    private int mTotalSteps = 360 / mAngleIncrement; // 360 degrees
    private int mRoundTrips = 0; // count of round trips in the circle
  
    /** Array of values that the slider iterates through **/ 
    private double[] mValueArray = new double[0];
    private String mValueUnitName = "kg";
    
    /** Logging **/
    private String TAG = CircularSeekBar.class.getName();

    public CircularSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        /** Initialize colors of the circles **/
        mEmptyCircleColor.setColor(Color.rgb(115, 115, 115)); // grey color
        mEmptyCircleColor.setAntiAlias(true);
        mSelectedCircleColor.setColor(Color.rgb(255, 0, 165)); // pink color
        mSelectedCircleColor.setAntiAlias(true);
        mThumbColor.setColor(Color.WHITE);
        mThumbColor.setAntiAlias(true);
        
        /** Initialize the text paint **/
        mTextStyle.setSubpixelText(true);
        mTextStyle.setAntiAlias(true);
        mTextStyle.setColor(Color.WHITE);
        mTextStyle.setTextAlign(Paint.Align.CENTER);
        
        /** Initialize the buttons **/
        mButtonPushedColor.setColor(Color.argb(102, 115, 115, 115)); // light grey color
        mButtonPushedColor.setAntiAlias(true);
    }

    /****************** INTERFACE METHODS ****************/

    /**
     * Main method, orchestrating the drawing of the circular seek bar, buttons and rest of
     * the layout
     **/
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        /** A. Calculates dimension of the circular seek bar*/
        if (getWidth() != mWidth || getHeight() != mHeight) {
            mWidth = getWidth();
            mHeight = getHeight();
            mCenterX = mWidth / 2;
            mCenterY = mHeight / 2;

            mDiameter = Math.min(mWidth, mHeight) - (2 * INSETS);
            int thickness = mDiameter / 15;

            int left = (mWidth - mDiameter) / 2;
            int top = (mHeight - mDiameter) / 2;
            int bottom = top + mDiameter;
            int right = left + mDiameter;
            mOuterCircle = new RectF(left, top, right, bottom);

            int innerDiameter = mDiameter - thickness * 2;
            mInnerCircle = new RectF(left + thickness, top + thickness, left + thickness
                    + innerDiameter, top + thickness + innerDiameter);

            int offset = thickness * 2;
            int buttonDiameter = mDiameter - offset * 2;
            mButtonCircle = new RectF(left + offset, top + offset, left + offset + buttonDiameter,
                    top + offset + buttonDiameter);

            mTextStyle.setTextSize(mDiameter * 0.20f);
        }
        
        /** B. Calls the helper method to draw the circular seek bar **/
        drawCircularSeekBar(canvas);
        
        /** C. Calls the helper method to draw the text and buttons of the seek bar **/
        drawTextAndButtons(canvas);
    }
    
    /**
     * Accept a touches near the circle's edge, translate it to an angle, and
     * update the sweep angle.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent() called");

        if (mOuterCircle == null) {
            return true; // ignore all events until the canvas is drawn
        }

        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        mIsIncreasePushed = false;
        mIsDecreasePushed = false;
        
        int distanceFromCenterX = mCenterX - touchX;
        int distanceFromCenterY = mCenterY - touchY;
        int distanceFromCenterSquared = distanceFromCenterX * distanceFromCenterX
                + distanceFromCenterY * distanceFromCenterY;
        float maxSlider = (mDiameter * 1.3f) / 2;
        float maxUpDown = (mDiameter * 0.8f) / 2;

     
        // handle increment/decrement button events
        if (distanceFromCenterSquared < (maxUpDown * maxUpDown)) 
        {
            boolean isIncrease = touchX > mCenterX;

            if( event.getAction() == MotionEvent.ACTION_DOWN || 
                    event.getAction() == MotionEvent.ACTION_MOVE)
            {
                if (isIncrease) 
                {
                    mIsIncreasePushed = true;
                    increaseStep(mButtonChangeInterval);
                }
                 else 
                 {
                    mIsDecreasePushed = true;
                    decreaseStep(mButtonChangeInterval);
                 }
            }
            
            postInvalidate();
            return true;
            
        // if it's on the slider, handle sliders events
        } else if (distanceFromCenterSquared < (maxSlider * maxSlider)) 
        {
            int angle = pointToAngle(touchX, touchY);
            int sweepAngle = convertToSweepAngle(angle);
            int step = getStepForSweepAngle(sweepAngle);
            setSelectedStep(step);
            
            return true;

        } else {
            return false;
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
    
    /**************** COLOR METHODS ****************/
    
    /** Sets the color of the remaining/empty circle in the middle**/
    public void setEmptyCircleColor(int color)
    {
        mEmptyCircleColor.setColor(color);
    }
    
    /** Sets the color of the circle in the middle for selected steps from start to selected step**/
    public void setSelectedCircleColor(int color)
    {
        mSelectedCircleColor.setColor(color);
    }
    
    /** Sets the color of the seek bar thumb**/
    public void setSeekBarThumsColor(int color)
    {
        mThumbColor.setColor(color);
    }
    
    /** Sets the color of the buttons in the middle when they are pushed **/
    public void setButtonPushedColor(int color)
    {
        mButtonPushedColor.setColor(color);
    }

    /**************** DRAWING HELPER METHODS ****************/
    
    /**
     * Draw a circle and an arc of the selected step, from start thru end.
     */
    private void drawCircularSeekBar(Canvas canvas) {
        int sweepDegrees = getSweepAngleForStep(mSelectedStep) - 1;
        int startAngle = mStartAngle;
        
        // the colored "filled" part of the circle
        drawArc(canvas, startAngle, sweepDegrees, mSelectedCircleColor);
        Log.d(TAG, "drawCircularSeekBar() selected part startAngle: " + mStartAngle + " sweepDegrees: " + sweepDegrees);
        
        // the white selected part of the circle
        startAngle += sweepDegrees;
        drawArc(canvas, startAngle, mStepThumbTickness, mThumbColor);
        Log.d(TAG, "drawCircularSeekBar() thumb startAngle:" + (mStartAngle + sweepDegrees) + " sweepDegrees: " + mStepThumbTickness);
        
        // the grey empty part of the circle
        startAngle += mStepThumbTickness;
        drawArc(canvas, startAngle, 360 - sweepDegrees - mStepThumbTickness, mEmptyCircleColor);
        Log.d(TAG, "drawCircularSeekBar() empty part startAngle: " + (mStartAngle + sweepDegrees + mStepThumbTickness) + " sweepDegrees: " + (360 - sweepDegrees - mStepThumbTickness));
    }
    
    /**
     * Write labels in the middle of the circle
     */
    private void drawTextAndButtons(Canvas canvas) {
        Log.d(TAG, "drawClockTextAndButtons() called");

        // up/down button backgrounds
        if (mIsIncreasePushed) {
            canvas.drawArc(mButtonCircle, 270, 180, true, mButtonPushedColor);
        }
        if (mIsDecreasePushed) {
            canvas.drawArc(mButtonCircle, 90, 180, true, mButtonPushedColor);
        }

        // Writing the text in the middle
        canvas.drawText(getValueAtStep(mSelectedStep) + "", mCenterX, mCenterY - (mDiameter * 0.08f), mTextStyle);
        canvas.drawText(mValueUnitName, mCenterX, mCenterY + (mDiameter * 0.08f), mTextStyle);

        // up/down buttons
        Paint downPaint = mIsDecreasePushed ? mThumbColor : mEmptyCircleColor;
        canvas.drawRect(mCenterX - mDiameter * 0.32f, mCenterY - mDiameter * 0.01f, mCenterX - mDiameter
                * 0.22f, mCenterY + mDiameter * 0.01f, downPaint);
        
        Paint upPaint = mIsIncreasePushed ? mThumbColor : mEmptyCircleColor;
        canvas.drawRect(mCenterX + mDiameter * 0.22f, mCenterY - mDiameter * 0.01f, mCenterX + mDiameter
                * 0.32f, mCenterY + mDiameter * 0.01f, upPaint);
        canvas.drawRect(mCenterX + mDiameter * 0.26f, mCenterY - mDiameter * 0.05f, mCenterX + mDiameter
                * 0.28f, mCenterY + mDiameter * 0.05f, upPaint);
    }
    
    /** Generic method for drawing arcs **/
    private void drawArc(Canvas canvas, int startAngle, int sweepDegrees, Paint paint) {
        Log.d(TAG, "drawArc() called");

        if (sweepDegrees <= 0) 
            return;

        mPath.reset();
        mPath.arcTo(mOuterCircle, startAngle, sweepDegrees);
        mPath.arcTo(mInnerCircle, startAngle + sweepDegrees, -sweepDegrees);
        mPath.close();
        canvas.drawPath(mPath, paint);
    }
    
    /******************* GETTERS AND SETTES *************/
    
    /** Returns sweep angle for given step.
     * 
     *  Example: step 12 returns sweep angle 120 
     */
    public int getSweepAngleForStep(int step)
    {
        step = step % mTotalSteps; // in case the current step belong to other round trips
        return step * mAngleIncrement;
    }

    /** Returns step for given sweep angle.
     * 
     *  Example: sweep angle 120 returns step 12
     */
    public int getStepForSweepAngle(int sweepAngle)
    {
        return sweepAngle / mAngleIncrement;
    }
    
    /** Returns the round trips in the circle seek bar **/
    public int getRoundTrips()
    {
        return this.mRoundTrips;
    }
    
    /** Sets the array of double values of the seek bar and invalidate current step selection **/
    public void setValueArray(double[] values)
    {
        this.mValueArray = values;
        this.mSelectedStep = 0;
        this.mRoundTrips = 0;
        postInvalidate();
    }
    
    /**
     * Sets the selected step in the circle according to the current round trip. 
     * 
     * Must be positive value and less then valueArray.length
     * 
     */
    public void setSelectedStep(int step)
    {
        if(step < 0 ) // ignore negative steps
            step = 0;
        
        if(step > mTotalSteps) // the step is set from the code
        {
            mRoundTrips = (step / mTotalSteps); // set round trips
            mSelectedStep = step; // set selected step
            Log.d(TAG, "Setting selected step to: " + mSelectedStep + " and round trips is now: " + mRoundTrips);
        }
        else // the step is set from seek bar
        {
            step += (mRoundTrips * mTotalSteps);
            
            if( mSelectedStep == step || step > mValueArray.length) // do nothing if the step is the same as the current selected step or greater then array lenght
            {
                step = 0;
                return; 
            }
            
            Log.d(TAG, "Selected step: " + mSelectedStep + ", new step: " + step + ", total steps: " + mTotalSteps + ", round trips: " + mRoundTrips + ", modulus: " + (mSelectedStep%mTotalSteps));
            
            if(mSelectedStep-step == mTotalSteps-1) // add one round trip
                mRoundTrips ++;
            else if(mSelectedStep - step == -(mTotalSteps-1) && mRoundTrips != 0) // reduce one round trip
                mRoundTrips --;
    
            this.mSelectedStep = step;
     
            Log.d(TAG, "Setting selected step to: " + mSelectedStep + " and round trips is now: " + mRoundTrips);
        }
        postInvalidate();
    }
    
    /** Returns value at given step. 
     * 
     * @param step positive value in the index range of the valueArray
     * @return value at given step. If the step is outside of the index range of valueArray 0.00 will be returned 
     */
    public double getValueAtStep(int step)
    {
        if(step < 0 || mValueArray == null || mValueArray.length == 0 || step >= mValueArray.length)
            return 0.00;
        
        return mValueArray[step];
    }
    
    /** 
     *  Returns the value for the selected step in the seek bar.
     *  
     *  @return value for the selected step or 0.00 if the valueArray is empty
     * 
     * **/
    public double getSelectedValue()
    {
        if(mValueArray != null && mValueArray.length > 0)
            return mValueArray[mSelectedStep];
        else
            return 0.00;
    }
    
    
    /**
     * Finds the first occurrence of the value in the valueArray and 
     * sets the selected step to it. 
     * 
     * @param value that is going to be selected in the seek bar. If not found no change will be done.
     */
    public void setSelectedStepForValue(double value)
    {
        // find the first index/step of the value in the valueArray
        if(mValueArray != null && mValueArray.length > 0)
        {
            for (int step = 0; step < mValueArray.length; step++) 
            {
                if(mValueArray[step] == value)
                {
                    // set the selected step to the value index/step
                    setSelectedStep(step);
                    return;
                }
            }
        }
    }
    
    /** The unit name of the values. This name is displayed in the middle of the circle **/
    public void setValueUnitName(String name)
    {
        mValueUnitName = name;
    }
    
    /** Returns the unit name of the values in the seek bar**/
    public String getValueUnitName()
    {
        return mValueUnitName;
    }
    
    /** Returns the selected step in the seek bar **/
    public int getSelectedStep()
    {
        return mSelectedStep;
    }
    
    
    /**
     * Sets the increment/decrement value of the seek bar
     * 
     * @param stepInterval positive value
     */
    public void setButtonChangeInterval(int stepInterval)
    {
        if(stepInterval < 0 )
            return;
        
        mButtonChangeInterval = stepInterval;
    }
       
    /***********************  STEP MANAGEMENT METHODS ******************/
    
    /** 
     * Increases selected step with given increment. 
     * 
     * @param increment positive value less then valueArray.length
     * 
     * **/
    public void increaseStep(int increment)
    {
        if(increment < 0 )
            return;
        
        int step = mSelectedStep + increment;
        
        if(step >= mValueArray.length)
            step = mValueArray.length-1;
        
        mRoundTrips = (step / mTotalSteps);
        mSelectedStep = step;
        
        postInvalidate();
    }
    
    /** 
     * Decreases selected step with given decrement. 
     * 
     * @param decrement positive value. Will not decrease to step below zero.
     * 
     * **/
    public void decreaseStep(int decrement)
    {
        int step = mSelectedStep - decrement;
        
        if(step < 0)
            step = 0;
        
        mRoundTrips = (step / mTotalSteps);
        mSelectedStep = step;
        
        postInvalidate();
    }

    /****************** METHODS FOR GETTING SELECTED ANGLE ***********/
    
    /**
     * Convert the angle into a sweep angle. The sweep angle is a
     * positive angle between the start angle and the touched angle.
     */
    public int convertToSweepAngle(int angle)
    {
        int sweepAngle = 360 + angle - mStartAngle;
        sweepAngle = roundToNearest(sweepAngle);
        if (sweepAngle > 360) {
            sweepAngle = sweepAngle - 360;
        }
        
        Log.d(TAG, "Converting from angle: " + angle + " to sweepAngle: " + sweepAngle);
        
        return sweepAngle;
    }
    
    /**
     * Returns the number of degrees (0-359) for the given point, such that 0 starts at 90 degrees  
     * and 180 degrees is at 270 degrees.
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

        if (x >= mCenterX && y < mCenterY) // [0..90]
        {
            double opp = x - mCenterX;
            double adj = mCenterY - y;
            Log.d(TAG, "pointToAngle(): [0..90] called. opp: " + opp + ", adj: " + adj + " = " + (270 + (int) Math.toDegrees(Math.atan(opp / adj))));
            return 270 + (int) Math.toDegrees(Math.atan(opp / adj));
        } else if (x > mCenterX && y >= mCenterY) // [90..180]
        {
            double opp = y - mCenterY;
            double adj = x - mCenterX;
            Log.d(TAG, "pointToAngle() [90..180] called. opp: " + opp + ", adj: " + adj + " = " + (int) Math.toDegrees(Math.atan(opp / adj)));
            return (int) Math.toDegrees(Math.atan(opp / adj));
        } else if (x <= mCenterX && y > mCenterY) // [180..270]
        {
            double opp = mCenterX - x;
            double adj = y - mCenterY;
            Log.d(TAG, "pointToAngle() // [180..270] called. opp: " + opp + ", adj: " + adj + " = " +  (90 + (int) Math.toDegrees(Math.atan(opp / adj))));
            return 90 + (int) Math.toDegrees(Math.atan(opp / adj));
        } else if (x < mCenterX && y <= mCenterY) // [270..359]
        {
            double opp = mCenterY - y;
            double adj = mCenterX - x;
            Log.d(TAG, "pointToAngle() // [270..360] called. opp: " + opp + ", adj: " + adj + " = " +  (180 + (int) Math.toDegrees(Math.atan(opp / adj))));
            return 180 + (int) Math.toDegrees(Math.atan(opp / adj));
        }

        throw new IllegalArgumentException();
    }

    /**
     * Rounds the angle to the nearest 5 degrees, which equals 1 step on a
     * circle seek bar. Not strictly necessary, but it discourages fat-fingered users from
     * being frustrated when trying to select a fine-grained period.
     */
    private int roundToNearest(int angle) 
    {
        return ((angle + 5) / 10) * 10;
    }
}
