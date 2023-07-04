package de.ojauch;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
