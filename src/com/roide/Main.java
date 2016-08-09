package com.roide;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Main {

    private String[] mUUIDHashedArray;
    // To keep a count of no of times a key was randomly selected during sampling
    private HashMap<String, Integer> mKeySelectionCountMap = new HashMap<>();

    public static void main(String[] args) {
        // write your code here
        for(int i=5; i<=50; i=i+5) {
            Main main = new Main();
            System.out.print("i=" + (500 * i) + "\t");
            //main.testModifiedBinarySearch();
            main.generateUUIDs(500 * i);
            main.sortUUIDArray();
            main.doSampling();
            main.printSamplingResult();
        }
    }

    private void doSampling() {
        for (int i = 0; i < mUUIDHashedArray.length; i++) {
            mKeySelectionCountMap.put(mUUIDHashedArray[i], 0);
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 100; j++) {
                for (int k = 0; k < 100; k++) {
                    String genKey = genHash(UUID.randomUUID().toString());
                    String closestKey = closestKey(genKey);
                    mKeySelectionCountMap.put(closestKey, mKeySelectionCountMap.get(closestKey) + 1);
                }
            }
        }
    }

    private void printSamplingResult() {
        int total = 0;

        for(Integer val : mKeySelectionCountMap.values()) {
            total += val;
        }

        double expFreq = total / mKeySelectionCountMap.size();
        double totalDev = 0;
        for (String key : mKeySelectionCountMap.keySet()) {
            Integer value = mKeySelectionCountMap.get(key);
            //System.out.println("key=" + key + "::val=" + value + "::dev=" + Math.abs(expFreq - value));
            totalDev += Math.abs(expFreq - value)/expFreq;
        }
        double avgDeviation = totalDev / mKeySelectionCountMap.size();
        //System.out.println("total=" + total + "\texpFreq=" + expFreq + "\ttotalDev=" + totalDev + "\t:avgDeviation=" + avgDeviation);
        //System.out.println("Quality " + (1-avgDeviation) + " %");
        System.out.println(String.format("Quality: %.2f", (1-avgDeviation)));
    }

    private String closestKey(String key) {
        return mUUIDHashedArray[modifiedBinSearch(0, mUUIDHashedArray.length - 1, key)];
    }

    private void testModifiedBinarySearch() {
        mUUIDHashedArray = new String[5];
        mUUIDHashedArray[0] = "A";
        mUUIDHashedArray[1] = "C";
        mUUIDHashedArray[2] = "E";
        mUUIDHashedArray[3] = "G";
        mUUIDHashedArray[4] = "I";
        testModifiedBinarySearch("A", "A");
        testModifiedBinarySearch("B", "C");
        testModifiedBinarySearch("C", "C");
        testModifiedBinarySearch("D", "E");
        testModifiedBinarySearch("E", "E");
        testModifiedBinarySearch("F", "G");
        testModifiedBinarySearch("G", "G");
        testModifiedBinarySearch("H", "I");
        testModifiedBinarySearch("I", "I");
        testModifiedBinarySearch("J", "A");
        testModifiedBinarySearch("Y", "A");

    }

    private void testModifiedBinarySearch(String key, String value) {
        int index = modifiedBinSearch(0, mUUIDHashedArray.length - 1, key);
        if (!mUUIDHashedArray[index].equals(value)) {
            System.out.println("Failed:for:" + key + "::expected:" + value + "::ButWas::" + mUUIDHashedArray[index]);
        } else {
            System.out.println("Success:" + key + "::expected:" + value);
        }
    }

    /**
     * @param low
     * @param high
     * @param key
     * @return index of the value, that is just higher than the key
     */
    private int modifiedBinSearch(int low, int high, String key) {
        if (low >= high) {
            if (high < 0) {
                int compare = key.compareTo(mUUIDHashedArray[0]);
                if (compare < 0) {
                    return mUUIDHashedArray.length - 1;
                }
            } else if (high >= mUUIDHashedArray.length - 1) {
                int compare = key.compareTo(mUUIDHashedArray[high]);
                if (compare > 0) {
                    return 0;
                }
            } else if (low == high) {
                int compare = key.compareTo(mUUIDHashedArray[high]);
                if (compare > 0) {
                    return (high + 1);
                }
            } else if (low > high) {
                int compare = key.compareTo(mUUIDHashedArray[high]);
                if (compare > 0) {
                    return high + 1;
                }
            }
            return high;
        }

        int mid = low + (high - low) / 2;
        int compare = key.compareTo(mUUIDHashedArray[mid]);
        if (compare == 0) {
            return mid;
        } else if (compare > 0) {
            // key is greater, so we need to scan right of mid
            return modifiedBinSearch(mid + 1, high, key);
        } else {
            // key is smaller, so move to left, and find first value greater than key
            return modifiedBinSearch(low, mid - 1, key);
        }
    }

    /**
     * @param count The no of uuid's to generate in sampling
     */
    private void generateUUIDs(int count) {
        mUUIDHashedArray = new String[count];
        for (int i = 0; i < count; i++) {
            mUUIDHashedArray[i] = genHash(UUID.randomUUID().toString());
            //mUUIDHashedArray[i] = UUID.randomUUID().toString();
        }
    }

    private void sortUUIDArray() {
        Arrays.sort(mUUIDHashedArray);
    }

    private void printUUIDArray() {
        for (int i = 0; i < mUUIDHashedArray.length; i++) {
            System.out.println(mUUIDHashedArray[i]);
        }
    }

    private static String genHash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(key.getBytes(StandardCharsets.UTF_8));
            return toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String toHex(byte[] data) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            buffer.append(Character.forDigit(data[i] >> 4 & 0xF, 16));
            buffer.append(Character.forDigit((data[i] & 0xF), 16));
        }
        return buffer.toString();
    }
}
