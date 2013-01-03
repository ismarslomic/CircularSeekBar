
package com.example.clockslider;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import java.util.Date;

public class MainActivity extends Activity {
    /** the main UI control */
    private ClockSlider clockSlider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View dialogView = getLayoutInflater().inflate(R.layout.main, null);
        clockSlider = (ClockSlider) dialogView.findViewById(R.id.clockSlider);
        clockSlider.setStart(new Date());
        clockSlider.setColor(Color.rgb(0x33, 0xb5, 0xe5));
        setContentView(dialogView);
    }
}
