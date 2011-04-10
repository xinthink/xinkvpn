package xink.vpn;

import static xink.vpn.Constants.*;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnState;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class VpnConnectorService extends Service {

    private final class VpnStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();

            if (ACT_TOGGLE_VPN_CONN.equals(action)) {
                toggleVpnState(intent);
            } else if (ACTION_VPN_CONNECTIVITY.equals(action)) {
                onStateChanged(intent);
            } else {
                Log.w(TAG, "VpnStateReceiver ignores unknown intent:" + intent);
            }
        }

    }

    private static final String TAG = VpnConnectorService.class.getName();

    private static final ComponentName THIS_APPWIDGET = new ComponentName("xink.vpn", "xink.vpn.VpnAppWidgetProvider");

    private Context context;
    private VpnStateReceiver receiver;
    private VpnState state = VpnState.IDLE;
    private VpnActor actor;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "VpnConnectorService created");
        context = getApplicationContext();
        actor = new VpnActor(context);

        updateViews();
        registerReceivers();
        actor.checkStatus();
    }

    private void registerReceivers() {
        receiver = new VpnStateReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_VPN_CONNECTIVITY);
        filter.addAction(ACT_TOGGLE_VPN_CONN);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "VpnConnectorService destroyed");

        unregisterReceivers();
        super.onDestroy();
    }

    private void unregisterReceivers() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    public void toggleVpnState(final Intent intent) {
        switch (state) {
        case IDLE:
            connect();
            break;
        case CONNECTED:
            actor.disconnect();
            break;
        default:
            Log.i(TAG, "toggleVpnState intent not handled, currentState=" + state + ", intent=" + intent);
            break;
        }
    }

    private void connect() {
        try {
            actor.connect();
        } catch (NoActiveVpnException e) {
            Toast.makeText(this, R.string.err_no_active_vpn, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "connect failed, no active vpn");
        }
    }

    public void onStateChanged(final Intent intent) {
        Log.d(TAG, "onStateChanged: " + intent);

        String profileName = intent.getStringExtra(BROADCAST_PROFILE_NAME);

        if (profileName.equals(getActvieProfileName())) {
            VpnState state = VpnActor.extractVpnState(intent);
            int err = intent.getIntExtra(BROADCAST_ERROR_CODE, VPN_ERROR_NO_ERROR);

            stateChanged(profileName, state, err);
        } else {
            Log.d(TAG, "ignores non-active profile event for " + profileName);
        }
    }

    private String getActvieProfileName() {
        VpnProfileRepository repository = VpnProfileRepository.getInstance(context);
        VpnProfile activeProfile = repository.getActiveProfile();
        return activeProfile != null ? activeProfile.getName() : null;
    }

    private void stateChanged(final String profileName, final VpnState newState, final int errCode) {
        Log.d(TAG, "stateChanged, '" + profileName + "', state: " + state + "->" + newState + ", errCode=" + errCode);
        state = newState;
        updateViews();
    }

    private void updateViews() {
        RemoteViews views = new RemoteViews(context.getPackageName(), getViewId());
        // Update specific list of appWidgetIds if given, otherwise default to all
        updateButtons(views);
        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
        gm.updateAppWidget(THIS_APPWIDGET, views);
    }

    private int getViewId() {
        int view = 0;
        switch (state) {
        case CONNECTED:
            view = R.layout.vpn_widget_connected;
            break;
        case CONNECTING:
        case DISCONNECTING:
            view = R.layout.vpn_widget_transition;
            break;
        default:
            view = R.layout.vpn_widget_disconnected;
            break;
        }

        return view;
    }

    private void updateButtons(final RemoteViews views) {
        Intent intent = new Intent(Constants.ACT_TOGGLE_VPN_CONN);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.frmToggleVpnStatue, pendingIntent);
    }
}
