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

import org.json.JSONException;
import org.json.JSONObject;


public class PptpProfile extends VpnProfile {

    public boolean encrypted;

    public PptpProfile() {
        type = VpnType.PPTP;
    }

    /*
     * (non-Javadoc)
     * 
     * @see xink.vpn.VpnProfile#clone()
     */
    @Override
    public PptpProfile clone() {
        return (PptpProfile) super.clone();
    }

    /*
     * (non-Javadoc)
     * 
     * @see xink.vpn.VpnProfile#saveTo(org.json.JSONObject)
     */
    @Override
    protected void saveTo(final JSONObject jo) throws JSONException {
        super.saveTo(jo);
        jo.put("encrypt", encrypted);
    }

    /*
     * (non-Javadoc)
     * 
     * @see xink.vpn.VpnProfile#restoreFrom(org.json.JSONObject)
     */
    @Override
    protected void restoreFrom(final JSONObject jo) throws JSONException {
        super.restoreFrom(jo);
        encrypted = jo.getBoolean("encrypt");
    }
}
