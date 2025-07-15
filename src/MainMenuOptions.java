public enum MainMenuOptions {
    VIEW_FULL_MENU, DESCRIBE_COFFEE, VIEW_MATCHES_AND_ORDER;

    public String toString() {
        return switch (this) {
            case VIEW_FULL_MENU -> "View the Full Menu";
            case DESCRIBE_COFFEE -> "Describe my Ideal Coffee";
            case VIEW_MATCHES_AND_ORDER -> "View my Coffee Matches and Order";

        };
    }
}
