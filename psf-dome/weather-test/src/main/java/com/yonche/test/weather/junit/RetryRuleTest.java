package com.yonche.test.weather.junit;

import org.junit.Rule;
import org.junit.Test;

/**
 * Created by stony on 16/11/22.
 */
public class RetryRuleTest {

    @Rule
    public RetryRule retry = new RetryRule(3);

    @Test
    public void test1() {
        System.out.println("---- retry  -- ");
    }
}
