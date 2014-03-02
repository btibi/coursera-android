package course.labs.GraphicsLab;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.RelativeLayout;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BubbleActivity extends Activity {

    // These variables are for testing purposes, do not modify
    private final static int RANDOM = 0;
    private final static int SINGLE = 1;
    private final static int STILL = 2;
    private static int speedMode = RANDOM;

    private static final int MENU_STILL = Menu.FIRST;
    private static final int MENU_SINGLE_SPEED = Menu.FIRST + 1;
    private static final int MENU_RANDOM_SPEED = Menu.FIRST + 2;

    private static final String TAG = "Lab-Graphics";

    // Main view
    private RelativeLayout mFrame;

    // Bubble image
    private Bitmap mBitmap;

    // Display dimensions
    private int mDisplayWidth, mDisplayHeight;

    // Sound variables

    // AudioManager
    private AudioManager mAudioManager;
    // SoundPool
    private SoundPool mSoundPool;
    // ID for the bubble popping sound
    private int mSoundID;
    // Audio volume
    private float mStreamVolume;
    private int maxScreenSizeX;
    private int maxScreenSizeY;


    // Gesture Detector
    private GestureDetector mGestureDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        saveScreenSize();

        setupGestureDetector();

        setContentView(R.layout.main);

        // Set up user interface
        mFrame = (RelativeLayout) findViewById(R.id.frame);

        // Load basic bubble Bitmap
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.b64);

    }

    private void saveScreenSize() {
        Point size = new Point();
        Display display = getWindowManager().getDefaultDisplay();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)    {
            display.getSize(size);
            maxScreenSizeX = size.x;
            maxScreenSizeY = size.y;
        }else{
            maxScreenSizeX = display.getWidth();
            maxScreenSizeY = display.getHeight();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Manage bubble popping sound
        // Use AudioManager.STREAM_MUSIC as stream type

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        mStreamVolume = (float) mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC)
                / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        // TODO - make a new SoundPool, allowing up to 10 streams
        mSoundPool = null;

        // TODO - set a SoundPool OnLoadCompletedListener that calls setupGestureDetector()


        // TODO - load the sound from res/raw/bubble_pop.wav
        mSoundID = 0;

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {

            // Get the size of the display so this view knows where borders are
            mDisplayWidth = mFrame.getWidth();
            mDisplayHeight = mFrame.getHeight();

        }
    }

    // Set up GestureDetector
    private void setupGestureDetector() {

        mGestureDetector = new GestureDetector(this,

                new GestureDetector.SimpleOnGestureListener() {

                    // If a fling gesture starts on a BubbleView then change the
                    // BubbleView's velocity

                    @Override
                    public boolean onFling(MotionEvent event1, MotionEvent event2,
                                           float velocityX, float velocityY) {

                        // TODO - Implement onFling actions.
                        // You can get all Views in mFrame using the
                        // ViewGroup.getChildCount() method


                        return false;

                    }

                    // If a single tap intersects a BubbleView, then pop the BubbleView
                    // Otherwise, create a new BubbleView at the tap's location and add
                    // it to mFrame. You can get all views from mFrame with ViewGroup.getChildAt()

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent event) {

                        // You can get all Views in mFrame using the
                        // ViewGroup.getChildCount() method
                        boolean newBubble = true;
                        for (int i = 0; i < mFrame.getChildCount(); i++) {
                            BubbleView bubbleView = (BubbleView) mFrame.getChildAt(i);
                            if (bubbleView.intersects(event.getX(), event.getY())) {
                                bubbleView.stop(true);
                                newBubble = false;
                                break;
                            }
                        }

                        if (newBubble) {
                            BubbleView bubbleView = new BubbleView(getApplicationContext(), event.getX(), event.getY());
                            mFrame.addView(bubbleView);
                            bubbleView.start();
                        }
                        return true;
                    }
                });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onPause() {

        // TODO - Release all SoundPool resources


        super.onPause();
    }

    // BubbleView is a View that displays a bubble.
    // This class handles animating, drawing, popping amongst other actions.
    // A new BubbleView is created for each bubble on the display

    private class BubbleView extends View {

        private static final int BITMAP_SIZE = 64;
        private static final int REFRESH_RATE = 40;
        private final Paint mPainter = new Paint();
        private ScheduledFuture<?> mMoverFuture;
        private int mScaledBitmapWidth;
        private Bitmap mScaledBitmap;

        // location, speed and direction of the bubble
        private float mXPos, mYPos, mDx, mDy;
        private long mRotate, mDRotate;

        public BubbleView(Context context, float x, float y) {
            super(context);
            log("Creating Bubble at: x:" + x + " y:" + y);

            // Create a new random number generator to
            // randomize size, rotation, speed and direction
            Random r = new Random();

            // Creates the bubble bitmap for this BubbleView
            createScaledBitmap(r);

            // Adjust position to center the bubble under user's finger
            mXPos = x - mScaledBitmapWidth / 2;
            mYPos = y - mScaledBitmapWidth / 2;

            // Set the BubbleView's speed and direction
            setSpeedAndDirection(r);

            // Set the BubbleView's rotation
            setRotation(r);

            mPainter.setAntiAlias(true);
        }

        private void setRotation(Random r) {
            if (speedMode == RANDOM) {
                mDRotate = r.nextInt(3) + 1;
            } else {
                mDRotate = 0;
            }
        }

        private void setSpeedAndDirection(Random r) {

            // Used by test cases
            switch (speedMode) {

                case SINGLE:

                    // Fixed speed
                    mDx = 10;
                    mDy = 10;
                    break;

                case STILL:

                    // No speed
                    mDx = 0;
                    mDy = 0;
                    break;

                default:
                    // Limit movement speed in the x and y
                    // direction to [-3..3].
                    mDx = r.nextInt(7) - 3;
                    mDy = r.nextInt(7) - 3;
                    break;
            }
        }

        private void createScaledBitmap(Random r) {

            if (speedMode != RANDOM) {

                mScaledBitmapWidth = BITMAP_SIZE * 3;

            } else {
                mScaledBitmapWidth = (r.nextInt(3) + 1) * BITMAP_SIZE;
            }

            mScaledBitmap = Bitmap.createScaledBitmap(mBitmap, mScaledBitmapWidth, mScaledBitmapWidth, false);
        }

        // Start moving the BubbleView & updating the display
        private void start() {

            // Creates a WorkerThread
            ScheduledExecutorService executor = Executors
                    .newScheduledThreadPool(1);

            // Execute the run() in Worker Thread every REFRESH_RATE
            // milliseconds
            // Save reference to this job in mMoverFuture
            mMoverFuture = executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    // implement movement logic.
                    // Each time this method is run the BubbleView should
                    // move one step. If the BubbleView exits the display,
                    // stop the BubbleView's Worker Thread.
                    // Otherwise, request that the BubbleView be redrawn.
                    if (moveWhileOnScreen()) {
                        BubbleView.this.postInvalidate();
                    } else {
                        BubbleView.this.stop(false);
                    }
                }
            }, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);
        }

        private synchronized boolean intersects(float x, float y) {
            // Return true if the BubbleView intersects position (x,y)
            return mXPos < x && x < mXPos + mScaledBitmap.getWidth()
                    && mYPos < y && y < mYPos + mScaledBitmap.getHeight();
        }

        // Cancel the Bubble's movement
        // Remove Bubble from mFrame
        // Play pop sound if the BubbleView was popped

        private void stop(final boolean popped) {

            final BubbleView actual = this;
            if (null != mMoverFuture && mMoverFuture.cancel(true)) {

                // This work will be performed on the UI Thread

                mFrame.post(new Runnable() {
                    @Override
                    public void run() {

                        // Remove the BubbleView from mFrame
                        mFrame.removeView(actual);

                        if (popped) {
                            log("Pop!");

                            // TODO - If the bubble was popped by user,
                            // play the popping sound


                        }

                        log("Bubble removed from view!");

                    }
                });
            }
        }

        // Change the Bubble's speed and direction
        private synchronized void deflect(float velocityX, float velocityY) {
            log("velocity X:" + velocityX + " velocity Y:" + velocityY);

            //TODO - set mDx and mDy to be the new velocities divided by the REFRESH_RATE

            mDx = 0;
            mDy = 0;

        }

        // Draw the Bubble at its current location
        @Override
        protected synchronized void onDraw(Canvas canvas) {
            // save the canvas
            canvas.save();

            // increase the rotation of the original image by mDRotate
            mRotate += mDRotate;

            // Rotate the canvas by current rotation
            canvas.rotate(mRotate, mXPos + mScaledBitmapWidth / 2, mYPos + mScaledBitmapWidth / 2);

            // draw the bitmap at it's new location
            canvas.drawBitmap(mScaledBitmap, mXPos, mYPos, mPainter);

            // restore the canvas
            canvas.restore();
        }


        private synchronized boolean moveWhileOnScreen() {
            // Move the BubbleView
            mXPos += mDx;
            mYPos += mDy;
            // Returns true if the BubbleView has exited the screen
            return !isOutOfView();
        }

        private boolean isOutOfView() {
            // Return true if the BubbleView has exited the screen
            return mXPos < 0 || mYPos < 0
                    || mXPos + mScaledBitmap.getWidth() > maxScreenSizeX || mYPos + mScaledBitmap.getHeight() > maxScreenSizeY;

        }
    }

    // Do not modify below here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(Menu.NONE, MENU_STILL, Menu.NONE, "Still Mode");
        menu.add(Menu.NONE, MENU_SINGLE_SPEED, Menu.NONE, "Single Speed Mode");
        menu.add(Menu.NONE, MENU_RANDOM_SPEED, Menu.NONE, "Random Speed Mode");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_STILL:
                speedMode = STILL;
                return true;
            case MENU_SINGLE_SPEED:
                speedMode = SINGLE;
                return true;
            case MENU_RANDOM_SPEED:
                speedMode = RANDOM;
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static void log(String message) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, message);
    }
}