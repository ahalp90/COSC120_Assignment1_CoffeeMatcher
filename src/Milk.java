public enum Milk {
    FULL_CREAM, SKIM, SOY, ALMOND, OAT, COCONUT, NONE;

    public String toString() {
        return switch (this) {
            case FULL_CREAM -> "Full-cream";
            case SKIM -> "Skim";
            case SOY -> "Soy";
            case ALMOND -> "Almond";
            case OAT -> "Oat";
            case COCONUT -> "Coconut";
            case NONE -> "No Milk";
            //No defaults because exception handling is done at calling methods.
        };
    }

}