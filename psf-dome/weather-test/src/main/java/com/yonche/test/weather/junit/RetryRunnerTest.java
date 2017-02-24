package com.yonche.test.weather.junit;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by stony on 16/11/22.
 */
@RunWith(RetryRunner.class)
public class RetryRunnerTest {


    private int counter = 0;

    @RetryRunner.Repeat(10)
    @Test
    public void test(){
        System.out.println("  --  retry ---- " + (counter++));
    }

    @RetryRunner.Repeat(-10)
    @Test
    public void testx(){
        System.out.println("  --  retry ---- " + (counter++));
    }
}
