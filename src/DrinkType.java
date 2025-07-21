/**
 * Enum of possible drink types.
 * Has specific toString return values.
 */
public enum DrinkType {
    HOT_COFFEE, FRAPPE;

    /**
     * User-friendly String representations of DrinkType Enums
     * @return String of Enum value in user-friendly format.
     */
    public String toString() {
        return switch (this) {
            case HOT_COFFEE -> "Hot Coffee";
            case FRAPPE -> "Frappe";
            //No defaults because exception handling is done at calling methods.
        };
    }
}
