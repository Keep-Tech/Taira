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
import com.gotokeep.keep.taira.exception.TairaIllegalValueException;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * node for processing primitive type
 */
class PrimitiveNode extends Node {

    private TairaPrimitive type;

    private int bytes;

    /**
     * collection member node without field
     */
    PrimitiveNode(Class clazz, TairaPrimitive type) {
        super(clazz);
        this.type = type;
        bytes = type.byteSize();
    }

    /**
     * field node
     */
    PrimitiveNode(Field field, TairaPrimitive type) {
        super(field);
        this.field = field;
        this.type = type;
        bytes = evaluatePrimitiveSize(field);
    }

    @Override
    public int evaluateSize(Object value) {
        return bytes;
    }

    @Override
    public void serialize(ByteBuffer buffer, Object value) {
        checkOverflow(value);
        type.serialize(value == null ? type.defaultValue() : value, buffer, bytes);
    }

    @Override
    public Object deserialize(ByteBuffer buffer) {
        return type.deserialize(buffer, bytes);
    }

    /**
     * evaluate size
     *
     * @return size
     */
    private int evaluatePrimitiveSize(Field field) {
        ParamField annotation = ReflectionUtils.getAnnotation(field, ParamField.class);
        int realLength = type.byteSize();
        int annotationLength = annotation.bytes();
        if (annotationLength <= 0 || realLength <= 1 || realLength == annotationLength) {
            return realLength;
        }
        return annotationLength;
    }

    private void checkOverflow(Object value) throws TairaIllegalValueException {
        if (type.byteSize() <= 1 || value == null) {
            return;
        }
        // check value overflow when [bytes] specified
        double doubleValue;
        if (TairaPrimitive.CHAR.canProcess(clazz)) {
            // char to number
            doubleValue = Character.getNumericValue((Character) value);
        } else {
            doubleValue = ((Number) value).doubleValue();
        }
        double max = Math.pow(2, 8 * bytes - 1);
        if (doubleValue >= -max && doubleValue < max) {
            return;
        }
        throw new TairaIllegalValueException("Value [" + value + "] overflow, [bytes] should be larger");
    }
}
