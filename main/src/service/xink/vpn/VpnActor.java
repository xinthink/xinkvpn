/*
 * Copyright 2011 yingxinwu.g@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xink.vpn;

import static xink.vpn.Constants.*;

import java.util.List;

import xink.vpn.wrapper.VpnManager;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnService;
import xink.vpn.wrapper.VpnState;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.util.Log;

public class VpnActor {
    private static final int ONE_SEC = 1000;
    private static final String TAG = "xink";

    private VpnProfileRepository repository;
    private VpnManager vpnMgr;
    private VpnService vpnSrv;
    private Context context;

    public VpnActor(final Context ctx) {
        super();
        context = ctx;
    }

    public void connect() {
        final VpnProfile p = getRepository().getActiveProfile();
        if (p == null)
            throw new NoActiveVpnException("connect failed, no active vpn");

        connect(p);
    }

    public void connect(final VpnProfile p) {
        Log.i(TAG, "connect to: " + p);

        p.preConnect();
        final VpnProfile cp = p.dulicateToConnect(); // connect using a clone, so the secret key can be replace

        getVpnMgr().startVpnService();
        ServiceConnection c = new ServiceConnection() {
            @Override
            public void onServiceConnected(final ComponentName className, final IBinder service) {
                try {
                    boolean success = getVpnSrv().connect(service, cp);

                    if (!success) {
                        Log.d(TAG, "~~~~~~ connect() failed!");
                        broadcastConnectivity(cp.getName(), VpnState.IDLE, VPN_ERROR_CONNECTION_FAILED);
                    } else {
                        Log.d(TAG, "~~~~~~ connect() succeeded!");
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "connect()", e);
                    broadcastConnectivity(cp.getName(), VpnState.IDLE, VPN_ERROR_CONNECTION_FAILED);
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
            broadcastConnectivity(cp.getName(), VpnState.IDLE, VPN_ERROR_CONNECTION_FAILED);
        }
    }

    public void disconnect() {
        Log.i(TAG, "disconnect active vpn");

        getVpnMgr().startVpnService();
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

    public void checkStatus() {
        final VpnProfile p = getRepository().getActiveProfile();
        if (p == null)
            return;

        checkStatus(p);
    }

    private void checkStatus(final VpnProfile p) {
        Log.i(TAG, "check status of vpn: " + p);

        final ConditionVariable cv = new ConditionVariable();
        cv.close();

        getVpnMgr().startVpnService();
        ServiceConnection c = new ServiceConnection() {
            @Override
            public synchronized void onServiceConnected(final ComponentName className, final IBinder service) {
                cv.open();
                try {
                    getVpnSrv().checkStatus(service, p);
                } catch (Exception e) {
                    Log.e(TAG, "checkStatus()", e);
                    broadcastConnectivity(p.getName(), VpnState.IDLE, VPN_ERROR_NO_ERROR);
                } finally {
                    context.unbindService(this);
                }
            }

            @Override
            public void onServiceDisconnected(final ComponentName className) {
                cv.open();
                broadcastConnectivity(p.getName(), VpnState.IDLE, VPN_ERROR_NO_ERROR);
                context.unbindService(this);
            }
        };

        boolean ret = getVpnMgr().bindVpnService(c);
        if (ret && !cv.block(ONE_SEC)) { // if binding failed, wait for a second, let status propagate
            broadcastConnectivity(p.getName(), VpnState.IDLE, VPN_ERROR_NO_ERROR);
        }
    }

    public void checkAllStatus() {
        VpnProfileRepository repo = getRepository();

        synchronized (repo) {
            List<VpnProfile> profiles = repo.getAllVpnProfiles();
            for (VpnProfile p : profiles) {
                checkStatus(p);
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

    public void activate(final VpnProfile p) {
        getRepository().setActiveProfile(p);
        broadcastConnectivity(p.getName(), p.getState(), VPN_ERROR_NO_ERROR);
    }

    public void broadcastConnectivity(final String profileName, final VpnState s, final int error) {
        Intent intent = new Intent(ACTION_VPN_CONNECTIVITY);
        intent.putExtra(BROADCAST_PROFILE_NAME, profileName);
        intent.putExtra(BROADCAST_CONNECTION_STATE, s);
        if (error != VPN_ERROR_NO_ERROR) {
            intent.putExtra(BROADCAST_ERROR_CODE, error);
        }
        context.sendBroadcast(intent);
    }
}
