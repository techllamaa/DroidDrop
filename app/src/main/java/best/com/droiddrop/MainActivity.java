package best.com.droiddrop;

import android.animation.Animator;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.nearby.connection.Strategy;

import java.util.Random;

public class MainActivity extends ConnectionsActivity {
    public static final boolean DEBUG = true;
    /** P2P_STAR is the higher bandwith connection type*/
    private static final Strategy CONNECTION_STRATEGY = Strategy.P2P_STAR;

    /**
     * TODO: Remove hardcoded id once done testing connections
     * This service id lets us find other nearby devices that are interested in the same thing.
     * Hardcoded for testing purposes.
     */
    private static final String SERVICE_ID = "ConnectionTest";

    /** A random ID used as device's name. */
    private String mName;

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
    private State appState = State.UNKNOWN;

    @ColorInt
    private int appConnected = COLORS[0];


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

        mName = generateRandomName();

        ((TextView) findViewById(R.id.name)).setText(mName);
    }


    @Override
    protected String getName() {
        return null;
    }

    @Override
    protected String getServiceId() {
        return null;
    }

    @Override
    protected Strategy getStrategy() {
        return null;
    }

    private static String generateRandomName() {
        String name = "";
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            name += random.nextInt(10);
        }
        return name;
    }

    /** UI states */
    public enum State {
        UNKNOWN,
        SEARCHING,
        CONNECTED
    }

}