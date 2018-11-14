/*
 * MIT License
 *
 * Copyright (c) 2018 Keep-Tech
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

import com.gotokeep.keep.taira.TairaData;
import com.gotokeep.keep.taira.annotation.ParamField;

import java.util.Arrays;

public class Bar implements TairaData {

    @ParamField(order = 0, length = 3) private Baz[] innerArrayVal;

    @ParamField(order = 1) private float floatVal;

    @ParamField(order = 2) private short shortVal;

    @ParamField(order = 3, bytes = 5) private long longVal;

    @ParamField(order = 4) private boolean booleanVal;

    public float getFloatVal() {
        return floatVal;
    }

    public void setFloatVal(float floatVal) {
        this.floatVal = floatVal;
    }

    public short getShortVal() {
        return shortVal;
    }

    public void setShortVal(short shortVal) {
        this.shortVal = shortVal;
    }

    public long getLongVal() {
        return longVal;
    }

    public void setLongVal(long longVal) {
        this.longVal = longVal;
    }

    public boolean isBooleanVal() {
        return booleanVal;
    }

    public void setBooleanVal(boolean booleanVal) {
        this.booleanVal = booleanVal;
    }

    public Baz[] getInnerArrayVal() {
        return innerArrayVal;
    }

    public void setInnerArrayVal(Baz[] innerArrayVal) {
        this.innerArrayVal = innerArrayVal;
    }

    @Override
    public String toString() {
        return "Bar{" + "innerArrayVal=" + Arrays.toString(innerArrayVal) + ", floatVal=" + floatVal + ", shortVal="
            + shortVal + ", longVal=" + longVal + ", booleanVal=" + booleanVal + '}';
    }
}
