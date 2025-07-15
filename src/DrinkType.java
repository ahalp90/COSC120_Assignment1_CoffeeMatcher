public enum DrinkType {
    HOT_COFFEE, FRAPPE;

    public String toString() {
        return switch (this) {
            case HOT_COFFEE -> "Hot Coffee";
            case FRAPPE -> "Frappe";


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
