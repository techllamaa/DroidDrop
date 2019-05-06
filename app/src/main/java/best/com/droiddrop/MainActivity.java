package best.com.droiddrop;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.util.SimpleArrayMap;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends ConnectionsActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private FloatingActionButton fab;

    private Intent myFileIntent;
    private ListView devices;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter listAdapter;
    private ArrayList<Endpoint> endpointArrayList = new ArrayList<>();

    private ListView files;
    private ArrayList<String> filesArrayList = new ArrayList<>();
    private ArrayAdapter filesListAdapter;

    private ListView recieve;
    private ArrayList<String> rArrayList = new ArrayList<>();
    private ArrayAdapter rListAdapter;


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

    /** Various maps for handling sending and recieving data**/
    private final SimpleArrayMap<Long, Payload> incomingFilePayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, Payload> completedFilePayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, String> filePayloadFilenames = new SimpleArrayMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText("Near By");
        tabLayout.getTabAt(1).setText("Shared");
        tabLayout.getTabAt(2).setText("Received");
//
//        myGridView = findViewById(R.id.myGridview);
//        FileAdapter fileAdapter = new FileAdapter(this);
//        myGridView.setAdapter(fileAdapter);

        fab = findViewById(R.id.fab);
        //final TextView txt = findViewById(R.id.fab_txt);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //txt.setVisibility(View.VISIBLE);
                //testingText= findViewById(R.id.pathTxt);
                myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                myFileIntent.setType("*/*");
                startActivityForResult(myFileIntent,10);
//                Intent intent = new Intent(MainActivity.this, NewMessageActivity.class);
//                startActivity(intent);

                deviceName = generateRandodeviceName();

            }
        });
        tabActivity();
    }

    private void tabActivity(){
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if(tab.getPosition()==0){
                    //do near by connecting here
                    arrayList.clear();
                    endpointArrayList.clear();
                    setState(State.SEARCHING);
                    Toast.makeText(getApplicationContext(), "Looking for other devices", Toast.LENGTH_SHORT).show();

                    devices = findViewById(R.id.nearByList);
                    listAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,arrayList);
                    devices.setAdapter(listAdapter);

                    devices.setOnItemClickListener((parent, view, position, id) -> {
                        String thing = (String) devices.getAdapter().getItem(position);
                        Toast.makeText(getApplicationContext(), "Attempting Connection: " + thing+ " at position "+ position, Toast.LENGTH_SHORT).show();


                        connectToEndpoint(endpointArrayList.get(position));
                    });

                }else if(tab.getPosition()==1){
                    setState(State.UNKNOWN);
                    disconnectFromAllEndpoints();
                    files = findViewById(R.id.sharedList);
                    filesListAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,filesArrayList);
                    files.setAdapter(filesListAdapter);



                    //do add file here
                }else{
                    setState(State.UNKNOWN);
                    disconnectFromAllEndpoints();
                    recieve = findViewById(R.id.recieveList);
                    rListAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,rArrayList);
                    recieve.setAdapter(rListAdapter);
                    //tab 3
                }

            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.add(new NearByTab(), "Near By");
        adapter.add(new SharedTab(), "Shared");
        adapter.add(new ReceivedTab(), "Received");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 10:
                if(requestCode != RESULT_OK){
                    sendData(data);
                    String path = data.getData().getPath();
//                    testingText= (TextView)findViewById(R.id.pathTxt);
//                    testingText.setText(path);
                }
                break;
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
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
        listAdapter.add(endpoint.getName());
        endpointArrayList.add(endpoint);
    }

    @Override
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        new AlertDialog.Builder(this)
                .setTitle("Accept connection to " + connectionInfo.getEndpointName())
                .setMessage("Confirm the code matches on both devices: " + connectionInfo.getAuthenticationToken())
                .setPositiveButton(
                        "Accept",
                        (DialogInterface dialog, int which) ->
                                // The user confirmed, so we can accept the connection.
                                acceptConnection(endpoint))
                .setNegativeButton(
                        android.R.string.cancel,
                        (DialogInterface dialog, int which) ->
                                // The user canceled, so we should reject the connection.
                                rejectConnection(endpoint))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        // Set the color to be the same between both devices
        deviceConnectedColor = COLORS[connectionInfo.getAuthenticationToken().hashCode() % COLORS.length];
    }

    @Override
    protected void onEndpointConnected(Endpoint endpoint) {
        Toast.makeText(this, "Connected to: "+endpoint.getName(), Toast.LENGTH_SHORT).show();
        toolbar.setBackgroundColor(Color.parseColor("#FF128820"));
        setState(State.CONNECTED);

    }

    @Override
    protected void onEndpointDisconnected(Endpoint endpoint) {
        toolbar.setBackgroundColor(Color.parseColor("#FF125688"));
        setState(State.SEARCHING);
    }

    @Override
    protected void disconnectFromAllEndpoints() {
        toolbar.setBackgroundColor(Color.parseColor("#FF125688"));
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
                disconnectFromAllEndpoints();
                break;
            default:
                // no-op
                break;
        }

