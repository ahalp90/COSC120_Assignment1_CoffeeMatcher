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


            // Default statement seems unnecessary?? Ask Alyssa. If the information read in is not
            // exactly correct to match an Enum constant, what do you want the program to do? Should
            // it to fail to read that menu item or the menu.txt file and give an appropriate
            // feedback message? Or should it use a default such as NA and load in the Coffee object
            // anyway? Having a default is important to protect the program from crashing.
            // Alternatively, you might handle any unmatched value within the loadMenu() or
            // getUserCriteria() method }
        };
    }


}
