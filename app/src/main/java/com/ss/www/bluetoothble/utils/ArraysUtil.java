package com.ss.www.bluetoothble.utils;

/**
 * Created by SS on 17-8-4.
 */
public class ArraysUtil {
    /**得到数组的最大值
     * @param arr
     * @return
     */
    public static float getMax(float[] arr){
        float max=arr[0];
        for(int i=1;i<arr.length;i++){
            if(arr[i]>max){
                max=arr[i];
            }
        }
        return max;
    }

    /**得到数组的最小值
     * @param arr
     * @return
     */
    public static float getMin(float[] arr){
        float min=arr[0];
        for (int i = 0; i < arr.length; i++) {
            if(arr[i] < min){
                min = arr[i];
            }
        }
        return min;
    }
    public static int byte2ToUnsignShort(byte[] b){
        return ((b[0]&0xff)<<8)|(b[1]&0xff);
    }
    //int转换成byte[]
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        //由高位到低位
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }
    //生成crc校验码
    public static int getCrc16(byte[] arr_buff) {
        int len = arr_buff.length;
        //预置 1 个 16 位的寄存器为十六进制FFFF, 称此寄存器为 CRC寄存器。
        int crc = 0xFFFF;
        int i, j;
        for (i = 0; i < len; i++) {
            //把第一个 8 位二进制数据 与 16 位的 CRC寄存器的低 8 位相异或, 把结果放于 CRC寄存器
            crc = ((crc & 0xFF00) | (crc & 0x00FF) ^ (arr_buff[i] & 0xFF));
            for (j = 0; j < 8; j++) {
                //把 CRC 寄存器的内容右移一位( 朝低位)用 0 填补最高位, 并检查右移后的移出位
                if ((crc & 0x0001) > 0) {
                    //如果移出位为 1, CRC寄存器与多项式A001进行异或
                    crc = crc >> 1;
                    crc = crc ^ 0xA001;
                } else
                    //如果移出位为 0,再次右移一位
                    crc = crc >> 1;
            }
        }
        return crc;

    }
    //字节数组转成整形
    public static int bytesToInt(byte[] b) {
        if(b.length == 4){
            int i = (b[0] << 24) & 0xFF000000;
            i |= (b[1] << 16) & 0xFF0000;

            i |= (b[2] << 8) & 0xFF00;

            i |= b[3] & 0xFF;
            return i;
        }
        return 0;
    }
}