//        // Update the UI.
//        switch (oldState) {
//            case UNKNOWN:
//                // Unknown is our initial state. Whatever state we move to,
//                // we're transitioning forwards.
//                transitionForward(oldState, newState);
//                break;
//            case SEARCHING:
//                switch (newState) {
//                    case UNKNOWN:
//                        transitionBackward(oldState, newState);
//                        break;
//                    case CONNECTED:
//                        transitionForward(oldState, newState);
//                        break;
//                    default:
//                        // no-op
//                        break;
//                }
//                break;
//            case CONNECTED:
//                // Connected is our final state. Whatever new state we move to,
//                // we're transitioning backwards.
//                transitionBackward(oldState, newState);
//                break;
//        }
    }

//    @UiThread
//    private void transitionForward(State oldState, final State newState) {
//        mPreviousStateView.setVisibility(View.VISIBLE);
//        mCurrentStateView.setVisibility(View.VISIBLE);
//
//        updateTextView(mPreviousStateView, oldState);
//        updateTextView(mCurrentStateView, newState);
//
//        if (ViewCompat.isLaidOut(mCurrentStateView)) {
//            mCurrentAnimator = createAnimator(false /* reverse */);
//            mCurrentAnimator.addListener(
//                    new AnimatorListener() {
//                        @Override
//                        public void onAnimationEnd(Animator animator) {
//                            updateTextView(mCurrentStateView, newState);
//                        }
//                    });
//            mCurrentAnimator.start();
//        }
//    }

//    @UiThread
//    private void transitionBackward(State oldState, final State newState) {
//        mPreviousStateView.setVisibility(View.VISIBLE);
//        mCurrentStateView.setVisibility(View.VISIBLE);
//
//        updateTextView(mCurrentStateView, oldState);
//        updateTextView(mPreviousStateView, newState);
//
//        if (ViewCompat.isLaidOut(mCurrentStateView)) {
//            mCurrentAnimator = createAnimator(true /* reverse */);
//            mCurrentAnimator.addListener(
//                    new AnimatorListener() {
//                        @Override
//                        public void onAnimationEnd(Animator animator) {
//                            updateTextView(mCurrentStateView, newState);
//                        }
//                    });
//            mCurrentAnimator.start();
//        }
//    }

