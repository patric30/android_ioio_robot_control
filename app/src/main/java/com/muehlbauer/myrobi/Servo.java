package com.muehlbauer.myrobi;


import android.util.Log;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

public class Servo {

    private int       servoMax      = 2000;
    private int       servoMin      = 1000;
    private int       servoCenter   = 1500;
    private boolean   servoInverse  = false;
    private String    servoName     = "";
    private int       servoPosition = 0;
    private PwmOutput servoOutput;

    private static final String TAG = "Servo";

    // Constructor.
    public Servo(PwmOutput servo, String name, int offset, int reduce, int position, boolean inverse) {
        this.servoOutput   = servo;
        this.servoName     = name;
        this.servoInverse  = inverse;
        this.servoMax      = servoMax - reduce;
        this.servoMin      = servoMin + reduce;
        if (!this.servoInverse) {
            this.servoCenter = this.servoCenter + offset;
        } else {
            this.servoCenter = this.servoCenter - offset;
        }
        this.servoPosition = position;
        this.set(this.servoPosition);
        Log.d(TAG, "Servo: " + servoName + " initiated.");
    }

    // Set Servo to position -100..0..100
    public void set(int position) {
        if (position != servoPosition) {
            try {
                int pulseWidth = 0;
                if (position >= 0) {
                    pulseWidth = (int) (servoCenter + ((servoMax - servoCenter) * (Math.abs(position) / 100.0)));
                    if (servoInverse) {
                        pulseWidth = servoMin + (servoMax - pulseWidth);
                    }
                } else if (position < 0) {
                    pulseWidth = (int) (servoCenter - ((servoCenter - servoMin) * (Math.abs(position) / 100.0)));
                    if (servoInverse) {
                        pulseWidth = servoMax - (pulseWidth - servoMin);
                    }
                }
                servoOutput.setPulseWidth(pulseWidth);
                servoPosition = position;
                //Log.d(TAG, "Servo: " + servoName + "; Position: " + position + "; PulseWidth: " + pulseWidth);
            } catch (ConnectionLostException e) {
                Log.d(TAG, "ConnectionLostException: " + e.toString());
            }
        }
    }

    // Move Servo by position delta (position = -100..0..100)
    public void move(int delta) {
        int position = servoPosition + delta;
        if (position > 100) { position = 100; }
        if (position < -100) { position = -100; }
        this.set(position);
    }

    // Check servo position.
    public boolean isMax() {
        return (servoPosition == 100);
    }

    // Check servo position.
    public boolean isMin() {
        return (servoPosition == -100);
    }

    // Check servo position.
    public boolean isExactCenter() {
        return (servoPosition == 0);
    }

    // Check servo position.
    public boolean isAroundCenter() {
        return (servoPosition >= -5 && servoPosition <= 5);
    }

    public int getPosition() {
        return servoPosition;
    }
}
