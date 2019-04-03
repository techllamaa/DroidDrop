package best.com.droiddrop;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class MainActivity extends ConnectionsActivity {
    public static final boolean DEBUG = true;

    /** P2P_STAR is the higher bandwidth connection type*/
    private static final Strategy CONNECTION_STRATEGY = Strategy.P2P_STAR;

    /** Length of state change animations. */
    private static final long ANIMATION_DURATION = 600;

    /**
     * TODO: Remove hardcoded id once done testing connections
     * This service id lets us find other nearby devices that are interested in the same thing.
     * Hardcoded for testing purposes.
     */
    private static final String SERVICE_ID = "ConnectionTest";

    /** A random ID used as device's name. */
    private String deviceName;

    /** Background colors for determining whether connection was made between two devices */
    @ColorInt
    private static final int[] COLORS =
            new int[] {
                    0xFFF44336 /* red */,
                    0xFF9C27B0 /* deep purple */,
                    0xFF00BCD4 /* teal */,
                    0xFF4CAF50 /* green */,
                    0xFFFFAB00 /* amber */,
                    0xFFFF9800 /* orange */,
                    0xFF795548 /* brown */
            };

    /** State of the app */
    private State deviceState = State.UNKNOWN;

    @ColorInt
    private int deviceConnectedColor = COLORS[0];


    /** An animator that controls the animation from previous state to current state. */
    @Nullable
    private Animator mCurrentAnimator;

    /** Various state for animation transitions */
    private TextView mPreviousStateView;
    private TextView mCurrentStateView;

    private TextView mDebugLogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar()
                .setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.actionBar));

        mPreviousStateView = (TextView) findViewById(R.id.previous_state);
        mCurrentStateView = (TextView) findViewById(R.id.current_state);

        mDebugLogView = (TextView) findViewById(R.id.debug_log);
        mDebugLogView.setVisibility(DEBUG ? View.VISIBLE : View.GONE);
        mDebugLogView.setMovementMethod(new ScrollingMovementMethod());

        deviceName = generateRandodeviceName();

        ((TextView) findViewById(R.id.name)).setText(deviceName);
    }

    @Override
    protected void onStart() {
        super.onStart();

        setState(State.SEARCHING);
    }

    @Override
    protected void onStop() {
        setState(State.UNKNOWN);

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (getState() == State.CONNECTED) {
            setState(State.SEARCHING);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {
        stopDiscovering();
        connectToEndpoint(endpoint);
    }

    @Override
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        // Set the color to be the same between both devices
        deviceConnectedColor = COLORS[connectionInfo.getAuthenticationToken().hashCode() % COLORS.length];

        acceptConnection(endpoint);
    }

    @Override
    protected void onEndpointConnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_connected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
        setState(State.CONNECTED);
    }

    @Override
    protected void onEndpointDisconnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_disconnected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
        setState(State.SEARCHING);
    }

    @Override
    protected void onConnectionFailed(Endpoint endpoint) {
        if (getState() == State.SEARCHING) {
            startDiscovering();
        }
    }

    /**
     * @param state The new state.
     */
    private void setState(State state) {
        if (deviceState == state) {
            logW("State set to " + state + " but already in that state");
            return;
        }

        logD("State set to " + state);
        State oldState = deviceState;
        deviceState = state;
        onStateChanged(oldState, state);
    }

    /** @return The current state. */
    private State getState() {
        return deviceState;
    }

    /**
     *
     * @param oldState The previous state we were in. Clean up anything related to this state.
     * @param newState The new state we're now in. Prepare the UI for this state.
     */
    private void onStateChanged(State oldState, State newState) {
        if (mCurrentAnimator != null && mCurrentAnimator.isRunning()) {
            mCurrentAnimator.cancel();
        }

        // Update Nearby Connections to the new state.
        switch (newState) {
            case SEARCHING:
                disconnectFromAllEndpoints();
                startDiscovering();
                startAdvertising();
                break;
            case CONNECTED:
                stopDiscovering();
                stopAdvertising();
                break;
            case UNKNOWN:
                stopAllEndpoints();
                break;
            default:
                // no-op
                break;
        }

        // Update the UI.
        switch (oldState) {
            case UNKNOWN:
                // Unknown is our initial state. Whatever state we move to,
                // we're transitioning forwards.
                transitionForward(oldState, newState);
                break;
            case SEARCHING:
                switch (newState) {
                    case UNKNOWN:
                        transitionBackward(oldState, newState);
                        break;
                    case CONNECTED:
                        transitionForward(oldState, newState);
                        break;
                    default:
                        // no-op
                        break;
                }
                break;
            case CONNECTED:
                // Connected is our final state. Whatever new state we move to,
                // we're transitioning backwards.
                transitionBackward(oldState, newState);
                break;
        }
    }

    @UiThread
    private void transitionForward(State oldState, final State newState) {
        mPreviousStateView.setVisibility(View.VISIBLE);
        mCurrentStateView.setVisibility(View.VISIBLE);

        updateTextView(mPreviousStateView, oldState);
        updateTextView(mCurrentStateView, newState);

        if (ViewCompat.isLaidOut(mCurrentStateView)) {
            mCurrentAnimator = createAnimator(false /* reverse */);
            mCurrentAnimator.addListener(
                    new AnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            updateTextView(mCurrentStateView, newState);
                        }
                    });
            mCurrentAnimator.start();
        }
    }

    @UiThread
    private void transitionBackward(State oldState, final State newState) {
        mPreviousStateView.setVisibility(View.VISIBLE);
        mCurrentStateView.setVisibility(View.VISIBLE);

        updateTextView(mCurrentStateView, oldState);
        updateTextView(mPreviousStateView, newState);

        if (ViewCompat.isLaidOut(mCurrentStateView)) {
            mCurrentAnimator = createAnimator(true /* reverse */);
            mCurrentAnimator.addListener(
                    new AnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            updateTextView(mCurrentStateView, newState);
                        }
                    });
            mCurrentAnimator.start();
        }
    }

    @NonNull
    private Animator createAnimator(boolean reverse) {
        Animator animator;
        if (Build.VERSION.SDK_INT >= 21) {
            int cx = mCurrentStateView.getMeasuredWidth() / 2;
            int cy = mCurrentStateView.getMeasuredHeight() / 2;
            int initialRadius = 0;
            int finalRadius = Math.max(mCurrentStateView.getWidth(), mCurrentStateView.getHeight());
            if (reverse) {
                int temp = initialRadius;
                initialRadius = finalRadius;
                finalRadius = temp;
            }
            animator =
                    ViewAnimationUtils.createCircularReveal(
                            mCurrentStateView, cx, cy, initialRadius, finalRadius);
        } else {
            float initialAlpha = 0f;
            float finalAlpha = 1f;
            if (reverse) {
                float temp = initialAlpha;
                initialAlpha = finalAlpha;
                finalAlpha = temp;
            }
            mCurrentStateView.setAlpha(initialAlpha);
            animator = ObjectAnimator.ofFloat(mCurrentStateView, "alpha", finalAlpha);
        }
        animator.addListener(
                new AnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator animator) {
                        mPreviousStateView.setVisibility(View.GONE);
                        mCurrentStateView.setAlpha(1);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mPreviousStateView.setVisibility(View.GONE);
                        mCurrentStateView.setAlpha(1);
                    }
                });
        animator.setDuration(ANIMATION_DURATION);
        return animator;
    }

    /** Updates colors for UI */
    @UiThread
    private void updateTextView(TextView textView, State state) {
        switch (state) {
            case SEARCHING:
                textView.setBackgroundResource(R.color.state_searching);
                textView.setText(R.string.status_searching);
                break;
            case CONNECTED:
                textView.setBackgroundColor(deviceConnectedColor);
                textView.setText(R.string.status_connected);
                break;
            default:
                textView.setBackgroundResource(R.color.state_unknown);
                textView.setText(R.string.status_unknown);
                break;
        }
    }

    /** Starts sends data to all connected devices. */
    private void sendData(Intent selectedFile) {
        logV("sendData()");
        if (selectedFile != null) {
            Uri uri = selectedFile.getData();
            Payload filePayload = null;

            try {
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");
                filePayload = Payload.fromFile(pfd);

                // Construct a simple message mapping the ID of the file payload to the desired filename.
                String filenameMessage = filePayload.getId() + ":" + uri.getLastPathSegment();

                // Send the filename message as a bytes payload.
                Payload filenameBytesPayload =
                        Payload.fromBytes(filenameMessage.getBytes(StandardCharsets.UTF_8));
                send(filenameBytesPayload);

                // Finally, send the file payload.
                send(filePayload);
            } catch (IOException e) {
                logE("sendData() failed", e);
            }


        } else {
            logE("sendData() failed", new Throwable("Null Intent"));
        }
    }

    @Override
    protected String[] getRequiredPermissions() {
        return super.getRequiredPermissions();
    }


    @Override
    protected String getName() {
        return deviceName;
    }

    @Override
    protected String getServiceId() {
        return SERVICE_ID;
    }

    @Override
    protected Strategy getStrategy() {
        return CONNECTION_STRATEGY;
    }

    private static String generateRandodeviceName() {
        String name = "";
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            name += random.nextInt(10);
        }
        return name;
    }

    @Override
    protected void logV(String msg) {
        super.logV(msg);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_verbose)));
    }

    @Override
    protected void logD(String msg) {
        super.logD(msg);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_debug)));
    }

    @Override
    protected void logW(String msg) {
        super.logW(msg);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_warning)));
    }

    @Override
    protected void logW(String msg, Throwable e) {
        super.logW(msg, e);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_warning)));
    }

    @Override
    protected void logE(String msg, Throwable e) {
        super.logE(msg, e);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_error)));
    }

    private void appendToLogs(CharSequence msg) {
        mDebugLogView.append("\n");
        mDebugLogView.append(DateFormat.format("hh:mm", System.currentTimeMillis()) + ": ");
        mDebugLogView.append(msg);
    }

    private static CharSequence toColor(String msg, int color) {
        SpannableString spannable = new SpannableString(msg);
        spannable.setSpan(new ForegroundColorSpan(color), 0, msg.length(), 0);
        return spannable;
    }

    /** UI states */
    public enum State {
        UNKNOWN,
        SEARCHING,
        CONNECTED
    }

    /** Allows us to override methods needed for UI */
    private abstract static class AnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animator) {}

        @Override
        public void onAnimationEnd(Animator animator) {}

        @Override
        public void onAnimationCancel(Animator animator) {}

        @Override
        public void onAnimationRepeat(Animator animator) {}
    }

}