//    @NonNull
//    private Animator createAnimator(boolean reverse) {
//        Animator animator;
//        if (Build.VERSION.SDK_INT >= 21) {
//            int cx = mCurrentStateView.getMeasuredWidth() / 2;
//            int cy = mCurrentStateView.getMeasuredHeight() / 2;
//            int initialRadius = 0;
//            int finalRadius = Math.max(mCurrentStateView.getWidth(), mCurrentStateView.getHeight());
//            if (reverse) {
//                int temp = initialRadius;
//                initialRadius = finalRadius;
//                finalRadius = temp;
//            }
//            animator =
//                    ViewAnimationUtils.createCircularReveal(
//                            mCurrentStateView, cx, cy, initialRadius, finalRadius);
//        } else {
//            float initialAlpha = 0f;
//            float finalAlpha = 1f;
//            if (reverse) {
//                float temp = initialAlpha;
//                initialAlpha = finalAlpha;
//                finalAlpha = temp;
//            }
//            mCurrentStateView.setAlpha(initialAlpha);
//            animator = ObjectAnimator.ofFloat(mCurrentStateView, "alpha", finalAlpha);
//        }
//        animator.addListener(
//                new AnimatorListener() {
//                    @Override
//                    public void onAnimationCancel(Animator animator) {
//                        mPreviousStateView.setVisibility(View.GONE);
//                        mCurrentStateView.setAlpha(1);
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animator) {
//                        mPreviousStateView.setVisibility(View.GONE);
//                        mCurrentStateView.setAlpha(1);
//                    }
//                });
//        animator.setDuration(ANIMATION_DURATION);
//        return animator;
//    }
//
//    /** Updates colors for UI */
//    @UiThread
//    private void updateTextView(TextView textView, State state) {
//        switch (state) {
//            case SEARCHING:
//
//                break;
//            case CONNECTED:
//                textView.setBackgroundColor(deviceConnectedColor);
//                break;
//            default:
//
//                break;
//        }
//    }

    /** Starts sends data to all connected devices. */
    private void sendData(Intent selectedFile) {
        filesListAdapter.add(selectedFile.toString());
        logV("sendData()");
        if (selectedFile != null) {
            Uri uri = selectedFile.getData();
            Payload filePayload = null;

            ParcelFileDescriptor pfd = null;
            try {
                pfd = getContentResolver().openFileDescriptor(uri, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            filePayload = Payload.fromFile(pfd);

            // Construct a simple message mapping the ID of the file payload to the desired filename.
            String filenameMessage = filePayload.getId() + ":" + uri.getLastPathSegment();

            // Send the filename message as a bytes payload.
            Payload filenameBytesPayload =
                    Payload.fromBytes(filenameMessage.getBytes(StandardCharsets.UTF_8));
            send(filenameBytesPayload);

            // Finally, send the file payload.
            send(filePayload);


        } else {
            logE("sendData() failed", new Throwable("Null Intent"));
        }
    }

    /** {@see ConnectionsActivity#onReceive(Endpoint, Payload)} */
    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) {
        if (payload.getType() == Payload.Type.BYTES) {
            String payloadFilenameMessage = new String(payload.asBytes(), StandardCharsets.UTF_8);
            long payloadId = addPayloadFilename(payloadFilenameMessage);
            processFilePayload(payloadId);
            rListAdapter.add(filePayloadFilenames.get(payloadId));
        } else if (payload.getType() == Payload.Type.FILE) {
            // Add this to our tracking map, so that we can retrieve the payload later.
            incomingFilePayloads.put(payload.getId(), payload);
        }
    }

    /**
     * Extracts the payloadId and filename from the message and stores it in the
     * filePayloadFilenames map. The format is payloadId:filename.
     */
    private long addPayloadFilename(String payloadFilenameMessage) {
        String[] parts = payloadFilenameMessage.split(":");
        long payloadId = Long.parseLong(parts[0]);
        String filename = parts[1];
        filePayloadFilenames.put(payloadId, filename);
        return payloadId;
    }


    private void processFilePayload(long payloadId) {
        // BYTES and FILE could be received in any order, so we call when either the BYTES or the FILE
        // payload is completely received. The file payload is considered complete only when both have
        // been received.
        Payload filePayload = completedFilePayloads.get(payloadId);
        String filename = filePayloadFilenames.get(payloadId);
        if (filePayload != null && filename != null) {
            completedFilePayloads.remove(payloadId);
            filePayloadFilenames.remove(payloadId);

            // Get the received file (which will be in the Downloads folder)
            File payloadFile = new File(filePayload.asFile().asJavaFile(),filename);

            // Rename the file.
            payloadFile.renameTo(new File(payloadFile.getParentFile(), filename));

        }
    }

    @Override
    public void onUpdate(Endpoint endpoint, PayloadTransferUpdate update) {
        if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
            long payloadId = update.getPayloadId();
            Payload payload = incomingFilePayloads.remove(payloadId);
            completedFilePayloads.put(payloadId, payload);
            if (payload != null && payload.getType() == Payload.Type.FILE) {
                processFilePayload(payloadId);
            }
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
