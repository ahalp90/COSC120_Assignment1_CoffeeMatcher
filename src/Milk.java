/**
 * Enum of possible Milk types.
 * Has specific toString return values.
 */
public enum Milk {
    FULL_CREAM, SKIM, SOY, ALMOND, OAT, COCONUT, NONE;

    /**
     * User-friendly String representations of Milk Enums
     * @return String
     */
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