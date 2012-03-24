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

package xink.vpn.wrapper;

import xink.vpn.R;
import xink.vpn.editor.L2tpIpsecPskProfileEditor;
import xink.vpn.editor.L2tpProfileEditor;
import xink.vpn.editor.PptpProfileEditor;
import xink.vpn.editor.VpnProfileEditor;

public enum VpnType {
    PPTP("PPTP", R.string.vpn_pptp, R.string.vpn_pptp_info, PptpProfile.class, PptpProfileEditor.class),
    L2TP("L2TP", R.string.vpn_l2tp, R.string.vpn_l2tp_info, L2tpProfile.class, L2tpProfileEditor.class),
    L2TP_IPSEC_PSK("L2TP/IPSec PSK", R.string.vpn_l2tp_psk, R.string.vpn_l2tp_psk_info, L2tpIpsecPskProfile.class, L2tpIpsecPskProfileEditor.class),
    // L2TP_IPSEC("L2TP/IPSec CRT", null)
    ;

    private String name;
    private Class<? extends VpnProfile> clazz;
    private boolean active;
    private int descRid;
    private int nameRid;
    private Class<? extends VpnProfileEditor> editorClass;

    VpnType(final String name, final int nameRid, final int descRid, final Class<? extends VpnProfile> clazz,
            final Class<? extends VpnProfileEditor> editorClass) {
        this.name = name;
        this.nameRid = nameRid;
        this.descRid = descRid;
        this.clazz = clazz;
        this.editorClass = editorClass;
    }

    public String getName() {
        return name;
    }

    public Class<? extends VpnProfile> getProfileClass() {
        return clazz;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean a) {
        this.active = a;
    }

    public int getNameRid() {
        return nameRid;
    }

    public int getDescRid() {
        return descRid;
    }

    public Class<? extends VpnProfileEditor> getEditorClass() {
        return editorClass;
    }

}
