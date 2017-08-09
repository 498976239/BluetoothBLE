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
}
