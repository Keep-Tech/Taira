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
import com.gotokeep.keep.taira.exception.TairaInternalException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

/**
 * node for processing collection & array
 */
@SuppressWarnings({ "unchecked" })
class CollectionNode extends Node {

    /**
     * charset
     */
    private Charset charset;

    /**
     * length defined in ParamField
     */
    private int length;

    /**
     * member type
     */
    private Class memberType;

    /**
     * member type node
     */
    private Node memberNode;

    CollectionNode(Field field, Charset charset) {
        super(field);
        this.charset = charset;

        ParamField annotation = ReflectionUtils.getAnnotation(field, ParamField.class);
        length = annotation.length();
        memberType = ReflectionUtils.getCollectionFirstMemberType(field);
        memberNode = createMemberNode();
    }

    @Override
    public int evaluateSize(Object value) {
        int collectionLength = getCollectionLength(value);
        int memberByteSize = memberNode.evaluateSize(null);
        if (length <= 0) {
            return collectionLength * memberByteSize;
        } else {
            return length * memberByteSize;
        }
    }

    @Override
    public void serialize(ByteBuffer buffer, Object value) {
        int collectionLength = getCollectionLength(value);
        checkOverflow(collectionLength);
        serializeMembers(buffer, value);
    }

    @Override
    public Object deserialize(ByteBuffer buffer) {
        Collection collection;
        if (clazz.isArray()) {
            collection = new ArrayList();
        } else {
            collection = TairaTypeConst.newCollection(clazz);
        }
        if (collection == null) {
            return null;
        }
        if (length <= 0) {
            // buffer tail
            while (buffer.position() < buffer.limit()) {
                collection.add(memberNode.deserialize(buffer));
            }
        } else {
            for (int i = 0; i < length; i++) {
                collection.add(memberNode.deserialize(buffer));
            }
        }
        if (clazz.isArray()) {
            return collection.toArray((Object[]) Array.newInstance(memberType, collection.size()));
        } else {
            return collection;
        }
    }

    private Node createMemberNode() {
        TairaPrimitive primitive = TairaTypeConst.findPrimitive(memberType);
        if (primitive != null) {
            return new PrimitiveNode(memberType, primitive);
        } else if (TairaTypeConst.isTairaClass(memberType)) {
            return new TairaDataNode(memberType, charset);
        } else {
            // illegal type, annotation error
            throw new TairaInternalException(
                "Illegal field type [" + field.getType() + "] in class [" + clazz.getName() + "]");
        }
    }

    private int getCollectionLength(Object value) {
        if (value != null) {
            if (clazz.isArray()) {
                return Array.getLength(value);
            } else if (TairaTypeConst.isSupportedCollection(clazz)) {
                return ((Collection) value).size();
            }
        }
        return 0;
    }

    private void serializeMembers(ByteBuffer buffer, Object value) {
        int collectionLength = getCollectionLength(value);
        if (clazz.isArray()) {
            for (int i = 0; i < collectionLength; i++) {
                memberNode.serialize(buffer, Array.get(value, i));
            }
        } else {
            Collection collection = (Collection) value;
            for (Object member : collection) {
                memberNode.serialize(buffer, member);
            }
        }
        // node with length, fill remain empty bytes
        if (length > 0) {
            int memberByteSize = memberNode.evaluateSize(null);
            buffer.put(new byte[(length - collectionLength) * memberByteSize]);
        }
    }

    private void checkOverflow(int memberCount) {
        if (memberCount > length) {
            throw new TairaIllegalValueException("Field [" + field.getName() + "] overflow, [length] should be larger");
        }
    }
}
