package io.github.ojauch;

public class HashValue {
    private final String algorithm;
    private final String value;

    public HashValue(String algorithm, String value) {
        this.algorithm = algorithm;
        this.value = value;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getValue() {
        return value;
    }
}
