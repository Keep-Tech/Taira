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

import com.gotokeep.keep.taira.exception.TairaAnnotationException;
import com.gotokeep.keep.taira.exception.TairaIllegalValueException;
import com.gotokeep.keep.taira.exception.TairaInternalException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Taira API
 */
public class Taira {

    /**
     * default instance
     */
    public static Taira DEFAULT = new Taira();

    /**
     * debug switch
     *
     * in debug mode, {@link TairaInternalException} may throw
     */
    public static boolean DEBUG = false;

    /**
     * default charset for String encoding/decoding
     */
    private Charset charset = Charset.forName("utf-8");

    /**
     * default byte order
     */
    private ByteOrder order = ByteOrder.BIG_ENDIAN;

    /**
     * cache TairaDataNode root, speed up further executions
     */
    private final Map<Class<? extends TairaData>, TairaDataNode> rootNodeCache = new HashMap<>();

    /**
     * use default
     */
    private Taira() {}

    /**
     * construct Taira instance
     *
     * @param order specified byte order
     */
    public Taira(ByteOrder order) {
        this.order = order;
    }

    /**
     * construct Taira instance
     *
     * @param charset specified charset
     */
    public Taira(Charset charset) {
        this.charset = charset;
    }

    /**
     * construct Taira instance
     *
     * @param charset specified charset
     * @param order specified byte order
     */
    public Taira(Charset charset, ByteOrder order) {
        this.charset = charset;
        this.order = order;
    }

    /**
     * serialize TairaData instance to byte array
     *
     * @param object object instance to serialize
     * @return byte array
     * @throws TairaAnnotationException when annotation error
     * @throws TairaIllegalValueException when value error
     */
    public <T extends TairaData> byte[] toBytes(T object) throws TairaAnnotationException, TairaIllegalValueException {
        if (object == null) {
            return null;
        }
        try {
            AnnotationUtils.checkAnnotationOrThrow(object.getClass());
            return serializeArray(object);
        } catch (TairaInternalException e) {
            if (DEBUG) {
                throw e;
            } else {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * serialize TairaDta instance to byte buffer
     *
     * @param object object instance to serialize
     * @return byte array
     * @throws TairaAnnotationException when annotation error
     * @throws TairaIllegalValueException when value error
     */
    public <T extends TairaData> ByteBuffer toByteBuffer(T object)
        throws TairaAnnotationException, TairaIllegalValueException {
        if (object == null) {
            return null;
        }
        try {
            AnnotationUtils.checkAnnotationOrThrow(object.getClass());
            return serializeBuffer(object);
        } catch (TairaInternalException e) {
            if (DEBUG) {
                throw e;
            } else {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * deserialize byte array to TairaData instance
     *
     * @param data byte array data
     * @param clazz TairaData type
     * @return TairaData instance
     * @throws TairaAnnotationException when annotation error
     */
    public <T extends TairaData> T fromBytes(byte[] data, Class<T> clazz) throws TairaAnnotationException {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            AnnotationUtils.checkAnnotationOrThrow(clazz);
            return deserializeArray(data, clazz);
        } catch (TairaInternalException e) {
            if (DEBUG) {
                throw e;
            } else {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * deserialize byte array to TairaData instance
     *
     * @param data byte array data
     * @param clazz TairaData type
     * @return TairaData instance
     * @throws TairaAnnotationException when annotation error
     */
    public <T extends TairaData> T fromByteBuffer(ByteBuffer data, Class<T> clazz) throws TairaAnnotationException {
        if (data == null || !data.hasRemaining()) {
            return null;
        }
        try {
            AnnotationUtils.checkAnnotationOrThrow(clazz);
            return deserializeBuffer(data, clazz);
        } catch (TairaInternalException e) {
            if (DEBUG) {
                throw e;
            } else {
                e.printStackTrace();
            }
        }
        return null;
    }

    private <T extends TairaData> byte[] serializeArray(T data) {
        return serializeBuffer(data).array();
    }

    @SuppressWarnings("unchecked")
    private <T extends TairaData> T deserializeBuffer(ByteBuffer buffer, Class<T> clazz) {
        TairaDataNode root = getTairaNode(clazz, charset);
        return (T) root.deserialize(buffer);
    }

    private <T extends TairaData> ByteBuffer serializeBuffer(T data) {
        TairaDataNode root = getTairaNode(data.getClass(), charset);
        int byteSize = root.evaluateSize(data);
        ByteBuffer buffer;
        try {
            buffer = ByteBuffer.allocate(byteSize).order(order);
        } catch (IndexOutOfBoundsException | IllegalStateException e) {
            throw new TairaInternalException(e);
        }
        root.serialize(buffer, data);
        buffer.flip();
        ByteBuffer readonlyBuffer = buffer.asReadOnlyBuffer();
        ByteBuffer compactBuffer = ByteBuffer.allocate(readonlyBuffer.limit()).order(order);
        compactBuffer.put(Arrays.copyOfRange(buffer.array(), 0, readonlyBuffer.limit()));

        return compactBuffer;
    }

    private <T extends TairaData> T deserializeArray(byte[] array, Class<T> clazz) {
        ByteBuffer buffer;
        try {
            buffer = ByteBuffer.wrap(array).order(order);
        } catch (IndexOutOfBoundsException | IllegalStateException e) {
            throw new TairaInternalException(e);
        }
        return deserializeBuffer(buffer, clazz);
    }

    /**
     * get a TairaDataNode instance from cache or create one
     *
     * @param clazz class
     * @return node
     */
    private TairaDataNode getTairaNode(Class<? extends TairaData> clazz, Charset charset) {
        TairaDataNode node = rootNodeCache.get(clazz);
        if (node == null) {
            node = new TairaDataNode(clazz, charset);
            rootNodeCache.put(clazz, node);
        }
        return node;
    }
}
