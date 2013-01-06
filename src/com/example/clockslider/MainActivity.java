
package com.example.clockslider;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    /** the main UI control */
    private ClockSlider clockSlider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View dialogView = getLayoutInflater().inflate(R.layout.main, null);
        clockSlider = (ClockSlider) dialogView.findViewById(R.id.clockSlider);
        setContentView(dialogView);
    }
}
