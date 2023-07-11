# WACZ Java library

This java library is used to validate WACZ archives and read their metadata.

## Usage

```java
WaczArchive waczArchive = new WaczArchive("archive.wacz");
try {
    waczArchive.validate();
    System.out.println("Wacz archive is valid");
} catch (InvalidWaczException e) {
    System.out.println("Wacz archive is invalid");
    System.out.println("Reason: " + e.getMessage());
}

Map<String, Boolean> checksums = waczArchive.verifyChecksums();
WaczMetadata metadata = waczArchive.getMetadata();
```