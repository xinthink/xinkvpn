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

package xink.vpn.test.helper;

import static junit.framework.Assert.*;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class JsonAssert {

    private JsonAssert() {
    }

    public static void assertJsonEquals(final JSONObject expected, final JSONObject actual) {
        assertNotNull("expected json should NOT be null", expected);
        assertNotNull("actual json should NOT be null", actual);
        assertEquals("length NOT equals", expected.length(), actual.length());

        for (@SuppressWarnings("rawtypes")
        Iterator it = expected.keys(); it.hasNext();) {
            String k = (String) it.next();
            assertProp(expected, actual, k);
        }
    }

    public static void assertJsonEquals(final JSONArray expected, final JSONArray actual) {
        assertNotNull("expected json should NOT be null", expected);
        assertNotNull("actual json should NOT be null", actual);
        assertEquals("length NOT equals", expected.length(), actual.length());
        
        for (int i = 0; i < expected.length(); i++) {
            assertItem(expected, actual, i);
        }
    }

    private static void assertProp(final JSONObject expected, final JSONObject actual, final String key) {
        try {
            assertTrue("no value for " + key, actual.has(key));
            Object expV = expected.get(key);
            Object actV = actual.get(key);

            doAssertion(expV, actV);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertItem(final JSONArray expected, final JSONArray actual, final int index) {
        try {
            Object expV = expected.get(index);
            Object actV = actual.get(index);

            doAssertion(expV, actV);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static void doAssertion(final Object exp, final Object act) {
        if (exp instanceof JSONArray) {
            assertJsonEquals((JSONArray) exp, (JSONArray) act);
        } else if (exp instanceof JSONObject) {
            assertJsonEquals((JSONObject) exp, (JSONObject) act);
        } else {
            assertEquals(exp.getClass(), act.getClass());
            assertEquals(exp, act);
        }
    }
}
