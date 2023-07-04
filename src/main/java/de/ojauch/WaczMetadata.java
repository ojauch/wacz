package de.ojauch;

import java.time.ZonedDateTime;
import java.util.Optional;

public record WaczMetadata(
        String waczVersion,
        Optional<String> title,
        Optional<String> description,
        Optional<ZonedDateTime> created,
        Optional<ZonedDateTime> modified,
        Optional<String> software,
        Optional<String> mainPageUrl,
        Optional<ZonedDateTime> mainPageDate) {

    static class Builder {

        private String waczVersion = null;
        private String title = null;
        private String description = null;
        private ZonedDateTime created = null;
        private ZonedDateTime modified = null;
        private String software = null;
        private String mainPageUrl = null;
        private ZonedDateTime mainPageDate = null;

        public WaczMetadata build() {
            return new WaczMetadata(waczVersion, Optional.ofNullable(title), Optional.ofNullable(description),
                    Optional.ofNullable(created), Optional.ofNullable(modified), Optional.ofNullable(software),
                    Optional.ofNullable(mainPageUrl), Optional.ofNullable(mainPageDate));
        }

        public void setWaczVersion(String waczVersion) {
            this.waczVersion = waczVersion;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setCreated(ZonedDateTime created) {
            this.created = created;
        }

        public void setModified(ZonedDateTime modified) {
            this.modified = modified;
        }

        public void setSoftware(String software) {
            this.software = software;
        }

        public void setMainPageUrl(String mainPageUrl) {
            this.mainPageUrl = mainPageUrl;
        }

        public void setMainPageDate(ZonedDateTime mainPageDate) {
            this.mainPageDate = mainPageDate;
        }
    }
}
