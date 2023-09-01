package es.incidence.library.config;

public class IncidenceLibraryConfig {
    final private String apikey;

    private IncidenceLibraryConfig(String apikeyLoc) {
        this.apikey = apikeyLoc;
    }

    public static class Builder {
        private String apikey;

        public Builder setApikey(String apikey) {
            this.apikey = apikey;
            return this;
        }

        public IncidenceLibraryConfig createIncidenceLibraryConfig() {
            return new IncidenceLibraryConfig(apikey);
        }
    }
}
