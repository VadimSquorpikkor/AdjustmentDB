package com.squorpikkor.app.adjustmentdb;

public class Encrypter {

    /**
     * The key is used in encryption algorithm
     */
    private final static byte[] mKey = new byte[]{
            108, (byte) 234, 63, (byte) 156, 78, 47, 9, 73, (byte) 217, 48, (byte) 158};

    /**
     * Encrypts given byte array and decode it if array has been already encrypted
     *
     * @param inArray a byte array to encrypt
     * @return encrypted byte array
     */
    public static byte[] convertData(byte[] inArray) {
        byte[] outArray = new byte[inArray.length];
        int kN = 0;
        for (int i = 0; i < inArray.length; i++) {
            byte b1 = inArray[i];
            byte b2 = mKey[kN];
            outArray[i] = (byte) (b1 ^ b2);
            kN++;
            if (kN >= mKey.length)
                kN = 0;
        }
        return outArray;
    }
}
