/**
 * Author: Ismar Slomic (ismar@slomic.no)
 * 
 * 
 */

package com.example.clockslider;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    /** the main UI control */
    private ClockSlider clockSlider;
    private double[] mValueArray = new double[3901];
    private String ARGS_VALUE = "value";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View dialogView = getLayoutInflater().inflate(R.layout.main, null);
        clockSlider = (ClockSlider) dialogView.findViewById(R.id.clockSlider);
        clockSlider.setEmptyCircleColor(Color.GRAY);
        clockSlider.setSelectedCircleColor(Color.WHITE);
        clockSlider.setSeekBarThumsColor(Color.BLACK);
        clockSlider.setButtonPushedColor(Color.WHITE);

        generateValueArray();
        clockSlider.setValueArray(mValueArray);
        
        if (savedInstanceState != null) // restoring previous state
        {
            double value = savedInstanceState.getDouble(ARGS_VALUE);
            clockSlider.setSelectedStepForValue(value);
        }
        else
            clockSlider.setSelectedStepForValue(75.5);

        setContentView(dialogView);
    }

    /** Keep the selected value in the seek bar in case of restoring the activity
     *  for instance by changing the orientation **/
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        double selectedValue = clockSlider.getSelectedValue();
        outState.putDouble(ARGS_VALUE, selectedValue);
    }

    /** Generates the values in the valueArray **/
    private void generateValueArray() {
        int arrayIndex = 0;
        double arrayValue = 0;

        while (arrayValue < 10) {
            mValueArray[arrayIndex] = (Math.round(arrayValue * 100.0) / 100.0);
            arrayValue += 0.01;
            arrayIndex++;
        }

        mValueArray[arrayIndex] = (Math.round(arrayValue * 10.0) / 10.0);

        while (arrayValue < 300) {
            mValueArray[arrayIndex] = (Math.round(arrayValue * 10.0) / 10.0);
            arrayValue += 0.1;
            arrayIndex++;
        }
    }
}
