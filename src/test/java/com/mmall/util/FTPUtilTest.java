package com.mmall.util;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class FTPUtilTest {

    @Test
    public void uploadFile() throws IOException {
        File file = new File("/Users/ios/Downloads/11.jpg");


        boolean b = FTPUtil.uploadFile(Arrays.asList(file));
        System.out.println(b);
    }
}