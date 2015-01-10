package com.muehlbauer.myrobi;

import java.util.Locale;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.DigitalOutput.Spec.Mode;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;


/**
 * This is the main activity of the Robot application.
 */
public class MainActivity extends IOIOActivity {

    private Button mWalkButton, mBackButton, mLeftButton, mRightButton, mDanceButton;

    private Robot.ACTION action = Robot.ACTION.NONE;

    private SeekBar servo01SeekBar;
    private SeekBar servo02SeekBar;
    private SeekBar servo03SeekBar;

    private TextToSpeech ttobj;
    private SpreadsheetDataFeed sheet;
    private static final String TAG = "MainActivity";

    /**
     * Called when the activity is first created. Here we normally initialize
     * our GUI.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        servo01SeekBar = (SeekBar) findViewById(R.id.seekBar1);
        servo02SeekBar = (SeekBar) findViewById(R.id.seekBar2);
        servo03SeekBar = (SeekBar) findViewById(R.id.seekBar3);
        mWalkButton    = (Button) findViewById(R.id.walkButton);
        mWalkButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    action = Robot.ACTION.WALK;
                    speak("Schaut mal, ich laufe.");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    action = Robot.ACTION.HOME;
                    speak("Das war anstrengend.");
                }
                return true;
            }
        });
        mLeftButton    = (Button) findViewById(R.id.leftButton);
        mLeftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    action = Robot.ACTION.LEFT;
                    speak("Ab nach Links.");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    action = Robot.ACTION.HOME;
                    speak("Krass.");
                }
                return true;
            }
        });
        mRightButton    = (Button) findViewById(R.id.rightButton);
        mRightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    action = Robot.ACTION.RIGHT;
                    speak("Ich muss nach Rechts.");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    action = Robot.ACTION.HOME;
                    speak("Fertig.");
                }
                return true;
            }
        });
        mBackButton    = (Button) findViewById(R.id.backButton);
        mBackButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    action = Robot.ACTION.BACK;
                    speak("Schaut mal, ich laufe rückwärts.");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    action = Robot.ACTION.HOME;
                    speak("Mann bin ich cool.");
                }
                return true;
            }
        });
        mDanceButton = (Button) findViewById(R.id.danceButton);
        mDanceButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    action = Robot.ACTION.DANCE;
                    speak("Hurra Musik!");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    action = Robot.ACTION.HOME;
                    speak("Das rockt.");
                }
                return true;
            }
        });
        ttobj=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            Locale loc = new Locale("de");
                            ttobj.setLanguage(loc);
                            Log.d(TAG, "TTS completed.");
                        }
                    }
                });
        //sheet = new SpreadsheetDataFeed();
        //String urlString = "https://spreadsheets.google.com/feeds/cells/1LiCXJbSn3bmChyGjctZd0WBK_UkoWBkAU0YapWImlks/1/public/basic?alt=json-in-script&callback=JSON_CALLBACK";
        //sheet.execute(urlString);
        Log.d(TAG, "Create completed.");
    }

    private void speak(String text) {
        ttobj.setPitch((float) 0.3);
        ttobj.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected IOIOLooper createIOIOLooper() {
        return new Looper();
    }

    /**
     * This is the thread on which all the IOIO activity happens. It will be run
     * every time the application is resumed and aborted when it is paused. The
     * method setup() will be called right after a connection with the IOIO has
     * been established (which might happen several times!). Then, loop() will
     * be called repetitively until the IOIO gets disconnected.
     */
    class Looper extends BaseIOIOLooper {

        private final int SERVO01_PIN       = 1;
        private final int SERVO02_PIN       = 2;
        private final int SERVO03_PIN       = 3;
        private final int SERVO04_PIN       = 4;
        private final int SERVO05_PIN       = 10;
        private final int LED1_PIN          = 34;
        private final int LED2_PIN          = 35;
        private final int PWM_FREQ          = 100;

        private PwmOutput servoHipOutput;
        private PwmOutput servoStepOutput;
        private PwmOutput servoArmLOutput;
        private PwmOutput servoArmROutput;
        private PwmOutput servoHeadOutput;
        private DigitalOutput mLed1;
        private DigitalOutput mLed2;

