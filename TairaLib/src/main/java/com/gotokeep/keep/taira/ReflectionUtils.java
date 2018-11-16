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

import com.gotokeep.keep.taira.exception.TairaInternalException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * reflection utils
 */
@SuppressWarnings({ "unchecked" })
public class ReflectionUtils {

    private ReflectionUtils() {}

    /**
     * get field value from object
     *
     * @param targetObject object
     * @param field field
     * @return field value
     */
    public static <T> T getFieldValue(Object targetObject, Field field) {
        if (targetObject == null || field == null) {
            return null;
        }
        try {
            field.setAccessible(true);
            return (T) field.get(targetObject);
        } catch (SecurityException | ClassCastException | IllegalArgumentException | IllegalAccessException e) {
            throw new TairaInternalException(e);
        }
    }

    /**
     * check non-param constructor
     *
     * @return true if exists
     */
    public static boolean isNonParamConstructorExists(Class clazz) {
        try {
            Constructor[] constructors = clazz.getDeclaredConstructors();
            for (Constructor constructor : constructors) {
                if (constructor.getParameterTypes().length == 0) {
                    return true;
                }
            }
        } catch (SecurityException e) {
            throw new TairaInternalException(e);
        }
        return false;
    }

    /**
     * get annotation instance from a field
     *
     * @param field field
     * @param annotationClazz annotation type
     * @return annotation instance
     */
    public static <T extends Annotation> T getAnnotation(Field field, Class<T> annotationClazz) {
        if (field == null || annotationClazz == null) {
            throw new TairaInternalException(
                "Field [" + field + "] or Annotation Class [" + annotationClazz + "] is NULL");
        }
        try {
            return field.getAnnotation(annotationClazz);
        } catch (ClassCastException e) {
            throw new TairaInternalException(e);
        }
    }

    /**
     * construct object instance
     *
     * @param clazz class type
     * @return class instance
     */
    public static <T> T createParamInstance(Class<?> clazz) {
        T object;
        try {
            TairaPrimitive processor = TairaTypeConst.findPrimitive(clazz);
            if (processor != null) {
                object = (T) processor.defaultValue();
            } else {
                object = (T) clazz.newInstance();
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new TairaInternalException(e);
        }
        if (object == null) {
            object = (T) Array.get(Array.newInstance(clazz, 1), 0);
        }
        return object;
    }

    /**
     * get fields from class which has specified annotation
     *
     * @param clazz class type
     * @param annotationClazz Annotation type
     * @return fields list
     */
    static List<Field> extractAnnotatedFields(Class<?> clazz,
                                              Class<? extends Annotation> annotationClazz) {
        return extractAnnotatedFields(clazz, annotationClazz, false);
    }

    /**
     * get fields from class which has specified annotation
     *
     * @param clazz class type
     * @param annotationClazz Annotation type
     * @param hierarchically if check all parent classes' fields recursively
     * @return fields list
     */
    static List<Field> extractAnnotatedFields(Class<?> clazz,
                                              Class<? extends Annotation> annotationClazz,
                                              boolean hierarchically) {
        List<Field> fields = new ArrayList<>();
        do {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(annotationClazz)) {
                    fields.add(field);
                }
            }
            if (!hierarchically) {
                break;
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        return fields;
    }

    /**
     * set object field value
     *
     * @param targetObject object
     * @param field field
     * @param fieldValue value
     */
    public static void setField(Object targetObject, Field field, Object fieldValue) {
        if (targetObject == null || field == null) {
            throw new TairaInternalException("Field [" + field + "] or targetObject [" + targetObject + "] is NULL");
        }
        try {
            field.setAccessible(true);
            field.set(targetObject, fieldValue);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException e) {
            throw new TairaInternalException(e);
        }
    }

    /**
     * get member type from array or collection
     *
     * @param collectionField 集合 field
     * @return member type
     */
    public static Class<?> getCollectionFirstMemberType(Field collectionField) {
        Class<?> collectionType = collectionField.getType();
        if (collectionType.isArray()) {
            return collectionType.getComponentType();
        } else if (TairaTypeConst.isSupportedCollection(collectionType)) {
            return (Class<?>) ((ParameterizedType) collectionField.getGenericType()).getActualTypeArguments()[0];
        }
        return null;
    }

    /**
     * check class inheritance
     *
     * @return true if child inherited from parent
     */
    static boolean isParentClass(Class parent, Class child) {
        return parent != null && child != null && parent.isAssignableFrom(child);
    }

    /**
     * check interface or abstract class
     *
     * @return true if abstract or interface
     */
    static boolean isInterfaceOrAbstract(Class clazz) {
        return clazz != null && (Modifier.isAbstract(clazz.getModifiers()) || Modifier.isInterface(
            clazz.getModifiers()));
    }
}
