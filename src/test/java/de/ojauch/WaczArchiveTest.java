package de.ojauch;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WaczArchiveTest {
    @Test
    public void testValidateValidArchive() {
        File validWaczFile = new File(getClass().getClassLoader()
                .getResource("valid-example.wacz").getFile());
        WaczArchive archive = new WaczArchive(validWaczFile);
        assertDoesNotThrow(archive::validate);
    }

    @Test
    public void testValidateNoDatapackage() {
        File noDatapackageFile = new File(getClass().getClassLoader()
                .getResource("no-datapackage.wacz").getFile());
        WaczArchive noDatapackageArchive = new WaczArchive(noDatapackageFile);
        assertThrows(InvalidWaczException.class, noDatapackageArchive::validate);
    }

    @Test
    public void testValidateInvalidDatapackage() {
        File invalidDatapackageFile = new File(getClass().getClassLoader()
                .getResource("invalid-datapackage.wacz").getFile());
        WaczArchive invalidDatapackageArchive = new WaczArchive(invalidDatapackageFile);
        assertThrows(InvalidWaczException.class, invalidDatapackageArchive::validate);
    }

    @Test
    public void testValidateNoArchives() {
        File noArchivesFile = new File(getClass().getClassLoader()
                .getResource("no-archives.wacz").getFile());
        WaczArchive noArchivesArchive = new WaczArchive(noArchivesFile);
        assertThrows(InvalidWaczException.class, noArchivesArchive::validate);
    }

    @Test
    public void testValidateNoPages() {
        File noPagesFile = new File(getClass().getClassLoader()
                .getResource("no-pages.wacz").getFile());
        WaczArchive noArchivesArchive = new WaczArchive(noPagesFile);
        assertThrows(InvalidWaczException.class, noArchivesArchive::validate);
    }

    @Test
    public void testGetMetadata() throws Exception {
        File validWaczFile = new File(getClass().getClassLoader()
                .getResource("valid-example.wacz").getFile());
        WaczArchive archive = new WaczArchive(validWaczFile);
        WaczMetadata metadata = archive.getMetadata();

        assertEquals("1.1.1", metadata.waczVersion());
        assertTrue(metadata.title().isPresent());
        assertEquals("valid-example", metadata.title().get());
        assertTrue(metadata.software().isPresent());
        assertEquals("Webrecorder ArchiveWeb.page 0.10.1, using warcio.js 2.1.0", metadata.software().get());
        assertFalse(metadata.description().isPresent());
        assertTrue(metadata.created().isPresent());
        assertTrue(metadata.modified().isPresent());
    }

    @Test
    public void testGetMetadataInvalid() {
        File noDatapackageFile = new File(getClass().getClassLoader()
                .getResource("no-datapackage.wacz").getFile());
        WaczArchive noDatapackageArchive = new WaczArchive(noDatapackageFile);
        assertThrows(InvalidWaczException.class, noDatapackageArchive::getMetadata);
    }

    @Test
    public void testVerifyChecksums() throws Exception {
        File validWaczFile = new File(getClass().getClassLoader()
                .getResource("valid-example.wacz").getFile());
        WaczArchive archive = new WaczArchive(validWaczFile);
        Map<String, Boolean> checksums = archive.verifyChecksums();

        for (String path : checksums.keySet()) {
            assertTrue(checksums.get(path), "Checksum of file " + path + " should match");
        }
    }

    @Test
    public void testVerifyChecksumsInvalidChecksum() throws Exception {
        File invalidWaczFile = new File(getClass().getClassLoader()
                .getResource("invalid-checksum.wacz").getFile());
        WaczArchive archive = new WaczArchive(invalidWaczFile);
        Map<String, Boolean> checksums = archive.verifyChecksums();
        assertFalse(checksums.get("archive/data.warc.gz"));
    }
}
