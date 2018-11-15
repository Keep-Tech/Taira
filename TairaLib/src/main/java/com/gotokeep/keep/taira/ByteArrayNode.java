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
import java.nio.charset.Charset;

/**
 * node for processing byte[] and String
 */
class ByteArrayNode extends Node {

    private Charset charset;

    ByteArrayNode(Field field, Charset charset) {
        super(field);
        this.charset = charset;

        byteSize = evaluateSize(field);
    }

    @Override
    public void evaluateSize(Object value) {
        super.evaluateSize(value);
        byteSize = valueToByteArray(value).length;
    }

    @Override
    public void serialize(ByteBuffer buffer, Object value) {
        byte[] byteValue = valueToByteArray(value);
        checkOverflow(byteValue);
        int remainSize = byteSize - byteValue.length;
        buffer.put(byteValue);
        if (remainSize > 0) {
            // fill remains
            buffer.put(new byte[remainSize]);
        }
    }

    @Override
    public Object deserialize(ByteBuffer buffer) {
        byte[] bytes;
        if (byteSize <= 0) {
            // tail byte array
            bytes = new byte[buffer.remaining()];
        } else {
            bytes = new byte[byteSize];
        }
        buffer.get(bytes);
        if (String.class.equals(clazz)) {
            return new String(bytes, charset);
        } else {
            return bytes;
        }
    }

    private int evaluateSize(Field field) {
        ParamField annotation = ReflectionUtils.getAnnotation(field, ParamField.class);
        return annotation.bytes();
    }

    private void checkOverflow(byte[] array) {
        if (array.length > byteSize) {
            throw new TairaIllegalValueException("Field [" + field.getName() + "] overflow, [bytes] should be larger");
        }
    }

    private byte[] valueToByteArray(Object value) {
        if (value == null) {
            return new byte[byteSize];
        }
        if (String.class.equals(clazz)) {
            return ((String) value).getBytes(charset);
        } else {
            return (byte[]) value;
        }
    }
}
