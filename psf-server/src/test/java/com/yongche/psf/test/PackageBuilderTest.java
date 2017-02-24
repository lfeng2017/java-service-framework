package com.yongche.psf.test;

import com.yongche.psf.core.PackageBuilder;
import org.junit.Test;

/**
 * Created by stony on 16/11/3.
 */
public class PackageBuilderTest {

    @Test
    public void test(){
        int num = 129;
        System.out.println("测试的int值为:" + num);

        byte[] int2bytes = PackageBuilder.int2Bytes(num);
        System.out.printf("int转成bytes: ");
        for (int i = 0; i < 4; ++i) {
            System.out.print(int2bytes[i] + " ");
        }
        System.out.println();

        int bytes2int = PackageBuilder.bytes2Int(int2bytes);
        System.out.println("bytes转行成int: " + bytes2int);

        byte int2OneByte = PackageBuilder.int2OneByte(num);
        System.out.println("int转行成one byte: " + int2OneByte);

        int oneByte2Int = PackageBuilder.oneByte2Int(int2OneByte);
        System.out.println("one byte转行成int: " + oneByte2Int);
        System.out.println();

        long longNum = 100000;
        System.out.println("测试的long值为：" + longNum);

        byte[] long2Bytes = PackageBuilder.long2Bytes(longNum);
        System.out.printf("long转行成bytes: ");
        for (int ix = 0; ix < long2Bytes.length; ++ix) {
            System.out.print(long2Bytes[ix] + " ");
        }
        System.out.println();

        long bytes2Long = PackageBuilder.bytes2Long(long2Bytes);
        System.out.println("bytes转行成long: " + bytes2Long);
    }
}
