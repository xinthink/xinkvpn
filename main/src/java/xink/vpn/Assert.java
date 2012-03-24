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

/**
 * 提供断言的支持
 * @author ywu
 */
public final class Assert {

    // 不允许实例化
    private Assert() {
        // do nothing
    }

    /**
     * Asserts the expression to be true
     * @param expr expression to be tested
     */
    public static void isTrue(final boolean expr) {
        isTrue(expr, "[Assertion failed] expression is expected to be true");
    }

    /**
     * Asserts the expression to be true
     * @param expr expression to be tested
     * @param msg assertion failure message
     */
    public static void isTrue(final boolean expr, final String msg) {
        if (!expr) throw new AssertionError(msg);
    }

    /**
     * Asserts the expression to be false
     * @param expr expression to be tested
     */
    public static void isFalse(final boolean expr) {
        isFalse(expr, "[Assertion failed] expression is expected to be true");
    }

    /**
     * Asserts the expression to be false
     * @param expr expression to be tested
     * @param msg assertion failure message
     */
    public static void isFalse(final boolean expr, final String msg) {
        if (expr) throw new AssertionError(msg);
    }

    /**
     * Asserts the expression NOT null
     * @param expr expression to be tested
     */
    public static void notNull(final Object expr) {
        notNull(expr, "[Assertion failed] expression is expected NOT null");
    }

    /**
     * Asserts the expression NOT null
     * @param expr expression to be tested
     * @param msg assertion failure message
     */
    public static void notNull(final Object expr, final String msg) {
        if (expr == null) throw new AssertionError(msg);
    }

    /**
     * Asserts the expression to be null
     * @param expr expression to be tested
     */
    public static void isNull(final Object expr) {
        isNull(expr, "[Assertion failed] expression is expected to be null");
    }

    /**
     * Asserts the expression to be null
     * @param expr expression to be tested
     * @param msg assertion failure message
     */
    public static void isNull(final Object expr, final String msg) {
        if (expr != null) throw new AssertionError(msg);
    }

    /**
     * Asserts two objects are equals, NOT null
     * @param obj1 Object
     * @param obj2 Object
     */
    public static void isEquals(final Object obj1, final Object obj2) {
        isEquals(obj1, obj2, "[Assertion failed] objects are expected to be equals");
    }

    /**
     * Asserts two objects are equals, NOT null
     * @param obj1 Object
     * @param obj2 Object
     * @param msg assertion failure message
     */
    public static void isEquals(final Object obj1, final Object obj2, final String msg) {
        if (obj1 == null || !obj1.equals(obj2)) throw new AssertionError(msg);
    }
}
