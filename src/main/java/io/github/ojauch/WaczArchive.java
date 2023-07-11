package io.github.ojauch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class WaczArchive {

    private static final Pattern ARCHIVE_PATTERN = Pattern.compile("archive/.+\\.warc(\\.gz)?");
    private static final Pattern INDEX_PATTERN = Pattern.compile("indexes/.+\\.cdx(\\.gz)?");

    private final File waczFile;

    public WaczArchive(File waczFile) {
        this.waczFile = waczFile;
    }

    /**
     * Assert that the given file is a valid WACZ archive
     *
     * @throws InvalidWaczException if the file is not a valid WACZ archive
     * @throws IOException if the file is not accessible
     */
    public void validate() throws InvalidWaczException, IOException {
        ZipFile waczArchive;
        try {
            waczArchive = getZipFile();
        } catch (ZipException e) {
            throw new InvalidWaczException("File is not a valid zip archive");
        }

        ZipEntry datapackageEntry = waczArchive.getEntry("datapackage.json");
        if (datapackageEntry == null) {
            throw new InvalidWaczException("datapackage.json does not exist");
        }
        validateDatapackage(waczArchive.getInputStream(datapackageEntry));

        ZipEntry pagesEntry = waczArchive.getEntry("pages/pages.jsonl");
        if (pagesEntry == null) {
            throw new InvalidWaczException("pages/pages.jsonl does not exist");
        }
        validatePages(waczArchive.getInputStream(pagesEntry));

        List<ZipEntry> archiveEntries = new ArrayList<>();
        List<ZipEntry> indexEntries = new ArrayList<>();

        for (Enumeration<? extends ZipEntry> e = waczArchive.entries(); e.hasMoreElements();) {
            ZipEntry currentEntry = e.nextElement();

            Matcher archiveMatcher = ARCHIVE_PATTERN.matcher(currentEntry.getName());
            if (archiveMatcher.matches()) {
                archiveEntries.add(currentEntry);
            }

            Matcher indexMatcher = INDEX_PATTERN.matcher(currentEntry.getName());
            if (indexMatcher.matches()) {
                indexEntries.add(currentEntry);
            }
        }

        if (archiveEntries.size() == 0) {
            throw new InvalidWaczException("wacz contains no archives");
        }

        if (indexEntries.size() == 0) {
            throw new InvalidWaczException("wacz contains no indexes");
        }
    }

    /**
     * Get metadata of the wacz archive from the datapackage file
     *
     * @return a wacz metadata object
     * @throws InvalidWaczException if the datapackage was invalid or not found
     * @throws IOException if a file wasn't readable
     */
    public WaczMetadata getMetadata() throws InvalidWaczException, IOException {
        Datapackage datapackage = getDatapackage();

        WaczMetadata.Builder metadataBuilder = new WaczMetadata.Builder();

        if (datapackage.getWaczVersion() != null) {
            metadataBuilder.setWaczVersion(datapackage.getWaczVersion());
        }

        if (datapackage.getTitle() != null) {
            metadataBuilder.setTitle(datapackage.getTitle());
        }

        if (datapackage.getDescription() != null) {
            metadataBuilder.setDescription(datapackage.getDescription());
        }

        if (datapackage.getCreated() != null) {
            metadataBuilder.setCreated(datapackage.getCreated());
        }

        if (datapackage.getModified() != null) {
            metadataBuilder.setModified(datapackage.getModified());
        }

        if (datapackage.getSoftware() != null) {
            metadataBuilder.setSoftware(datapackage.getSoftware());
        }

        if (datapackage.getMainPageUrl() != null) {
            metadataBuilder.setMainPageUrl(datapackage.getMainPageUrl());
        }

        if (datapackage.getMainPageDate() != null) {
            metadataBuilder.setMainPageDate(datapackage.getMainPageDate());
        }

        return metadataBuilder.build();
    }

    /**
     * Verify checksums of datapackage resources
     *
     * @return map with file paths as keys and true if the checksum did match and false otherwise
     * @throws InvalidWaczException if the datapackage was invalid
     * @throws IOException if a file was not found or wasn't readable
     * @throws NoSuchAlgorithmException if the datapackage used a hashing algo that is not supported by the java
     *      platform
     */
    public Map<String, Boolean> verifyChecksums() throws InvalidWaczException, IOException, NoSuchAlgorithmException {
        Map<String, Boolean> checksums = new HashMap<>();

        Datapackage datapackage = getDatapackage();
        ZipFile zipFile = getZipFile();

        if (datapackage.getResources() == null) {
            throw new InvalidWaczException("resources property must be set");
        }

        for (Resource resource : datapackage.getResources()) {
            if (resource.getHash() == null) {
                continue;
            }

            HashValue hashValue = HashingHelper.getHashValue(resource.getHash());

            ZipEntry entry = zipFile.getEntry(resource.getPath());
            InputStream is = zipFile.getInputStream(entry);

            String strDigest = HashingHelper.calculateChecksum(is, hashValue.getAlgorithm());

            checksums.put(resource.getPath(), strDigest.equals(hashValue.getValue()));
        }

        // verify datapackage.json checksum if datapackage-digest.json exists
        ZipEntry datapackageDigestEntry = zipFile.getEntry("datapackage-digest.json");
        if (datapackageDigestEntry != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            DatapackageDigest datapackageDigest = mapper.readValue(zipFile.getInputStream(datapackageDigestEntry),
                    DatapackageDigest.class);

            HashValue hashValue = HashingHelper.getHashValue(datapackageDigest.getHash());

            ZipEntry entry = zipFile.getEntry("datapackage.json");
            InputStream is = zipFile.getInputStream(entry);

            String strDigest = HashingHelper.calculateChecksum(is, hashValue.getAlgorithm());

            checksums.put("datapackage.json", strDigest.equals(hashValue.getValue()));
        }

        return checksums;
    }

    private Datapackage getDatapackage() throws InvalidWaczException, IOException {
        ZipFile zipFile = getZipFile();
        ZipEntry datapackageEntry = zipFile.getEntry("datapackage.json");
        if (datapackageEntry == null) {
            throw new InvalidWaczException("datapackage.json does not exist");
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        Datapackage datapackage;

        try {
            datapackage = mapper.readValue(zipFile.getInputStream(datapackageEntry), Datapackage.class);
        } catch (Exception e) {
            throw new InvalidWaczException("datapackage.json is no valid json");
        }

        return datapackage;
    }

    private void validateDatapackage(InputStream datapackageInputStream) throws InvalidWaczException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        JsonSchema datapackageSchema = factory.getSchema(getClass().getResourceAsStream("/data-package.json"));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(datapackageInputStream);
        } catch (IOException e) {
            throw new InvalidWaczException("failed reading datapackage.json");
        }

        Set<ValidationMessage> validationErrors = datapackageSchema.validate(jsonNode);

        if (!validationErrors.isEmpty()) {
            throw new InvalidWaczException("invalid datapackage");
        }
    }

    private void validatePages(InputStream pagesInputStream) throws InvalidWaczException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(pagesInputStream));
        ObjectMapper mapper = new ObjectMapper();

        try {
            String line = reader.readLine();
            int i = 0;
            while (line != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                JsonNode lineData = mapper.readTree(line);
                if (i == 0) {
                    if (!lineData.has("format")) {
                        throw new InvalidWaczException("pages header has no format key");
                    }
                } else {
                    if (!lineData.has("url")) {
                        throw new InvalidWaczException("Page has no url property");
                    }
                    if (!lineData.has("ts")) {
                        throw new InvalidWaczException("Page has no ts property");
                    }
                }
                i++;
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new InvalidWaczException("Unable to read pages.jsonl");
        }
    }

    private ZipFile getZipFile() throws IOException {
        return new ZipFile(waczFile);
    }

}
