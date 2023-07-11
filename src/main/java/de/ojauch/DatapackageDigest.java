package de.ojauch;

public class DatapackageDigest {
    private String path;
    private String hash;
    private SignedData signedData;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public SignedData getSignedData() {
        return signedData;
    }

    public void setSignedData(SignedData signedData) {
        this.signedData = signedData;
    }
}
