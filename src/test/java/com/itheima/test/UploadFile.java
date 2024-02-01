package com.itheima.test;

import org.junit.jupiter.api.Test;

public class UploadFile {

    @Test
    public void test() {
        String fileName = "abc.jpg";

        String suffix = fileName.substring(fileName.lastIndexOf("."));

        System.out.println(suffix);
    }
}
