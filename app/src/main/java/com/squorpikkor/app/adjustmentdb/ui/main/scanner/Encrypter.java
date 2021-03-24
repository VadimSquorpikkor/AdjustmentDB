package com.squorpikkor.app.adjustmentdb.ui.main.scanner;

public class Encrypter {

    private final static int[] mKey = new int[]{
             128, 214, 36, 111, 87, 74, 8, 33, 117, 48, 158};

    public static int[] convertData(int[] inArray) {
        int[] outArray = new int[inArray.length];
        int kN = 0;
        for (int i = 0; i < inArray.length; i++) {
            int b1 = inArray[i];
            int b2 = mKey[kN];
            outArray[i] = b1 ^ b2;
            kN++;
            if (kN >= mKey.length)
                kN = 0;
        }
        return outArray;
    }

    static int[] arrFromString(String s) {
        int[] arr = new int[s.length()];
        for (int i = 0; i < s.length(); i++) {
            arr[i] = s.charAt(i);
        }
        return arr;
    }

    static String stringFromArr(int[] arr) {
        StringBuilder s = new StringBuilder();
        char ch;
        for (int i = 0; i < arr.length; i++) {
            ch = (char)arr[i];
            s.append(ch);
        }
        return s.toString();
    }

    public static String decodeMe(String code) {
        return stringFromArr(Encrypter.convertData(arrFromString(code)));
    }
}
