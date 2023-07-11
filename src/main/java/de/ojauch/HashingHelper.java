package de.ojauch;

import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashingHelper {

    /**
     * Get hash value and algorithm from datapackage hash string
     *
     * @param hashString hash string of a datapackage
     * @return HashValue object with the algorithm and value from the hashString, algorithm is MD5 by default if no
     *  algorithm is specified
     */
    public static HashValue getHashValue(String hashString) {
        String[] hashParts = hashString.split(":", 2);

        String hashAlgo;
        String hashValue;

        if (hashParts.length == 1) {
            hashAlgo = "MD5";
            hashValue = hashParts[0];
        } else {
            hashAlgo = hashParts[0];
            hashValue = hashParts[1];
        }

        return new HashValue(hashAlgo, hashValue);
    }

    /**
     * Calculate the checksum of data from an input stream
     *
     * @param is input stream to read the data from
     * @param algorithm hashing algorithm to use to calculate the checksum
     * @return hex string representation of the checksum
     * @throws NoSuchAlgorithmException if there is no implementation for the specified hashing algorithm
     * @throws IOException if the input stream is not readable
     */
    public static String calculateChecksum(InputStream is, String algorithm)
            throws NoSuchAlgorithmException, IOException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);

        try (DigestInputStream dis = new DigestInputStream(is, messageDigest)) {
            while (dis.read() != -1) {
            }
        }
        byte[] digest = messageDigest.digest();
        return Hex.encodeHexString(digest);
    }
}
