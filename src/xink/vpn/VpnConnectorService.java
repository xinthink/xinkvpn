package xink.vpn;

import static xink.vpn.Constants.*;

import java.io.Serializable;

import xink.vpn.wrapper.VpnManager;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnService;
import xink.vpn.wrapper.VpnState;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

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

    private static final String TAG = "xink";

    private static final ComponentName THIS_APPWIDGET = new ComponentName("xink.vpn", "xink.vpn.VpnAppWidgetProvider");

    private VpnProfileRepository repository;
    private VpnManager vpnMgr;
    private VpnService vpnSrv;
    private Context context;
    private VpnStateReceiver receiver;
    private VpnState state = VpnState.IDLE;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "VpnConnectorService created");
        context = getApplicationContext();
        registerReceivers();
        checkStatus();
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
            disconnect();
            break;
        default:
            Log.i(TAG, "toggleVpnState intent not handled, currentState=" + state + ", intent=" + intent);
            break;
        }
    }

    private void connect() {
        final VpnProfile p = getRepository().getActiveProfile();
        if (p == null) {
            return;
        }

        Log.i(TAG, "connect active vpn: " + p);

        getVpnMgr().startVpnService();

        ServiceConnection c = new ServiceConnection() {
            @Override
            public void onServiceConnected(final ComponentName className, final IBinder service) {
                try {
                    boolean success = getVpnSrv().connect(service, p);

                    if (!success) {
                        Log.d(TAG, "~~~~~~ connect() failed!");
                    } else {
                        Log.d(TAG, "~~~~~~ connect() succeeded!");
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "connect()", e);
                    stateChanged(p.getName(), VpnState.IDLE, VPN_ERROR_CONNECTION_FAILED);
                } finally {
                    context.unbindService(this);
                }
            }

            @Override
            public void onServiceDisconnected(final ComponentName className) {
                Log.e(TAG, "onServiceDisconnected");
                checkStatus();
            }
        };

        if (!getVpnMgr().bindVpnService(c)) {
            Log.e(TAG, "bind service failed");
            stateChanged(p.getName(), VpnState.IDLE, VPN_ERROR_CONNECTION_FAILED);
        }
    }

    private void disconnect() {
        Log.i(TAG, "disconnect active vpn");

        ServiceConnection c = new ServiceConnection() {
            @Override
            public void onServiceConnected(final ComponentName className, final IBinder service) {
                try {
                    getVpnSrv().disconnect(service);
                } catch (Exception e) {
                    Log.e(TAG, "disconnect()", e);
                    checkStatus();
                } finally {
                    context.unbindService(this);
                }
            }

            @Override
            public void onServiceDisconnected(final ComponentName className) {
                Log.e(TAG, "onServiceDisconnected");
                checkStatus();
            }
        };
        if (!getVpnMgr().bindVpnService(c)) {
            Log.e(TAG, "bind service failed");
            checkStatus();
        }
    }

    private void checkStatus() {
        final VpnProfile p = getRepository().getActiveProfile();
        if (p == null) {
            return;
        }

        Log.i(TAG, "check status of vpn: " + p);

        final ConditionVariable cv = new ConditionVariable();
        cv.close();

        ServiceConnection c = new ServiceConnection() {
            @Override
            public synchronized void onServiceConnected(final ComponentName className, final IBinder service) {
                cv.open();
                try {
                    getVpnSrv().checkStatus(service, p);
                } catch (Exception e) {
                    Log.e(TAG, "checkStatus()", e);
                    stateChanged(p.getName(), VpnState.IDLE, VPN_ERROR_NO_ERROR);
                } finally {
                    context.unbindService(this);
                }
            }

            @Override
            public void onServiceDisconnected(final ComponentName className) {
                cv.open();
                stateChanged(p.getName(), VpnState.IDLE, VPN_ERROR_NO_ERROR);
                context.unbindService(this);
            }
        };
        if (getVpnMgr().bindVpnService(c)) {
            // wait for a second, let status propagate
            if (!cv.block(1000)) {
                stateChanged(p.getName(), VpnState.IDLE, VPN_ERROR_NO_ERROR);
            }
        }
    }

    private VpnProfileRepository getRepository() {
        if (repository == null) {
            repository = VpnProfileRepository.getInstance(context);
        }

        return repository;
    }

    private VpnManager getVpnMgr() {
        if (vpnMgr == null) {
            vpnMgr = new VpnManager(context);
        }
        return vpnMgr;
    }

    private VpnService getVpnSrv() {

        if (vpnSrv == null) {
            vpnSrv = new VpnService(context);
        }
        return vpnSrv;
    }

    public void onStateChanged(final Intent intent) {
        Log.i(TAG, "stateChanged: " + intent);

        String profileName = intent.getStringExtra(BROADCAST_PROFILE_NAME);
        VpnState state = getVpnState(intent);
        int err = intent.getIntExtra(BROADCAST_ERROR_CODE, VPN_ERROR_NO_ERROR);

        stateChanged(profileName, state, err);
    }

    private VpnState getVpnState(final Intent intent) {
        Serializable obj = intent.getSerializableExtra(BROADCAST_CONNECTION_STATE);
        VpnState state = VpnState.IDLE;

        if (obj != null) {
            state = VpnState.valueOf(obj.toString());
        }
        return state;
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
        views.setOnClickPendingIntent(R.id.btnToggleVpnStatus, pendingIntent);
    }
}
