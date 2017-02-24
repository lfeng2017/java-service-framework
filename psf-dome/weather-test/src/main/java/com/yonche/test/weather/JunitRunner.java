package com.yonche.test.weather;

import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Created by stony on 16/11/21.
 */
public class JunitRunner {

    public static void main(String[] args){
        String _funId = (args.length > 0) ? args[0] : "0";
        int funId = Integer.valueOf(_funId);
        Result result = null;
        switch (funId){
            case 3:
                result = JUnitCore.runClasses(PerformanceParallelMaxTest.class);
                break;
            case 2:
                result = JUnitCore.runClasses(PerformanceStabilizeTest.class);
                break;
            case 1:
                result = JUnitCore.runClasses(PerformanceParallelTest.class);
                break;
            case 0:
            default:
                result = JUnitCore.runClasses(PerformanceSingleTest.class);
                break;
        }
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.println("result successful : " + result.wasSuccessful()  + " ,failure : "  + result.getFailureCount() + " ,ignore : " + result.getIgnoreCount());
        System.exit(0);
    }
}
