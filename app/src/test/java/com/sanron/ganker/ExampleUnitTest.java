package com.sanron.ganker;

import org.junit.Test;

import java.text.SimpleDateFormat;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        String s1 = "2015-10-09T04:54:34";
        String s2 = "2015-10-09T04:54:34.857000";
        String s3 = "2015-10-09T04:54:34.23Z";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        System.out.println(sdf.parse(s1));
        System.out.println(sdf.parse(s2));
        System.out.println(sdf.parse(s3));
    }
}