package com.atguan.gmall.payment.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Streamutil {

    public static String inputStream2String(InputStream inStream,String encoding) {
        int _buffer_size = 1024;
        String result = null;
        if (inStream != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] tempBytes = new byte[_buffer_size];
            int count = -1;
            try {
                while ((count = inStream.read(tempBytes,0,_buffer_size)) != -1) {

                    outputStream.write(tempBytes,0,count);
                }
                tempBytes = null;
                outputStream.flush();
                result = new String(outputStream.toByteArray(),encoding);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
