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
package com.gotokeep.keep.taira.samples;

import com.google.gson.Gson;
import com.gotokeep.keep.taira.Taira;
import com.gotokeep.keep.taira.TairaData;

import java.nio.charset.Charset;
import java.util.Arrays;

public class Main {

    private static final Gson GSON = new Gson();

    public static void main(String[] args) {
        Taira.DEBUG = true;

        // Init object
        Foo foo = new Foo();
        foo.setByteField((byte) 2);
        foo.setIntField(103);
        foo.setCharField('$');
        foo.setDoubleField(123.21);
        foo.setBytesField(new byte[] { 11, 22, 33, 44 });
        foo.setStringField("world");

        Bar bar = new Bar();
        bar.setFloatVal(123.2f);
        bar.setShortVal((short) 11);
        bar.setLongVal(1242354L);
        bar.setBooleanVal(true);

        Baz[] bazArray = new Baz[3];
        bazArray[0] = new Baz(1);
        bazArray[1] = new Baz(3);
        bazArray[2] = new Baz(5);

        bar.setInnerArrayVal(bazArray);
        foo.setBarField(bar);
        foo.setIntListField(Arrays.asList(3, 5, 9));

        System.out.println("fooObject: " + foo.toString());

        // Taira serialize
        taira(foo);
        // Gson serialize
        gson(foo);
    }

    /**
     * Use Taira serialize/deserialize 1000 times
     */
    private static void taira(TairaData data) {
        long serializeStart = System.currentTimeMillis();
        byte[] fooBytes = new byte[0];
        for (int i = 0; i < 1000; i++) {
            fooBytes = Taira.DEFAULT.toBytes(data);
        }
        System.out.println("Taira serialize x 1000 time cost: " + (System.currentTimeMillis() - serializeStart));
        System.out.println("Taira serialize data size: " + fooBytes.length);

        long deserializeStart = System.currentTimeMillis();
        Foo foo = null;
        for (int i = 0; i < 1000; i++) {
            foo = Taira.DEFAULT.fromBytes(fooBytes, Foo.class);
        }
        System.out.println("Taira deserialize x 1000 time cost: " + (System.currentTimeMillis() - deserializeStart));
        System.out.println("Taira deserialize result: " + String.valueOf(foo));
    }

    /**
     * Use Gson serialize/deserialize 1000 times
     */
    private static void gson(TairaData object) {
        long serializeStart = System.currentTimeMillis();
        String json = "";
        for (int i = 0; i < 1000; i++) {
            json = GSON.toJson(object);
        }
        System.out.println("Gson serialize x 1000 time cost: " + (System.currentTimeMillis() - serializeStart));
        System.out.println("Gson serialize data size: " + json.getBytes(Charset.forName("UTF-8")).length);

        long deserializeStart = System.currentTimeMillis();
        Foo foo = null;
        for (int i = 0; i < 1000; i++) {
            foo = GSON.fromJson(json, Foo.class);
        }
        System.out.println("Gson deserialize x 1000 time cost: " + (System.currentTimeMillis() - deserializeStart));
        System.out.println("Gson deserialize result: " + String.valueOf(foo));
    }
}
