package com.muehlbauer.myrobi;

import android.util.Log;
import ioio.lib.api.PwmOutput;

public class Robot {

    private final int SERVO_HIP_OFFSET  = 30;
    private final int SERVO_STEP_OFFSET = -20;
    private final int SERVO_HIP_REDUCE  = 300;
    private final int SERVO_STEP_REDUCE = 280;

    private Servo servoHip;
    private Servo servoStep;
    private Servo servoArmL;
    private Servo servoArmR;
    private Servo servoHead;

    private int cycle = 0;
    public enum ACTION {
        NONE, WALK, DANCE, LEFT, RIGHT, BACK, HOME;
    }
    private ACTION action = ACTION.NONE;

    private static final String TAG = "Robot";

    // Constructor.
    public Robot (PwmOutput servoHipOutput,
                      PwmOutput servoStepOutput,
                      PwmOutput servoArmLOutput,
                      PwmOutput servoArmROutput,
                      PwmOutput servoHeadOutput) {
        this.servoHip  = new Servo(servoHipOutput,  "Hip",  SERVO_HIP_OFFSET,  SERVO_HIP_REDUCE,  0, false);
        this.servoStep = new Servo(servoStepOutput, "Step", SERVO_STEP_OFFSET, SERVO_STEP_REDUCE, 0, false);
        this.servoArmL = new Servo(servoArmLOutput, "ArmL", 0, 0, 0, true);
        this.servoArmR = new Servo(servoArmROutput, "ArmR", 0, 0, 0, false);
        this.servoHead = new Servo(servoHeadOutput, "Head", 0, 0, 0, true);
        Log.d(TAG, "Robot initiated.");
    }

    public ACTION getAction() {
        return action;
    }

    public void home() {
        if (action != ACTION.NONE) {
            action = ACTION.HOME;
            // Reset all variables.
            cycle = 0;
            // Move Servos to home.
            if (servoHip.isAroundCenter()) {
                servoHip.set(0);
            }
            if (servoHip.getPosition() > 0) {
                servoHip.move(-5);
            }
            if (servoHip.getPosition() < 0) {
                servoHip.move(5);
            }
            if (servoStep.isAroundCenter()) {
                servoStep.set(0);
            }
            if (servoStep.getPosition() > 0) {
                servoStep.move(-5);
            }
            if (servoStep.getPosition() < 0) {
                servoStep.move(5);
            }
            if (servoHead.isAroundCenter()) {
                servoHead.set(0);
            }
            if (servoHead.getPosition() > 0) {
                servoHead.move(-5);
            }
            if (servoHead.getPosition() < 0) {
                servoHead.move(5);
            }
            if (servoArmL.getPosition() > -100) {
                servoArmL.move(-5);
            }
            if (servoArmR.getPosition() > -100) {
                servoArmR.move(-5);
            }
            // Set action to NONE once home position is reached.
            if (servoHip.isExactCenter() &&
                    servoStep.isExactCenter() &&
                    servoHead.isExactCenter() &&
                    servoArmL.isMin() &&
                    servoArmR.isMin()) {
                action = ACTION.NONE;
                Log.d(TAG, "Back to home position.");
            }
        }
    }

    public void homePosition() {
        // Move Servos to home.
        servoHip.set(0);
        servoStep.set(0);
        servoHead.set(0);
        servoArmL.set(-100);
        servoArmR.set(-100);
        // Reset all variables.
        cycle = 0;
        action = ACTION.NONE;
        Log.d(TAG, "Forced home position.");
    }

    public void dance() {
        if (action != ACTION.DANCE) {
            // Set servos to dance start position.
            servoHip.set(0);
            servoStep.set(0);
            servoArmL.set(0);
            servoArmR.set(0);
            servoHead.set(0);
            // Set cycle to start.
            cycle = 0;
            // Set action to DANCE.
            action = ACTION.DANCE;
            Log.d(TAG, "DANCE initiated.");
        }
        // Start 2 cycle dance move.
        if (cycle == 0 && !servoStep.isMin()) {
            servoStep.move(-5);
            servoArmL.move(-5);
            servoArmR.move(+5);
            servoHead.move(-5);
        }
        if (cycle == 0 && servoStep.isMin()) {
            cycle++;
        }
        if (cycle == 1 && !servoStep.isMax()) {
            servoStep.move(+5);
            servoArmL.move(+5);
            servoArmR.move(-5);
            servoHead.move(+5);
        }
        if (cycle == 1 && servoStep.isMax()) {
            cycle = 0;
        }
    }

