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

    private int bytes;

    ByteArrayNode(Field field, Charset charset) {
        super(field);
        this.charset = charset;
        ParamField annotation = ReflectionUtils.getAnnotation(field, ParamField.class);
        bytes = annotation.bytes();
    }

    @Override
    public int evaluateSize(Object value) {
        if (bytes <= 0 && value != null) {
            return valueToByteArray(value).length;
        }
        return bytes;
    }

    @Override
    public void serialize(ByteBuffer buffer, Object value) {
        if (value == null) {
            buffer.put(new byte[bytes]);
            return;
        }
        byte[] byteValue = valueToByteArray(value);
        if (bytes <= 0) {
            // tail without [bytes]
            buffer.put(byteValue);
            return;
        }
        // with [bytes]
        checkOverflow(byteValue);
        int remainSize = bytes - byteValue.length;
        buffer.put(byteValue);
        if (remainSize > 0) {
            // fill remains
            buffer.put(new byte[remainSize]);
        }
    }

    @Override
    public Object deserialize(ByteBuffer buffer) {
        byte[] bytesValue;
        if (bytes <= 0) {
            // tail byte array
            bytesValue = new byte[buffer.remaining()];
        } else {
            bytesValue = new byte[bytes];
        }
        buffer.get(bytesValue);
        if (String.class.equals(clazz)) {
            return new String(bytesValue, charset);
        } else {
            return bytesValue;
        }
    }

    private void checkOverflow(byte[] array) {
        if (array.length > bytes) {
            throw new TairaIllegalValueException("Field [" + field.getName() + "] overflow, [bytes] should be larger");
        }
    }

    private byte[] valueToByteArray(Object value) {
        if (String.class.equals(clazz)) {
            return ((String) value).getBytes(charset);
        } else {
            return (byte[]) value;
        }
    }
}
