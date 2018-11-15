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
import java.util.List;

public class Foo implements TairaData {

    @ParamField(order = 0) private byte byteField;

    @ParamField(order = 1) private Bar barField;

    @ParamField(order = 2, bytes = 2) private int intField;

    @ParamField(order = 3) private double doubleField;

    @ParamField(order = 4) private char charField;

    @ParamField(order = 5, bytes = 5) private byte[] bytesField;

    @ParamField(order = 6, bytes = 5) private String stringField;

    @ParamField(order = 7, length = 3) private List<Integer> intListField;

    @ParamField(order = 8) private String remainsStringField;

    public byte getByteField() {
        return byteField;
    }

    public void setByteField(byte byteField) {
        this.byteField = byteField;
    }

    public int getIntField() {
        return intField;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public double getDoubleField() {
        return doubleField;
    }

    public void setDoubleField(double doubleField) {
        this.doubleField = doubleField;
    }

    public void setCharField(char charField) {
        this.charField = charField;
    }

    public char getCharField() {
        return charField;
    }

    public byte[] getBytesField() {
        return bytesField;
    }

    public void setBytesField(byte[] bytesField) {
        this.bytesField = bytesField;
    }

    public String getStringField() {
        return stringField;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public Bar getBarField() {
        return barField;
    }

    public void setBarField(Bar barField) {
        this.barField = barField;
    }

    public List<Integer> getIntListField() {
        return intListField;
    }

    public void setIntListField(List<Integer> intListField) {
        this.intListField = intListField;
    }

    public String getRemainsStringField() {
        return remainsStringField;
    }

    public void setRemainsStringField(String remainsStringField) {
        this.remainsStringField = remainsStringField;
    }

    @Override
    public String toString() {
        return "Foo{" + "byteField=" + byteField + ", barField=" + barField + ", intField=" + intField
            + ", doubleField=" + doubleField + ", charField=" + charField + ", bytesField=" + Arrays.toString(
            bytesField) + ", stringField='" + stringField + '\'' + ", intListField=" + intListField
            + ", remainsStringField='" + remainsStringField + '\'' + '}';
    }
}