        private int servoArmL;
        private int servoArmR;
        private int servoHead;

        /** The on-board LED. */
        private DigitalOutput led_;

        private Robot robot;

        /**
         * Called every time a connection with IOIO has been established.
         * Typically used to open pins.
         *
         * @throws ConnectionLostException
         *             When IOIO connection is lost.
         */
        @Override
        protected void setup() throws ConnectionLostException {
            servoHipOutput  = ioio_.openPwmOutput(new DigitalOutput.Spec(SERVO01_PIN, Mode.OPEN_DRAIN), PWM_FREQ);
            servoStepOutput = ioio_.openPwmOutput(new DigitalOutput.Spec(SERVO02_PIN, Mode.OPEN_DRAIN), PWM_FREQ);
            servoArmLOutput = ioio_.openPwmOutput(new DigitalOutput.Spec(SERVO03_PIN, Mode.OPEN_DRAIN), PWM_FREQ);
            servoArmROutput = ioio_.openPwmOutput(new DigitalOutput.Spec(SERVO04_PIN, Mode.OPEN_DRAIN), PWM_FREQ);
            servoHeadOutput = ioio_.openPwmOutput(new DigitalOutput.Spec(SERVO05_PIN, Mode.OPEN_DRAIN), PWM_FREQ);
            led_            = ioio_.openDigitalOutput(0, true);
            mLed1           = ioio_.openDigitalOutput(LED1_PIN, false);
            mLed2           = ioio_.openDigitalOutput(LED2_PIN, false);
            robot = new Robot(servoHipOutput, servoStepOutput, servoArmLOutput, servoArmROutput,servoHeadOutput);
            robot.homePosition();
            speak("Hallo, ich bin Robi.");
            Log.d(TAG, "Setup completed.");
        }

        @Override
        public void disconnected() {
            speak("Gute Nacht.");
        }

        @Override
        public void loop() throws ConnectionLostException {
            try {
                //Signalize connection to IOIO board.
                led_.write(false);
                mLed1.write(true);
                mLed2.write(true);

                // Manual moves for arms and head.
                // Only possible if not pre-configured move is processing.
                if (robot.getAction() == Robot.ACTION.NONE) {
                    if (servoArmL != (1000 + (1000 - servo01SeekBar.getProgress()))) {
                        servoArmL = (1000 + (1000 - servo01SeekBar.getProgress()));
                        servoArmLOutput.setPulseWidth(servoArmL);
                        Log.d(TAG, "Servo: ArmL; PulseWidth: " + servoArmL);
                    }
                    if (servoArmR != (1000 + (servo02SeekBar.getProgress()))) {
                        servoArmR = (1000 + (servo02SeekBar.getProgress()));
                        servoArmROutput.setPulseWidth(servoArmR);
                        Log.d(TAG, "Servo: ArmR; PulseWidth: " + servoArmR);
                    }
                    if (servoHead != (1000 + (1000 - servo03SeekBar.getProgress()))) {
                        servoHead = (1000 + (1000 - servo03SeekBar.getProgress()));
                        servoHeadOutput.setPulseWidth(servoHead);
                        Log.d(TAG, "Servo: Head; PulseWidth: " + servoHead);
                    }
                }

                // Pre-configured robot moves.
                switch (action) {
                    case WALK:
                        robot.walk();
                        break;
                    case LEFT:
                        robot.left();
                        break;
                    case RIGHT:
                        robot.right();
                        break;
                    case BACK:
                        robot.back();
                        break;
                    case DANCE:
                        robot.dance();
                        break;
                    case HOME:
                        robot.home();
                        break;
                }

                Thread.sleep(10);
            } catch (InterruptedException e) {
                Log.d(TAG, "InterruptedException: " + e.toString());
                ioio_.disconnect();
            } catch (ConnectionLostException e) {
                Log.d(TAG, "ConnectionLostException: " + e.toString());
                throw e;
            }
        }
    }
}