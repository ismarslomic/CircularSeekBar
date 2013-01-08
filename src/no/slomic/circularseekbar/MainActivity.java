/**
 * Author: Ismar Slomic (ismar@slomic.no)
 * 
 * 
 */

package no.slomic.circularseekbar;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    /** the main UI control */
    private CircularSeekBar mCircularSeekBar;
    private double[] mValueArray = new double[3901];
    private String ARGS_VALUE = "value";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View dialogView = getLayoutInflater().inflate(R.layout.main, null);
        mCircularSeekBar = (CircularSeekBar) dialogView.findViewById(R.id.circularSeekBar);
        
        // Setting the colors of the circle
        mCircularSeekBar.setEmptyCircleColor(Color.GRAY);
        mCircularSeekBar.setSelectedCircleColor(Color.WHITE);
        mCircularSeekBar.setSeekBarThumsColor(Color.BLACK);
        mCircularSeekBar.setButtonPushedColor(Color.LTGRAY);

        // Setting the values that seek bar will iterate through
        generateValueArray();
        mCircularSeekBar.setValueArray(mValueArray);
        
        // Setting the start value
        if (savedInstanceState != null) // restoring previous state
        {
            double value = savedInstanceState.getDouble(ARGS_VALUE);
            mCircularSeekBar.setSelectedStepForValue(value);
        }
        else // setting default value
            mCircularSeekBar.setSelectedStepForValue(75.5);

        // Setting the value unit name displayed in the middle, below the value 
        mCircularSeekBar.setValueUnitName("inch");
        
        // Sets how many steps should be changed when pressing + and - buttons
        mCircularSeekBar.setButtonChangeInterval(10);
        
        setContentView(dialogView);
    }

    /** Keep the selected value in the seek bar in case of restoring the activity
     *  for instance by changing the orientation **/
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        double selectedValue = mCircularSeekBar.getSelectedValue();
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
