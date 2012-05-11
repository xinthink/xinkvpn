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

import java.text.SimpleDateFormat;
import java.util.Date;

import xink.vpn.editor.EditAction;
import xink.vpn.editor.VpnProfileEditor;
import xink.vpn.wrapper.KeyStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class VpnSettings extends Activity {

    private static final String TAG = "xink"; //$NON-NLS-1$

    private VpnProfileRepository repository;
    private ListView vpnListView;
    private VpnListAdapter vpnListAdapter;
    private ActionMode vpnActMode;
    private VpnActor actor;
    private BroadcastReceiver stateBroadcastReceiver;
    private KeyStore keyStore;
    private Runnable resumeAction;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = VpnProfileRepository.i();
        actor = new VpnActor(getApplicationContext());
        keyStore = new KeyStore(getApplicationContext());

        setTitle(R.string.selectVpn);
        setContentView(R.layout.vpn_list);

        initVpnList();
    }

    private void initVpnList() {
        vpnListView = (ListView) findViewById(R.id.listVpns);
    
        vpnListAdapter = new VpnListAdapter(this);
        vpnListView.setAdapter(vpnListAdapter);
        vpnListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);

        vpnListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
                vpnActMode = mode;
                mode.getMenuInflater().inflate(R.menu.vpn_list_context_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
                updateCab(mode); // restore CAB state
                return true;
            }

            @Override
            public void onDestroyActionMode(final ActionMode mode) {
                vpnActMode = null;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
                return onOptionsItemSelected(item);
            }

            @Override
            public void onItemCheckedStateChanged(final ActionMode mode, final int position, final long id,
                    final boolean checked) {
                updateCab(mode);
            }

            private void updateCab(final ActionMode mode) {
                int cnt = vpnListView.getCheckedItemCount();
                mode.setTitle(getString(R.string.title_select_vpn_count, cnt));
                mode.getMenu().findItem(R.id.menu_edit_vpn).setEnabled(cnt == 1);
                mode.getMenu().findItem(R.id.menu_del_vpn).setEnabled(cnt > 0);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
        super.onStart();
    
        // registerReceivers();
        checkAllVpnStatus();
        checkHack(false);
    }

    @Override
    protected void onStop() {
        save();
        // unregisterReceivers();

        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    
        Log.d(TAG, "onResume, check and run resume action");
        if (resumeAction != null) {
            Runnable action = resumeAction;
            resumeAction = null;
            runOnUiThread(action);
        }
    }

    protected void finishVpnAction() {
        if (vpnActMode == null)
            return;

        vpnActMode.finish();
    }

    /*
     * Check whether the system is hacked to allow 3rd-party keypair
     */
    private void checkHack(final boolean force) {
        HackKeyStore hack = new HackKeyStore(this);
        hack.check(force);
    }

    private void checkAllVpnStatus() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                actor.checkAllStatus();
            }
        }, "vpn-state-checker").start(); //$NON-NLS-1$
    }

    private void onAddVpn() {
        startActivityForResult(new Intent(this, VpnTypeSelection.class), REQ_SELECT_VPN_TYPE);
    }

    private void onDeleteVpn() {
        if (vpnListView.getCheckedItemCount() == 0)
            return;

        final SparseBooleanArray items = vpnListView.getCheckedItemPositions();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert).setTitle(android.R.string.dialog_alert_title)
                .setMessage(R.string.del_vpn_confirm).setCancelable(true);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                for (int i = 0; i < items.size(); i++) {
                    if (items.valueAt(i)) {
                        VpnProfile p = vpnListAdapter.getItem(items.keyAt(i));
                        repository.deleteVpnProfile(p);
                    }
                }

                finishVpnAction();
                vpnListAdapter.notifyDataSetChanged();
            }
        }).setNegativeButton(android.R.string.no, null).show();
    }

    private void onEditVpn() {
        int cnt = vpnListView.getCheckedItemCount();
        if (cnt > 1 || cnt == 0)
            return;
        
        SparseBooleanArray items = vpnListView.getCheckedItemPositions();
        VpnProfile p = null;

        for (int i = 0; i < items.size(); i++) {
            if (items.valueAt(i)) {
                p = vpnListAdapter.getItem(items.keyAt(i));
                break;
            }
        }

        if (p != null) {
            finishVpnAction();
            editVpn(p);
        }
    }

    private void editVpn(final VpnProfile p) {
        VpnType type = p.type;

        Class<? extends VpnProfileEditor> editorClass = type.getEditorClass();
        if (editorClass == null) {
            Log.d(TAG, "editor class is null for " + type);
            return;
        }

        Intent intent = new Intent(this, editorClass);
        intent.setAction(EditAction.EDIT.toString());
        intent.putExtra(KEY_VPN_PROFILE_NAME, p.name);
        startActivityForResult(intent, REQ_EDIT_VPN);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.vpn_list_menu, menu);

        menu.findItem(R.id.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
        menu.findItem(R.id.menu_help).setIcon(android.R.drawable.ic_menu_help);
        menu.findItem(R.id.menu_exp).setIcon(android.R.drawable.ic_menu_save);
        menu.findItem(R.id.menu_imp).setIcon(android.R.drawable.ic_menu_set_as);
        menu.findItem(R.id.menu_diag).setIcon(android.R.drawable.ic_menu_manage);
        menu.findItem(R.id.menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        menu.findItem(R.id.menu_exp).setEnabled(!repository.getAllVpnProfiles().isEmpty());
        menu.findItem(R.id.menu_imp).setEnabled(checkLastBackup());

        return true;
    }

    private boolean checkLastBackup() {
        return repository.checkLastBackup(getBackupDir()) != null;
    }

    /**
     * Handles item selections
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        boolean consumed = true;
        int itemId = item.getItemId();

        switch (itemId) {
        case R.id.menu_add:
            onAddVpn();
            break;
        case R.id.menu_edit_vpn:
            onEditVpn();
            break;
        case R.id.menu_del_vpn:
            onDeleteVpn();
            break;
        case R.id.menu_about:
            showDialog(DLG_ABOUT);
            break;
        case R.id.menu_help:
            openWikiHome();
            break;
        case R.id.menu_exp:
            showDialog(DLG_BACKUP);
            break;
        case R.id.menu_imp:
            showDialog(DLG_RESTORE);
            break;
        case R.id.menu_diag:
            checkHack(true);
            break;
        case R.id.menu_settings:
            openSettings();
            break;
        default:
            consumed = super.onContextItemSelected(item);
            break;
        }

        return consumed;
    }


    private void openSettings() {
        startActivity(new Intent(this, Settings.class));
    }

    private AlertDialog createBackupDlg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info).setTitle(R.string.export).setMessage(getString(R.string.i_exp, getBackupDir()));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                doBackup();
            }
        }).setNegativeButton(android.R.string.cancel, null);

        AlertDialog dlg = builder.create();
        dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                Log.d(TAG, "onDismiss DLG_BACKUP");
                removeDialog(DLG_BACKUP);
            }
        });
        return dlg;
    }

    private void doBackup() {
        Log.d(TAG, "doBackup");

        try {
            repository.backup(getBackupDir());
            Toast.makeText(this, R.string.i_exp_done, Toast.LENGTH_SHORT).show();
        } catch (AppException e) {
            Log.e(TAG, "doBackup failed", e);
            Utils.showErrMessage(this, e);
        }
    }

    private AlertDialog createRestoreDlg() {
        String lastBak = makeLastBackupText();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info).setTitle(R.string.imp).setMessage(getString(R.string.i_imp, lastBak));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                doRestore();
            }
        }).setNegativeButton(android.R.string.cancel, null);

        AlertDialog dlg = builder.create();
        dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                Log.d(TAG, "onDismiss DLG_RESTORE");
                removeDialog(DLG_RESTORE);
            }
        });
        return dlg;
    }

    private String makeLastBackupText() {
        Date lastBackup = repository.checkLastBackup(getBackupDir());
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return f.format(lastBackup);
    }

    private void doRestore() {
        Log.d(TAG, "doRestore");

        try {
            repository.restore(getBackupDir());
            initVpnList();

            actor.disconnect();
            checkAllVpnStatus();
            Toast.makeText(this, R.string.i_imp_done, Toast.LENGTH_SHORT).show();
        } catch (AppException e) {
            Log.e(TAG, "doRestore failed", e);
            Utils.showErrMessage(this, e);
        }
    }

    private String getBackupDir() {
        return getString(R.string.exp_dir);
    }

    private void openWikiHome() {
        openUrl(getString(R.string.url_wiki_home));
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (data == null)
            return;

        switch (requestCode) {
        case REQ_SELECT_VPN_TYPE:
            onVpnTypePicked(data);
            break;
        case REQ_ADD_VPN:
            onVpnProfileAdded(data);
            break;
        case REQ_EDIT_VPN:
            onVpnProfileEdited();
            break;
        default:
            Log.w(TAG, "onActivityResult, unknown reqeustCode " + requestCode + ", result=" + resultCode + ", data=" + data); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            break;
        }
    }

    private void onVpnTypePicked(final Intent data) {
        VpnType pickedVpnType = (VpnType) data.getExtras().get(KEY_VPN_TYPE);
        addVpn(pickedVpnType);
    }

    private void addVpn(final VpnType vpnType) {
        Log.i(TAG, "add vpn " + vpnType); //$NON-NLS-1$
        Class<? extends VpnProfileEditor> editorClass = vpnType.getEditorClass();

        if (editorClass == null) {
            Log.d(TAG, "editor class is null for " + vpnType); //$NON-NLS-1$
            return;
        }

        Intent intent = new Intent(this, editorClass);
        intent.setAction(EditAction.CREATE.toString());
        startActivityForResult(intent, REQ_ADD_VPN);
    }

    private void onVpnProfileAdded(final Intent data) {
        Log.i(TAG, "new vpn profile created"); //$NON-NLS-1$
        refreshVpnListView();
    }

    private void onVpnProfileEdited() {
        Log.i(TAG, "vpn profile modified"); //$NON-NLS-1$
        refreshVpnListView();
    }

    private void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_VPN_CONNECTIVITY);
        stateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                String action = intent.getAction();

                if (ACTION_VPN_CONNECTIVITY.equals(action)) {
                    onStateChanged(intent);
                } else {
                    Log.d(TAG, "VPNSettings receiver ignores intent:" + intent); //$NON-NLS-1$
                }
            }
        };
        registerReceiver(stateBroadcastReceiver, filter);
    }

    private void onStateChanged(final Intent intent) {
        //Log.d(TAG, "onStateChanged: " + intent); //$NON-NLS-1$

        final String profileName = intent.getStringExtra(BROADCAST_PROFILE_NAME);
        final VpnState state = Utils.extractVpnState(intent);
        final int err = intent.getIntExtra(BROADCAST_ERROR_CODE, VPN_ERROR_NO_ERROR);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stateChanged(profileName, state, err);
            }
        });
    }

    private void stateChanged(final String profileName, final VpnState state, final int errCode) {
        //Log.d(TAG, "stateChanged, '" + profileName + "', state: " + state + ", errCode=" + errCode); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        VpnProfile p = repository.getProfileByName(profileName);

        if (p == null) {
            Log.w(TAG, profileName + " NOT found"); //$NON-NLS-1$
            return;
        }

        p.state = state;
        refreshVpnListView();
    }

    private void save() {
        repository.save();
    }

    private void unregisterReceivers() {
        if (stateBroadcastReceiver != null) {
            unregisterReceiver(stateBroadcastReceiver);
        }
    }

    protected void onActiveVpnChanged(final String id) {
        Assert.notNull(id);
        if (!id.equals(repository.getActiveProfileId())) {
            repository.setActiveProfileId(id);
            vpnListAdapter.notifyDataSetChanged();
        }
        finishVpnAction();
    }

    private void refreshVpnListView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vpnListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
        case DLG_ABOUT:
            return createAboutDlg();
        case DLG_BACKUP:
            return createBackupDlg();
        case DLG_RESTORE:
            return createRestoreDlg();
        default:
            break;
        }
        return null;
    }

    private Dialog createAboutDlg() {
        AlertDialog.Builder builder;

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.about, (ViewGroup) findViewById(R.id.aboutRoot));

        builder = new AlertDialog.Builder(this);
        builder.setView(layout).setTitle(getString(R.string.about));

        bindPackInfo(layout);

        AlertDialog dlg = builder.create();
        dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                Log.d(TAG, "onDismiss DLG_ABOUT");
                removeDialog(DLG_ABOUT);
            }
        });

        return dlg;
    }

    private void bindPackInfo(final View layout) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            TextView txtVer = (TextView) layout.findViewById(R.id.txtVersion);
            txtVer.setText(getString(R.string.pack_ver, getString(R.string.app_name), info.versionName));
        } catch (NameNotFoundException e) {
            Log.e(TAG, "get pack info failed", e); //$NON-NLS-1$
        }
    }

    private void openUrl(final String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    protected void toggleVpn(final VpnProfile p) {
        Assert.isTrue(p.state.isStable());

        if (p.state == VpnState.IDLE) {
            connect(p);
        } else {
            disconnect();
        }
    }

    private void connect(final VpnProfile p) {
        if (unlockKeyStoreIfNeeded(p)) {
            actor.connect(p);
        }
    }

    private boolean unlockKeyStoreIfNeeded(final VpnProfile p) {
        if (!p.needKeyStoreToConnect() || keyStore.isUnlocked())
            return true;

        Log.i(TAG, "keystore is locked, unlock it now and reconnect later.");
        resumeAction = new Runnable() {
            @Override
            public void run() {
                // redo this after unlock activity return
                connect(p);
            }
        };

        keyStore.unlock(this);
        return false;
    }

    private void disconnect() {
        actor.disconnect();
    }
}
