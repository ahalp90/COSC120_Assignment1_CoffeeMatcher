import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuSearcher {
    // appName, menuFilePath and iconPath are constants.
    // Constants have uppercase snake case names:
    // https://www.oracle.com/java/technologies/javase/codeconventions-namingconventions.html.
    // Declaring in field constants and objects referenced by multiple methods inspired by COSC120
    // Tute 4 solution3_4, FindADog.java, ln20-23.
    private final static String APP_NAME = "Java Bean Order Genie";
    private final static String MENU_FILE_PATH = "./menu.txt";
    private final static String ICON_PATH = "./java_bean.jpg";
    // TODO: not final. Do I make the class methods instance to allow this (and other changes)?
    private static ImageIcon icon;
    // Store drinks menu as a field so that it's accessible class-wide; use it as a parameter for
    // GUI menus allowing return to the main menu, without needing to repeatedly explicitly pass the
    // object reference.
    private static Menu menu;


    //**********MAIN METHOD**********
    public static void main(String args[]) {
        Menu menu = loadMenuItems();
        // Handle the exception that somehow, despite specific exception handling at all stages of
        // loadMenuItems(), the Menu object failed to load.
        if (menu == null || menu.getMenu().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "The menu could not be loaded. The program will exit when you exit this dialogue box.",
                    APP_NAME, JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        // Load image to icon for use in GUIs.
        icon = new ImageIcon(ICON_PATH);
        // Error check ImageIcon load idea from reading the documentation:
        // https://docs.oracle.com/en/java/javase/24/docs/api/java.desktop/javax/swing/ImageIcon.html#getImageLoadStatus()
        // Discussion on StackOverflow indicates that ImageIcon's failure to load from file will not cause a
        // fatal error--it will just produce a blank placeholder box. StackOverflow source:
        // https://stackoverflow.com/questions/77634130/i-cant-get-the-imageicon-to-display-properly
        if (icon.getImageLoadStatus() == MediaTracker.ERRORED
                || icon.getImageLoadStatus() == MediaTracker.ABORTED) {
            System.err.println("Error: The icon image failed to load. Check that the image is in the path "
            + ICON_PATH + " and that you permission to access this path.");
        }

        loadMainMenu();
    }

    //*********REMEMBER TO SORT COFFEES DISPLAY ON MENU AND ORDERING MENU FROM LEAST EXPENSIVE to MOST EXPENSIVE->
    public static void loadMainMenu() {
        // TODO null check exit app with message.
        String menuDialogString =
                "Welcome to the Java Bean Order Genie."
                        +"\nTake your time to search the menu for the coffee of your dreams."
                        +"\nUse the drop down menu to start making your order or explore our menu."
                        +"\n\nYou can use our custom coffee matcher to describe your ideal coffee, "
                        +"and then view your matching coffees before ordering."
                        +"\nIf haven't described your ideal coffee but still want to order, that's okay."
                        +"\nGo ahead and click 'View my Coffee Matches and Order' anyway.";

        // Main Menu GUI
        MainMenuOptions menuChoice = (MainMenuOptions) JOptionPane.showInputDialog(
                null,
                menuDialogString,
                APP_NAME,
                JOptionPane.QUESTION_MESSAGE,
                icon,
                MainMenuOptions.values(),
                MainMenuOptions.DESCRIBE_COFFEE);
        // Handle closure of main menu without option selection.
        if  (menuChoice == null) System.exit(0);

        switch (menuChoice) {
            case VIEW_FULL_MENU -> {
                showCoffeesMenu();
            }
            case DESCRIBE_COFFEE -> {
//                getUserDreamCoffee();
            }
            case VIEW_MATCHES_AND_ORDER -> {
//                viewMatchesAndOrder();
            }
            // This default should never be reached in normal program flow.
            default -> throw new IllegalStateException("Unexpected value: " + menuChoice);
        }


    }

    /**
     * Load menu items in the provided txt file into a Menu object, which holds them in a Map collection
     * and provides relevant interaction methods. Expects a txt file where the list of coffees begins on
     * the second line; the first line may be a header or other (unused) information. Each line is
     * understood to represent a coffee and its attributes. All coffees must have at least 10 item fields,
     * providing values for:
     * -menuItemId, menuItemName, price, numOfShots, sugar (yes or no), drinkType,
     * provenance, milkSet, extrasSet and description.
     * Failure to provide any of these values for a coffee,
     * other than a description, will result in the coffee not being added to the app's active menu.
     *
     * Ideas adapted from COSC120 Tute 4 solutions 3_4, FindADog.java,loadDogs() method at ln 249-293.
     *
     * @return a Menu object, which is a collection of coffee objects.
     */
    public static Menu loadMenuItems() {
        // Create Menu (with Map field) to hold coffees.
        Menu menu = new Menu();

        // Error caught messages print to terminal. For user friendliness it may be better to print
        // to a dialog box? But that would require a rethink of program logic, because then the app
        // would need to return to a default screen rather than just System.exit(1).



        // String list for storing each coffee type when read from the menu txt.
        List<String> fileContents = new ArrayList<>();

        // Load file contents.
        try {
            // Load the file
            Path path = Path.of(MENU_FILE_PATH);
            // Reads content of file as a list of line-separated strings.
            // Coffees separated by default and newline characters auto-cleaned.
            fileContents = Files.readAllLines(path);
        } catch (InvalidPathException e) {
            System.err.println("Invalid file path: " + e.getMessage());
            // This error is unrecoverable, as the menu must be loaded for all main app functions. Exit app.
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
            // This error is unrecoverable, as the menu must be loaded for all main app functions. Exit app.
            System.exit(1);
        }


        // Iterate through the coffees stored at each string in the list, skipping the header line String.
        // Continue statements at all element assignment checks to skip adding a coffee with faulty attribute values.
        // Maintain a parallel Set of menuItemIds to ensure unique entries returned to Menu--
        // discard coffees without a unique key.
        Set<String> uniqueCoffeeIds = new HashSet<>();

        //***PATTERNS AND MATCHERS FOR CLEANING TXT INPUT***
        // Regex String and pattern for splitting on commas not between square brackets.
        // Used to split menu item attributes.
        // Regex adapted from response by Nicolas Henneaux on August 3, 2016, at:
        // https://stackoverflow.com/questions/38747776/split-string-by-comma-if-not-within-square-brackets-or-parentheses
        // Pattern has its own .split() method, so no matcher required for splits.
        Pattern patternCommasNotBwSqBrackets = Pattern.compile(",(?![^\\[]*[\\]])");

        Pattern patternSqBracketsAroundString = Pattern.compile("^\\[|\\]$");


        for (int i=1;i<fileContents.size();i++) {
            // Split menu item line into individual array elements.
            // pattern.split() idea from https://www.geeksforgeeks.org/java/java-split-string-using-regex/
            String[] elements =
                    patternCommasNotBwSqBrackets.split(fileContents.get(i));
            //Remove all leading and trailing whitespace before processing.
            for (int element = 0; element < elements.length; element++) {
                elements[element] = elements[element].trim();
            }
            // Ensure all the index positions used to check the coffee's attributes are within the
            // bounds of the elements string array.
            if (elements.length != 10) {
                System.err.println("Error: The coffee at item line " + (i + 1) + " is invalid."
                        + "All coffees must have 10 item fields, providing values for:"
                        + "menu item ID, menu item name, price, numberOfShots, sugar, type, provenance, "
                        + "milk, extras, and description."
                        + "\nThis coffee has not been added to the menu.");
                continue; //Skip adding this coffee entirely
            }

            //**********MENU ITEM ID**********
            // Ensure the coffee has an ID, and that it's unique.
            // A regex check might be appropriate here too, but any unique string will work for mapping the coffee.
            String menuItemId;
            if (!elements[0].isEmpty() && !uniqueCoffeeIds.contains(elements[0])) {
                menuItemId = elements[0];
                uniqueCoffeeIds.add(menuItemId); // Add coffee ID to unique check.
            } else {
                System.err.println("Error: The coffee at item line " + (i + 1) + " is invalid. "
                        + "All coffees need a unique menu item ID."
                        + "\nThis coffee has not been added to the menu.");
                continue; // Skip adding this coffee entirely.
            }

            //**********MENU ITEM NAME**********
            String menuItemName;
            if (!elements[1].isEmpty()) {
                menuItemName = elements[1];
            } else {
                System.err.println("Error: The coffee at item line " + (i + 1) + " is invalid. "
                        + "All coffees need a name."
                        + "\nThis coffee has not been added to the menu.");
                continue; // Skip adding this coffee entirely.
            }
            //**********PRICE**********
            float price;
            try {
                price = Float.parseFloat(elements[2]);
            } catch (NumberFormatException e) {
                System.err.println("Error: The price at item line " + (i + 1) + " is invalid. Details: " + e.getMessage()
                        + "\nThis coffee has not been added to the menu.");
                continue; // Skip adding this coffee entirely.
            }
            //**********NUMBER OF SHOTS**********
            int numOfShots;
            try {
                numOfShots = Integer.parseInt(elements[3]);
            } catch (NumberFormatException e) {
                System.err.println("Error: The number of shots at item line " + (i + 1) + " is invalid. Details: "
                        + e.getMessage()
                        + "\nThis coffee has not been added to the menu.");
                continue; // Skip adding this coffee entirely.
            }

            //**********SUGAR**********
            boolean sugar;
            if ("yes".equalsIgnoreCase(elements[4])) {
                sugar = true;
            } else if ("no".equalsIgnoreCase(elements[4])) {
                sugar = false;
            } else {
                System.err.println("Error: The sugar status of the coffee at item line "
                        + (i + 1) + " is invalid. Sugar should be 'yes' or 'no', not '"
                        + elements[4] + "'."
                        + "\nThis coffee has not been added to the menu.");
                continue; // Skip adding this coffee entirely.
            }

            //**********DRINK TYPE**********
            DrinkType drinkType = null;

            // Iterate through the DrinkType Enum values and see if the provided string matches their toString().
            // Assign this parameter value based on any found equivalence; if no equivalence is found,
            // then the local variable will remain null. The provided drinkType value was not in the Enum,
            // and the exception should be handled accordingly.
            // This logic is repeated for Provenance and Milk equivalences.
            // *I would be inclined to write a helper method if there were further needs for this operation.*
            // However,the substantial difference in the structure for Milk (nested loops) meant it would be more
            // confusing than it's worth in that context, and do little to reduce code bloat.
            for (DrinkType drink : DrinkType.values()) {
                if (elements[5].equalsIgnoreCase(drink.toString())) {
                    drinkType = drink;
                    break;
                }
            }
            if (drinkType == null) {
                System.err.println("Error: The drink type of the coffee at item line " + (i + 1)
                        + " is invalid. Type should be 'hot coffee' or 'frappe', not '"
                        + elements[5] + "'."
                        + "\nThis coffee has not been added to the menu.");
                continue; // Skip adding this coffee entirely.
            }


            //**********PROVENANCE**********
            Provenance provenance = null;
            for (Provenance p : Provenance.values()) {
                if (elements[6].equalsIgnoreCase(p.toString())) {
                    provenance = p;
                    break;
                }
            }
            if (provenance == null) {
                System.err.println("Error: The provenance of the coffee at item line " + (i + 1)
                        + " is invalid. Provenance should be 'Indonesia Single Origin', "
                        + "Ethiopian Superior, or 'Rwanda Organic', not '"
                        + elements[6] + "'."
                        + "\nThis coffee has not been added to the menu.");
                continue; // Skip adding this coffee entirely.
            }


            //**********MILK**********
            Set<Milk> milkSet = new HashSet<>();

            // Remove all "[" at the start of the string, or "]" at the end of the string.
            //
            // Matcher idea taken from consistent use in COSC120 tutorial examples (eg Tute 4
            // Answers 3_4, FindADog.Java lines 156-159), my discovery that Pattern has no
            // replaceAll() method
            // (https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html), and the
            // discussion here:
            // https://stackoverflow.com/questions/1466959/string-replaceall-vs-matcher-replaceall-performance-differences
            String milkStringsWithSqBrackets = elements[7];
            Matcher milkMatcher = patternSqBracketsAroundString.matcher(milkStringsWithSqBrackets);
            String noBracketsMilks = milkMatcher.replaceAll("");

            String[] milkStrings = noBracketsMilks.split(",");
            // Trim leading/trailing whitespace from milks. Explicit *for* iterator to directly
            // modify element values.
            // Retrim as this string was formerly encased in square brackets.
            for (int j = 0; j < milkStrings.length; j++) {
                milkStrings[j] = milkStrings[j].trim();
            }

            // If the milkStrings array only has one String, and the String is empty, then this must
            // be a coffee without milk.
            if ((milkStrings.length == 1) && milkStrings[0].isEmpty()) {
                milkSet.add(Milk.NONE);
            } else {
                // Flag variable to ensure all milks were valid
                int milksToCheck = milkStrings.length;

                // Iterate through all milks in the string array, and within that all enum values.
                // Check that the milk string is equivalent to the Milk enum's toString text.
                // Add it to the Set of milks relevant to this coffee and decrement the flag.
                for (String milkTxt : milkStrings) {
                    for (Milk milkEnum : Milk.values()) {
                        if (milkTxt.equalsIgnoreCase(milkEnum.toString())) {
                            milkSet.add(milkEnum);
                            milksToCheck--;
                            // Move onto the next milkTxt in milkStrings, avoiding iterating through
                            // all Milk enum string equivalences.
                            break;
                        }
                    }
                }
                if (milksToCheck != 0) {
                    System.err.println("Error: One or more milk names of the coffee at item line " + (i + 1)
                            + " are invalid. Milk types may only be 'Full-cream', 'Skim', 'Soy', "
                            + "'Almond', 'Oat' or 'Coconut'. Coffees may also have an empty entry "
                            + "in this field for no-milk."
                            + "\nThis coffee has not been added to the menu.");
                    continue; // Skip adding this coffee entirely.
                }
            }

            //**********EXTRAS**********
            Set<String> extrasSet = new HashSet<>();

            // Pattern Matcher surrounding square bracket removal as for milk attributes above.
            String extrasStringsWithSqBrackets = elements[8];
            Matcher extrasMatcher = patternSqBracketsAroundString.matcher(extrasStringsWithSqBrackets);
            String noBracketsExtras = extrasMatcher.replaceAll("");


            String[] extrasStrings = noBracketsExtras.split(",");


            // Format the Strings so that each word starts with a capital and following letters
            // are lower-case; reduce likelihood of adding equivalent Strings to Set with different
            // word formatting or capitalisations.
            // Retrim before formatting, as this string was formerly encased in square brackets.
            // Explicit *for* iterator to directly modify element values.
            for (int k = 0; k < extrasStrings.length; k++) {
                extrasStrings[k] = capitaliseFirstLettersOnly(extrasStrings[k].trim());
            }

            // Add extras to local set. Coffees without extras are likely deliberate and should be
            // added as is (with an empty extras set).
            for (String extra : extrasStrings) {
                // Add only meaningful extras, skipping any empty Strings within the extras bracketed list.
                if (!extra.isEmpty()) extrasSet.add(extra);
            }

            //**********DESCRIPTION**********
            // Pattern Matcher surrounding square bracket removal as for milk attributes above.
            String descriptionStringsWithSqBrackets = elements[9];
            Matcher descriptionMatcher = patternSqBracketsAroundString.matcher(descriptionStringsWithSqBrackets);
            // Retrim as this string was formerly encased in square brackets.
            String description = descriptionMatcher.replaceAll("").trim();

            // Handle the exception of an empty description. This has no serious impact on geeks
            // searching for coffee matches, and its substance should be able to be inferred from
            // the other attributes. Therefore, a value exception here should not prevent a coffee
            // from being added.
            if (description.isEmpty()) {
                description = "No description provided.";
            }

            Coffee coffee = new Coffee(
                    menuItemId,
                    menuItemName,
                    price,
                    numOfShots,
                    sugar,
                    drinkType,
                    provenance,
                    milkSet,
                    extrasSet,
                    description);


            menu.addCoffee(coffee);
        }

        return menu;
    }

    /**
     * Show a menu of all available coffees.
     *
     * JTextArea and JScrollPane code adapted from GETah's response of Dec 4, 2011 at:
     * https://stackoverflow.com/questions/8375022/joptionpane-and-scroll-function
     * This code creates a JTextArea, assigns its text to the value of the string built using
     * StringBuilder, then assigns the value of the JTextArea to a JScrollPane object which has a scrollbar.
     * Then it sets attributes of the text area and JScrollPane.
     * Finally, it presents JOptionPane nested with the contents of the JScrollPane
     * (which itself nests the JTextArea's contents).
     *
     * @param menu is the Menu object which holds a collection (Map) of all Coffee objects.
     */
    public static void showCoffeesMenu() {
        // String - all coffee descriptions, line and asterisk-line separated.
        StringBuilder allCoffeesStringBuilder = new StringBuilder();
        allCoffeesStringBuilder.append("**********Java Bean Coffee Menu - All Coffees**********\n");
        for (Coffee coffee : menu.getMenu().values()) {
            allCoffeesStringBuilder.append("\n").append(coffee.coffeeDetailsString())
            .append("\n").append("\t**********");
        }

        // All coffees menu GUI with scrollbar.
        JTextArea textArea = new JTextArea(allCoffeesStringBuilder.toString());
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize(new Dimension(500,500 ) );
        JOptionPane.showOptionDialog(null, scrollPane, APP_NAME,JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, icon, null, null);
    }
    /**
     * a method that requests user input/selection of coffee features e.g.  type [hot coffee/frappe], milk
     * type (including a no-milk option), number of shots, sugar [yes/no], price range, and extras),
     *
     * Assignment and exception handling code adapted from COSC120 Tute 4 solutions 3_4,
     * FindADog.java getUserCriteria() method, ln167-243.
     *
     * @return dreamCoffee, a Coffee object representing the user's desired coffee attributes.
     */
    public static Coffee getUserDreamCoffee () {
        //**********GET DRINK TYPE**********
        DrinkType drinkType = (DrinkType) JOptionPane.showInputDialog(null,
                "For starters, what sort of drink would you like? Hot Coffee or Frappe?",
                APP_NAME, JOptionPane.QUESTION_MESSAGE, icon, DrinkType.values(), null);
        if  (drinkType == null) loadMainMenu();

        //**********GET MILK SET**********
        // User only selects one milk--milk choices are usually exclusive preferences.
        // IntelliJ prompted wrapping the assignment in Collections.singleton() to assign the Enum
        // value to my desired Collection type. I did some digging and decided EnumSet.of() was a
        // better option in case the program design changes to require muteability of the user's
        // milk choices. Sources:
        // https://www.geeksforgeeks.org/java/collections-singleton-method-java/ &
        // https://www.geeksforgeeks.org/java/enumset-of-method-in-java/

        Milk selectedMilk = (Milk) JOptionPane.showInputDialog(null,
                "What sort of milk would you like?",
                APP_NAME, JOptionPane.QUESTION_MESSAGE, icon, Milk.values(), null);
        if  (selectedMilk == null) loadMainMenu();

        //Necessary to send to Set as the Coffee parameters expect a Set.
        Set<Milk> milkSet = EnumSet.of(selectedMilk);

        //**********GET PRICE MIN**********
        // Set min and max price fields for looping and ensuring priceMax>priceMin.
        float priceMin = -1,  priceMax = -1;

        // null-checkable input var idea adapted from my solution to COSC120 Tute 4, FindADog.java
        // ln112-129.
        String priceMinInput; //Declared outside the loop to avoid recreating the object.
        do {
            // The expanded JOptionPane with icon placement returns an object, not String. Requires
            // explicit casting. Idea from response by selofain, Nov 20, 2019, at
            // https://stackoverflow.com/questions/33961793/custom-icon-joptionpane-showinputdialog
            priceMinInput = (String) JOptionPane.showInputDialog(null,
                    "What's the minimum you'd like to spend on your drink?",
                    APP_NAME, JOptionPane.QUESTION_MESSAGE, icon, null, null);
            if (priceMinInput == null) {
                loadMainMenu();
                break; //End loop because exiting to different GUI method.
            }
            try {
                priceMin = Float.parseFloat(priceMinInput);
                if (priceMin < 0) {
                    JOptionPane.showMessageDialog(null,
                            "Sorry, input must be in whole digit or decimal format, eg. 3.5\n"
                            +"Please try again.", APP_NAME, JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Sorry, input must be in whole digit or decimal format, eg. 3.5\n"
                                +"Please try again.", APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        } while(priceMin < 0);         // Allow 0 min price.

        //**********GET PRICE MAX**********
        String priceMaxInput;
        do {
            priceMaxInput = (String) JOptionPane.showInputDialog(null,
                    "What's the maximum you'd like to spend on your drink?",
                    APP_NAME, JOptionPane.QUESTION_MESSAGE, icon, null, null);
            if (priceMaxInput == null) {
                loadMainMenu();
                break; //End loop because exiting to different GUI method.
            }
            try {
                priceMax = Float.parseFloat(priceMaxInput);
                if (priceMin < priceMax) {
                    JOptionPane.showMessageDialog(null,
                            "Sorry, your maximum price must be equal to or larger than your minimum.\n"
                                    +"Please try again.", APP_NAME, JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Sorry, input must be in whole digit or decimal format, eg. 4.5\n"
                                +"Please try again.", APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        } while(priceMax < priceMin);

        //**********GET EXTRAS**********

        // Create array of menu extras set with "Skip" option.
        // TreeSet sorts alphabetically (consistent user experience choice), and then LinkedHashSet
        // preserves insertion order--so 'Skip' and 'Finished Adding Extras' can sit at the end of
        // the Collection (and subsequent option dropdown choices).
        Set<String> extrasSetWithMenuOptions = new TreeSet<>(menu.getAllMenuExtras());  // Doesn't actually contain menu options yet.
        extrasSetWithMenuOptions = new LinkedHashSet<>(extrasSetWithMenuOptions);
        if (extrasSetWithMenuOptions.contains("Skip") || extrasSetWithMenuOptions.contains("Finished Adding Extras")) {
            System.err.println(
                    "Extras set read from menu.txt contained extras identified as 'Skip' or "
                            +"'Finished Adding Extras'. These explicit extra selectors have been "
                            +"suppressed in favour of the relevant actions in user dreamCoffee extras selection.");
        }
        extrasSetWithMenuOptions.add("Skip");
        extrasSetWithMenuOptions.add("Finished Adding Extras");
        // toArray() requires explicit return type for the array type required. Though the COSC 120
        // Tute 4 3_4 FindADog.java ln170 example indicates it's not required if called in
        // JOptionPane parameters.
        String[] extrasSelectionArray = extrasSetWithMenuOptions.toArray(new String[0]);

        Set<String> extrasSet = new HashSet<>(); // Hold all user extras choices.
        String extraToAddToSet; // Container for individual extra selections.
        // Labelled loop to break from within switch. Idea from betteroutthanin's response of April 2, 2014:
        // https://stackoverflow.com/questions/22823395/java-how-can-i-break-a-while-loop-under-a-switch-statement
        extrasLoop: while (true) {
            extraToAddToSet = (String) JOptionPane.showInputDialog(null,
                    "Which extras would you like?\n"
                            + "\nWhen you're done, select Finished Adding Extras' to move to your dream coffee's next attribute."
                            + "\nSelect 'Skip' to skip adding any extras preferences; " +
                            "this will also clear any extras you chose before changing your mind.",
                    APP_NAME, JOptionPane.QUESTION_MESSAGE, icon, extrasSelectionArray, null);

            switch (extraToAddToSet) {
                case null -> {
                    loadMainMenu();
                    break extrasLoop; //End loop because exiting to different GUI method.
                }

                case "Skip" -> {
                    extrasSet.clear();
                    break extrasLoop;
                }

                case "Finished Adding Extras" -> {
                    break extrasLoop;
                }

                default -> {
                    if (extrasSet.contains(extraToAddToSet)) {
                        JOptionPane.showMessageDialog(null,
                                "You've already selected that extra. Extras that you've selected include: "
                                        + String.join(", ", extrasSet) + "\nPlease try again.",
                                APP_NAME, JOptionPane.INFORMATION_MESSAGE, icon);
                    } else {
                        extrasSet.add(extraToAddToSet);
                    }
                }
            }
        }

        //**********GET PROVENANCE**********
        Provenance provenance = (Provenance) JOptionPane.showInputDialog(null,
                "Terroir is important. What's the provenance of the beans you're after?",
                APP_NAME, JOptionPane.QUESTION_MESSAGE, icon, Provenance.values(), null);
        if  (provenance == null) loadMainMenu();

        //**********GET NUMBER OF SHOTS**********
        // Largely repeats get price min code.
        int numOfShots = -1; //Placeholder value used for looping.

        String numOfShotsInput;
        do {
            numOfShotsInput = (String) JOptionPane.showInputDialog(null,
                    "How many shots of coffee would you like?",
                    APP_NAME, JOptionPane.QUESTION_MESSAGE, icon, null, null);
            if (numOfShotsInput == null) {
                loadMainMenu();
                break; //End loop because exiting to different GUI method.
            }
            try {
                numOfShots = Integer.parseInt(numOfShotsInput);
                if (numOfShots < 0) {
                    JOptionPane.showMessageDialog(null,
                            "Sorry, the number of shots cannot be negative!\n"
                                    +"Please try again.", APP_NAME, JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Sorry, input must be in whole numbers, eg. 2\n"
                                +"Please try again.", APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        } while(numOfShots < 0);         // Allow 0 min shots in case non-caffeinated drinks are sold.

        //**********GET SUGAR**********
        String[] sugarOptions = {"yes", "no"};
        String sugarString = (String) JOptionPane.showInputDialog(null,
                "Would you like sugar?",
                APP_NAME, JOptionPane.QUESTION_MESSAGE, icon, sugarOptions, "no");
        if (sugarString==null) loadMainMenu();
        boolean sugar = sugarString.equals("yes"); // boolean initialises to false otherwise.

        Coffee dreamCoffee = new Coffee("","",-1, numOfShots, sugar, drinkType, provenance, milkSet, extrasSet, "");
        dreamCoffee.setPriceMin(priceMin);
        dreamCoffee.setPriceMax(priceMax);
        return dreamCoffee;
    }

    public static Coffee viewMatchesAndOrder(Coffee dreamCoffee){
        Set<Coffee> matches = new HashSet<>(menu.coffeeMatcher(dreamCoffee));
        StringBuilder viewMatchesSB =  new StringBuilder();
        viewMatchesSB.append("**********Java Bean Coffee Matcher and Ordering System**********\n\n");
        if (matches.isEmpty()) {
            viewMatchesSB.append("No matching coffee found.\n\n"
                    +"But feel free to order anything you like off the drop-down menu below.");
        } else {
            viewMatchesSB.append("Congratulations, you've matched!")
                    .append("\nYour coffee matches are shown below.")
                    .append("\nYou can order one of your matches from the drop-down list at the bottom of this window."
                            +"\n\n If you'd like to order a coffee you haven't yet matched with, "
                            +"these can also be selected for order.");
            for (Coffee coffee : matches) {
                viewMatchesSB.append("\n").append(coffee.coffeeDetailsString())
                .append("\n").append("\t**********");
            }
        }

        //Coffee names String[] for drop-down menu selection.
        String [] coffeeNamesAndId = new String[menu.getMenu().size()]; // Initiate with space for all menu coffees.
        // foreach with external counter to simultaneously iterate through Array and Map. Idea from
        // https://www.tutorialspoint.com/java-program-to-convert-collection-into-array
        int coffeeNamesAndIdIndex = 0;
        for (Coffee coffee : menu.getMenu().values()) {
            // Add Ids after plain language names, as these are the unique identifiers and could
            // potentially help people ordering if multiple coffees of the same plain language name
            // exist.
            coffeeNamesAndId[coffeeNamesAndIdIndex] = coffee.getMenuItemName() + " - " + coffee.getMenuItemID();
            coffeeNamesAndIdIndex++;
        }
        Arrays.sort(coffeeNamesAndId); // Present coffees in alphabetical order.

        // All coffees menu GUI with scrollbar.
        JTextArea textArea = new JTextArea(viewMatchesSB.toString());
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize(new Dimension(500,500 ) );
        String selectedCoffeeNameAndId =
                (String) JOptionPane.showInputDialog(null, scrollPane, APP_NAME,
                        JOptionPane.INFORMATION_MESSAGE, icon, coffeeNamesAndId, null);

        // Reverse the assignment of the coffee's ID to the selector string to retrieve it from the Menu's Map.
        String[] selectedCoffeeStringArray = selectedCoffeeNameAndId.split(" ");
        String selectedCoffeeID = selectedCoffeeStringArray[selectedCoffeeStringArray.length -1];

        Coffee selectedCoffee = menu.getMenu().get(selectedCoffeeID);
        return selectedCoffee;
    }


    public static Geek getUserInfo(){}

    /**
     * Capitalise the first letter of each word in a string, and send other letters to lowercase.
     *
     * Adapted with minor modification from this tutorial:
     * https://www.geeksforgeeks.org/java/java-program-to-capitalize-the-first-letter-of-each-word-in-a-string/
     * @param input the string to be modified
     * @return the string in the desired (capitalised[0]lowercase[1:]) format.
     */
    public static String capitaliseFirstLettersOnly(String input) {
        // Don't operate on null or empty strings.
        if (input == null || input.isEmpty()) return input;

        String[] words = input.split("\\s+"); // Split words on whitespace of any length

        // Rebuild words in string, splitting char0 and the rest of the word.
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            sb.append(Character.toUpperCase(word.charAt(0)));
            // Only apply to words with more than one letter. Avoid possible IndexOutOfBoundsException.
            if (word.length() > 1) sb.append(word.substring(1).toLowerCase());
            sb.append(" ");
        }

        return sb.toString().trim();
    }

}
