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

import com.gotokeep.keep.taira.annotation.ParamField;
import com.gotokeep.keep.taira.exception.TairaAnnotationException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Taira annotation related utils
 */
@SuppressWarnings("unchecked")
class AnnotationUtils {

    /**
     * ParamField order comparator
     */
    private static final Comparator<Field> ORDER_COMPARATOR = new Comparator<Field>() {
        @Override
        public int compare(Field left, Field right) {
            return left.getAnnotation(ParamField.class).order() - right.getAnnotation(ParamField.class).order();
        }
    };

    /**
     * cache a check-passed class type
     */
    private static final Set<Class<? extends TairaData>> ANNOTATION_CHECK_CACHE = new HashSet<>();

    private AnnotationUtils() {}

    /**
     * pre-check annotations before processing
     *
     * illegal use will throw {@link TairaAnnotationException}
     */
    public static void checkAnnotationOrThrow(Class<? extends TairaData> clazz) throws TairaAnnotationException {
        if (ANNOTATION_CHECK_CACHE.contains(clazz)) {
            return;
        }
        Set<Class<? extends TairaData>> recursiveTypeSet = new HashSet<>();
        checkAnnotationOrThrow(clazz, false, recursiveTypeSet);
        ANNOTATION_CHECK_CACHE.add(clazz);
    }

    /**
     * pre-check annotations before processing
     *
     * @param clazz class type
     * @param isRecursive whether it is a recursive call
     * @param recursiveTypeSet TairaData types in recursive calls
     */
    private static void checkAnnotationOrThrow(Class<? extends TairaData> clazz, boolean isRecursive,
                                               Set<Class<? extends TairaData>> recursiveTypeSet) {
        List<Field> fields = ReflectionUtils.extractAnnotatedFields(clazz, ParamField.class);

        if (fields.isEmpty()) {
            throw new TairaAnnotationException("No @ParamField declared in class [" + clazz.getName() + "]");
        }
        if (!ReflectionUtils.isNonParamConstructorExists(clazz)) {
            throw new TairaAnnotationException(
                "Class [" + clazz.getName() + "] should define a non-parameter constructor");
        }
        if (recursiveTypeSet.contains(clazz)) {
            throw new TairaAnnotationException("Recursive TairaData type " + clazz.getName() + " already exists");
        }

        Collections.sort(fields, ORDER_COMPARATOR);

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            Class fieldType = field.getType();
            PrimitiveProcessor intrinsicProcessor = TairaTypeConst.findIntrinsic(fieldType);

            // check ParamField
            checkParamFieldUsage(i, fields.size(), field, clazz, intrinsicProcessor, isRecursive);

            // check Fields
            if (intrinsicProcessor != null) {
                continue;
            }
            // TairaData type，recursively check
            if (TairaTypeConst.isTairaClass(fieldType)) {
                recursiveTypeSet.add(clazz);
                checkAnnotationOrThrow(fieldType, true, recursiveTypeSet);
                continue;
            }
            // String & byte[]，skip
            if (TairaTypeConst.isByteArray(field)) {
                continue;
            }
            // collection & other typed array
            if (TairaTypeConst.isSupportedCollection(fieldType) || fieldType.isArray()) {
                Class memberType = ReflectionUtils.getCollectionFirstMemberType(field);
                // abstract or interface member type not supported
                if (ReflectionUtils.isInterfaceOrAbstract(memberType)) {
                    throw new TairaAnnotationException(
                        "Member type of collection field [" + field.getName() + "] in class [" + clazz.getName()
                            + "] should not be interface or abstract");
                }
                // TairaData member type, recursively check
                if (TairaTypeConst.isTairaClass(memberType)) {
                    recursiveTypeSet.add(clazz);
                    checkAnnotationOrThrow(memberType, true, recursiveTypeSet);
                } else {
                    PrimitiveProcessor memberProcessor = TairaTypeConst.findIntrinsic(memberType);
                    // unsupported member type
                    if (memberProcessor == null) {
                        throw new TairaAnnotationException(
                            "Member type of collection field [" + field.getName() + "] in class [" + clazz.getName()
                                + "] can only be intrinsic type or TairaData");
                    }
                }
                continue;
            }

            // unsupported field type
            throw new TairaAnnotationException(
                "Type of field [" + field.getName() + "] in class [" + clazz.getName() + "] is not supported");
        }
    }

    /**
     * check ParamField
     */
    private static void checkParamFieldUsage(int fieldIndex, int fieldsSize, Field field, Class clazz,
                                             PrimitiveProcessor fieldProcessor, boolean isRecursive) {
        ParamField annotation = field.getAnnotation(ParamField.class);

        // check order
        if (annotation.order() != fieldIndex) {
            throw new TairaAnnotationException(
                "[order] on field [" + field.getName() + "] in class [" + clazz.getName() + "] is not sequential");
        }

        // check String & byte[]
        if (TairaTypeConst.isByteArray(field) && annotation.bytes() <= 0) {
            throw new TairaAnnotationException(
                "Field [" + field.getName() + "] in class [" + clazz.getName() + "] should specify [bytes] value");
        }

        // check intrinsic type bytes overflow
        if (fieldProcessor != null && annotation.bytes() > 0 && annotation.bytes() > fieldProcessor.byteSize()) {
            throw new TairaAnnotationException("[bytes] on field [" + field.getName() + "] in class [" + clazz.getName()
                + "] is too large (which should be lesser than or equal to " + fieldProcessor.byteSize() + ")");
        }

        // check collection & array length
        if (!TairaTypeConst.isByteArray(field) && (TairaTypeConst.isSupportedCollection(field.getType())
            || field.getType().isArray())) {
            // tail field without length
            if (fieldIndex < fieldsSize - 1 && annotation.length() == ParamField.LENGTH_DEFAULT) {
                throw new TairaAnnotationException(
                    "Field [" + field.getName() + "] in class [" + clazz.getName() + "] should specify [length] value");
            }
            // recursive TairaData tail field without length
            if (fieldIndex == fieldsSize - 1 && isRecursive && annotation.length() == ParamField.LENGTH_DEFAULT) {
                throw new TairaAnnotationException(
                    "Field [" + field.getName() + "] in recursive class [" + clazz.getName()
                        + "] should specify [length] value");
            }
        }
    }

    /**
     * get @ParamField annotated fields, and sort by order value
     *
     * @param clazz class type
     * @return sorted fields
     */
    public static List<Field> getSortedParamFields(Class<?> clazz) {
        List<Field> paramFields = ReflectionUtils.extractAnnotatedFields(clazz, ParamField.class);
        Collections.sort(paramFields, ORDER_COMPARATOR);
        return paramFields;
    }
}
