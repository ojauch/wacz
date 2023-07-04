package de.ojauch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
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
