public enum Provenance {
    INDONESIA_SINGLE_ORIGIN, ETHIOPIAN_SUPERIOR, RWANDA_ORGANIC;

    public String toString() {
        return switch (this) {
            case INDONESIA_SINGLE_ORIGIN ->  "Indonesia Single Origin";
            case ETHIOPIAN_SUPERIOR -> "Ethiopian Superior";
            case RWANDA_ORGANIC ->  "Rwanda Organic";
            //No defaults because exception handling is done at calling methods.
        };
    }
}
