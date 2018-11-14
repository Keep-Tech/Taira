/*
 * Copyright (c) 2018 Keep, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.gotokeep.keep.taira;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * define some Taira supported types and functions
 */
final class TairaTypeConst {

    /**
     * TairaData instance type
     */
    private static final Class<TairaData> TAIRA_CLASS = TairaData.class;

    /**
     * currently Taira supports List & Set
     */
    private static final Set<Class<? extends Collection>> SUPPORTED_COLLECTION_TYPE = new HashSet<>();

    static {
        SUPPORTED_COLLECTION_TYPE.add(List.class);
        SUPPORTED_COLLECTION_TYPE.add(Set.class);
    }

    private TairaTypeConst() {}

    /**
     * get a intrinsic type processor
     *
     * @param clazz class
     * @return intrinsic type
     */
    public static TairaPrimitive findIntrinsic(Class clazz) {
        for (TairaPrimitive type : TairaPrimitive.values()) {
            if (type.canProcess(clazz)) {
                return type;
            }
        }
        return null;
    }

    /**
     * whether it's a supported collection
     *
     * @param fieldType class
     * @return true if supported
     */
    public static boolean isSupportedCollection(Class fieldType) {
        for (Class clazz : TairaTypeConst.SUPPORTED_COLLECTION_TYPE) {
            if (clazz.equals(fieldType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * create collection instance when deserialize
     *
     * @return collection instance
     */
    public static Collection newCollection(Class fieldType) {
        if (List.class.equals(fieldType)) {
            return new ArrayList();
        } else if (Set.class.equals(fieldType)) {
            return new HashSet();
        }
        return null;
    }

    /**
     * whether it's TairaData instance
     *
     * @param clazz class
     * @return true if it's TairaData
     */
    public static boolean isTairaClass(Class clazz) {
        return clazz != null && ReflectionUtils.isParentClass(TAIRA_CLASS, clazz);
    }

    /**
     * whether it's byte array (including String type)
     *
     * @param field field
     * @return true if it's String or byte[]
     */
    public static boolean isByteArray(Field field) {
        if (field == null) {
            return false;
        }
        if (field.getType().isArray()) {
            Class memberType = ReflectionUtils.getCollectionFirstMemberType(field);
            return byte.class.equals(memberType) || Byte.class.equals(memberType);
        }
        return String.class.equals(field.getType());
    }

    /**
     * internal debug log
     *
     * @param from log tag
     * @param message log content
     */
    public static void log(String from, String message) {
        if (Taira.DEBUG) {
            System.out.println("[" + from + "] " + message);
        }
    }
}
