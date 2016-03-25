package com.example.leixiao.notepaper.utils;

import java.io.ByteArrayOutputStream;

public class MemoByteArrayOutputStream extends ByteArrayOutputStream {
    public byte[] getByteArrayBuffer() {
        return this.buf;
    }

    public int getByteArrayCount() {
        return this.count;
    }
}
