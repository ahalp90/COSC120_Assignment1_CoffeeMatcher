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
    // Declaring appName, menuFilePath and iconPath here inspired by COSC120 Tute 4 solution3_4,
    // FindADog.java, ln20-23.
    private final static String APP_NAME = "Java Bean Order Genie";
    private final static String MENU_FILE_PATH = "./menu.txt";
    private final static String ICON_PATH = "./java_bean.jpg";
    private static ImageIcon icon;


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
        icon = new ImageIcon(ICON_PATH);
        // Error check idea from reading the documentation:
        // https://docs.oracle.com/en/java/javase/24/docs/api/java.desktop/javax/swing/ImageIcon.html#getImageLoadStatus()
        // Discussion on StackOverflow indicates that ImageIcon's failure to load from file will not cause a
        // fatal error--it will just produce a blank placeholder box. StackOverflow source:
        // https://stackoverflow.com/questions/77634130/i-cant-get-the-imageicon-to-display-properly
        if (icon.getImageLoadStatus() == MediaTracker.ERRORED
                || icon.getImageLoadStatus() == MediaTracker.ABORTED) {
            System.err.println("Error: The icon image failed to load. Check that the image is in the path "
            + ICON_PATH + " and that you permission to access this path.");
        }

        loadMainMenu(menu);
    }

    //*********REMEMBER TO SORT COFFEES DISPLAY ON MENU AND ORDERING MENU FROM LEAST EXPENSIVE to MOST EXPENSIVE->
    public static void loadMainMenu(Menu menu) {
        // null check exit app with message.
        String menuDialogString =
                "Welcome to the Java Bean Order Genie."
                        +"\nTake your time to search the menu for the coffee of your dreams."
                        +"\nUse the drop down menu to start making your order or explore our menu."
                        +"\n\nYou can use our custom coffee matcher to describe your ideal coffee, "
                        +"and then view your matching coffees before ordering."
                        +"\nIf haven't described your ideal coffee but still want to order, that's okay."
                        +"\nGo ahead and click 'View my Coffee Matches and Order' anyway.";

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
                showCoffeesMenu(menu);
            }
            case DESCRIBE_COFFEE -> {
//                showDreamCoffeeMaker();
            }
            case VIEW_MATCHES_AND_ORDER -> {
//                showViewMatchesAndOrder();
            }
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
            // Trim leading/trailing whitespace from extras. Explicit *for* iterator to directly
            // modify element values.
            // Retrim as this string was formerly encased in square brackets.
            for (int k = 0; k < extrasStrings.length; k++) {
                extrasStrings[k] = extrasStrings[k].trim();
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
    public static void showCoffeesMenu(Menu menu) {
        StringBuilder allCoffeesStringBuilder = new StringBuilder();
        allCoffeesStringBuilder.append("*****Java Bean Coffee Menu - All Coffees*****\n");
        for (Coffee coffee : menu.getMenu().values()) {
            allCoffeesStringBuilder.append("\n").append(coffee.coffeeDetailsString());
            allCoffeesStringBuilder.append("\n").append("**********");
        }

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
     * @return dreamCoffee, a Coffee object representing the user's desired coffee attributes.
     */
//    public static Coffee getUserDreamCoffee () {
//
//        return;
//    }



//    public static extrasSet deriveExtras(Map<String, coffee>) {
//
//    }
}