    public void walk () {
        if (action != ACTION.WALK) {
            // Set servos to start position.
            servoHip.set(0);
            servoStep.set(0);
            servoArmL.set(-100);
            servoArmR.set(-100);
            servoHead.set(0);
            // Set cycle to start.
            cycle = 0;
            // Set action to DANCE.
            action = ACTION.WALK;
            Log.d(TAG, "WALK initiated.");
        }
        // Cycle 0: Lift right foot.
        if (cycle == 0 && !servoHip.isMax()) {
            servoHip.move(5);
        }
        if (cycle == 0 && servoHip.isMax()) {
            cycle++;
        }
        // Cycle 1: Move left foot forward.
        if (cycle == 1 && !servoStep.isMin()) {
            servoStep.move(-5);
            //servoArmL.move(1);
            //servoArmR.move(-1);
        }
        if (cycle == 1 && servoStep.isMin()) {
            cycle++;
        }
        // Cycle 2: Lift left foot.
        if (cycle == 2 && !servoHip.isMin()) {
            servoHip.move(-5);
        }
        if (cycle == 2 && servoHip.isMin()) {
            cycle++;
        }
        // Cycle 3: Move right foot forward.
        if (cycle == 3 && !servoStep.isMax()) {
            servoStep.move(5);
            //servoArmL.move(-1);
            //servoArmR.move(1);
        }
        if (cycle == 3 && servoStep.isMax()) {
            cycle = 0;
        }
    }

    public void back () {
        if (action != ACTION.BACK) {
            // Set servos to start position.
            servoHip.set(0);
            servoStep.set(0);
            servoArmL.set(-100);
            servoArmR.set(-100);
            servoHead.set(0);
            // Set cycle to start.
            cycle = 0;
            // Set action to DANCE.
            action = ACTION.BACK;
            Log.d(TAG, "BACK initiated.");
        }
        // Cycle 0: Lift right foot.
        if (cycle == 0 && !servoHip.isMax()) {
            servoHip.move(5);
        }
        if (cycle == 0 && servoHip.isMax()) {
            cycle++;
        }
        // Cycle 1: Move left foot back.
        if (cycle == 1 && !servoStep.isMax()) {
            servoStep.move(5);
        }
        if (cycle == 1 && servoStep.isMax()) {
            cycle++;
        }
        // Cycle 2: Lift left foot.
        if (cycle == 2 && !servoHip.isMin()) {
            servoHip.move(-5);
        }
        if (cycle == 2 && servoHip.isMin()) {
            cycle++;
        }
        // Cycle 3: Move right foot back.
        if (cycle == 3 && !servoStep.isMin()) {
            servoStep.move(-5);
        }
        if (cycle == 3 && servoStep.isMin()) {
            cycle = 0;
        }
    }

    public void left () {
        if (action != ACTION.LEFT) {
            // Set servos to start position.
            servoHip.set(0);
            servoStep.set(0);
            servoArmL.set(-100);
            servoArmR.set(-100);
            servoHead.set(0);
            // Set cycle to start.
            cycle = 0;
            // Set action to DANCE.
            action = ACTION.LEFT;
            Log.d(TAG, "LEFT initiated.");
        }
        // Cycle 0: Lift right foot.
        if (cycle == 0 && !servoHip.isMax()) {
            servoHip.move(3);
        }
        if (cycle == 0 && servoHip.isMax()) {
            cycle++;
        }
        // Cycle 1: Move right foot forward.
        if (cycle == 1 && !servoStep.isMin()) {
            servoStep.move(-3);
        }
        if (cycle == 1 && servoStep.isMin()) {
            cycle++;
        }
        // Cycle 2: Right foot down.
        if (cycle == 2 && !servoHip.isAroundCenter()) {
            servoHip.move(-3);
        }
        if (cycle == 2 && servoHip.isAroundCenter()) {
            cycle++;
        }
        // Cycle 3: Move right foot to center.
        if (cycle == 3 && !servoStep.isAroundCenter()) {
            servoStep.move(3);
        }
        if (cycle == 3 && servoStep.isAroundCenter()) {
            cycle = 0;
        }
    }

    public void right () {
        if (action != ACTION.RIGHT) {
            // Set servos to start position.
            servoHip.set(0);
            servoStep.set(0);
            servoArmL.set(-100);
            servoArmR.set(-100);
            servoHead.set(0);
            // Set cycle to start.
            cycle = 0;
            // Set action to DANCE.
            action = ACTION.RIGHT;
            Log.d(TAG, "RIGHT initiated.");
        }
        // Cycle 0: Lift left foot.
        if (cycle == 0 && !servoHip.isMin()) {
            servoHip.move(-3);
        }
        if (cycle == 0 && servoHip.isMin()) {
            cycle++;
        }
        // Cycle 1: Move left foot forward.
        if (cycle == 1 && !servoStep.isMax()) {
            servoStep.move(3);
        }
        if (cycle == 1 && servoStep.isMax()) {
            cycle++;
        }
        // Cycle 2: Left foot down.
        if (cycle == 2 && !servoHip.isAroundCenter()) {
            servoHip.move(3);
        }
        if (cycle == 2 && servoHip.isAroundCenter()) {
            cycle++;
        }
        // Cycle 3: Move left foot to center.
        if (cycle == 3 && !servoStep.isAroundCenter()) {
            servoStep.move(-3);
        }
        if (cycle == 3 && servoStep.isAroundCenter()) {
            cycle = 0;
        }
    }
}
