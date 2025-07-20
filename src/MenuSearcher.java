import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Make all mutable types protected. Final/private, and ensure all returned mutables return a copy rather than the original object******
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
    private static final ImageIcon ICON = loadIcon();
    // Store drinks menu as a field so that it's accessible class-wide; use it as a parameter for
    // GUI menus allowing return to the main menu, without needing to repeatedly explicitly pass the
    // object reference.
    // Initialise it directly with loadMenuItems() to make it final.
    private static final Menu menu = loadMenuItems();
    //Initialise coffee to support passing as parameter in mainMenuGui switches.
    private static Coffee dreamCoffee;


    //**********MAIN METHOD**********
    public static void main(String args[]) {
        // Handle the exceptions that (1) the Menu menu has no values, or (2) the Menu menu loaded empty
        // set; error-checking through loadMenuItems() suggests this would be because the provided
        // menu.txt contained no coffees below the header line.
        if (MenuSearcher.menu == null || MenuSearcher.menu.getMenu().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "The menu could not be loaded. The program will exit when you close this dialogue box.",
                    APP_NAME, JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }


        mainMenuGui();
    }

    //TODO*************************REMEMBER TO SORT COFFEES DISPLAY ON MENU AND ORDERING MENU FROM LEAST EXPENSIVE to MOST EXPENSIVE->
    private static void mainMenuGui() {
        String menuDialogString =
                "Welcome to the Java Bean Order Genie."
                        + "\n\nUse the drop down menu to start making your order or explore our menu."
                        + "\n\nYou can use our custom coffee matcher to describe your ideal coffee, "
                        + "and then 'View my Coffee Matches and Order' to order."
                        + "\n\nIf haven't described your ideal coffee but still want to order, that's okay."
                        + "\nJust go ahead and click 'Order Any Item Off the Menu";

        String[] mainMenuOptions =
                {"View the Full Menu", "Describe my Ideal Coffee", "View my Coffee Matches and Order",
                        "Order Any Item Off the Menu"};


        // switch loop because otherwise continuing the program after returning a value from a menu
        // case would require extra manual handling. Idea from
        // https://stackoverflow.com/questions/34928182/how-to-make-a-java-main-menu-loop-after-using-a-case
        mainMenuLoop:
        while (true) {
            // Main Menu GUI
            String menuChoice = (String) JOptionPane.showInputDialog(
                    null,
                    menuDialogString,
                    APP_NAME,
                    JOptionPane.QUESTION_MESSAGE,
                    ICON,
                    mainMenuOptions,
                    "Describe my Ideal Coffee");

            switch (menuChoice) {
                case null -> {
                    System.out.println("Program exited successfully from Main Menu.");
                    System.exit(0); //break loop unnecessary, since we're ending.
                }
                case "View the Full Menu" -> {
                    showCoffeesMenu();
                }
                case "Describe my Ideal Coffee" -> {
                    dreamCoffee = getUserDreamCoffee();
                }
                case "View my Coffee Matches and Order" -> {
                    if (dreamCoffee != null) {
                        //update dreamCoffee to take the attributes of the returned coffeeToOrder
                        dreamCoffee = viewMatchesAndOrder(dreamCoffee);
                        String orderOutString = createGeekAndOrderParent();
                        showCustomerOrder(orderOutString);
                    }
                }
                case "Order Any Item Off the Menu" -> {
                    dreamCoffee = orderAnyCoffeeParent();
                    String orderOutString = createGeekAndOrderParent();
                    showCustomerOrder(orderOutString);
                }
                // This default should never be reached in normal program flow.
                default -> {
                    System.err.println("Unexpected value at main menu selection: " + menuChoice);
                    System.exit(1);
                }
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
     * <p>
     * Ideas adapted from COSC120 Tute 4 solutions 3_4, FindADog.java,loadDogs() method at ln 249-293.
     *
     * @return a Menu object, which is a collection of coffee objects.
     */
    private static Menu loadMenuItems() {
        // Create Menu (with Map field) to hold coffees.
        Menu menu = new Menu();

        // String List for storing each coffee (unknown qty) when read from the menu txt.
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


        for (int i = 1; i < fileContents.size(); i++) {
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
            // Sugar appears currently input as all lower case in menu txt, but should be formatted as
            // 'Yes'/'No' for user experience in app.
            if ("Yes".equalsIgnoreCase(elements[4])) {
                sugar = true;
            } else if ("No".equalsIgnoreCase(elements[4])) {
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
     * <p>
     * Build String of the coffees' details. Calls on helper Coffee.coffeeDetailsString()
     * and then formats as relevant. Display these in coffee menu GUI
     *<p>
     * JTextArea and JScrollPane code adapted from GETah's response of Dec 4, 2011 at:
     * https://stackoverflow.com/questions/8375022/joptionpane-and-scroll-function
     * This code creates a JTextArea, assigns its text to the value of the string built using
     * StringBuilder, then assigns the value of the JTextArea to a JScrollPane object which has a scrollbar.
     * Then it sets attributes of the text area and JScrollPane.
     * Finally, it presents JOptionPane nested with the contents of the JScrollPane
     * (which itself nests the JTextArea's contents).
     */
    private static void showCoffeesMenu() {
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
        scrollPane.setPreferredSize(new Dimension(500, 500));
        int guiClosedCheck = JOptionPane.showOptionDialog(null, scrollPane, APP_NAME, JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, ICON, null, null);

        // -1 is close menu button, 0 is 'ok' button.
        if (guiClosedCheck == -1 || guiClosedCheck == 0) {
            mainMenuGui();
            return; // Superfluous, but good reminder to explicitly end method.
        }
    }

    /**
     * Requests user input/selection of coffee features e.g.  type [hot coffee/frappe], milk
     * type (including a no-milk option), number of shots, sugar [Yes/No], price range, and extras),
     * Calls a series of helper methods.
     * <p>
     * Assignment and exception handling code adapted from COSC120 Tute 4 solutions 3_4,
     * FindADog.java getUserCriteria() method, ln167-243.
     *
     * @return dreamCoffee, a Coffee object representing the user's desired coffee attributes.
     */
    private static Coffee getUserDreamCoffee() {
        DrinkType drinkType = getDreamDrinkType();
        Set<Milk> milkSet = getDreamMilkSet();
        // Declare priceMin in calling method to ensure priceMax>priceMin. -1 initialisation for looping.
        float priceMin = -1;
        priceMin = getDreamPriceMin(priceMin); // Assign to pre-initialised local float.
        float priceMax = getDreamPriceMax(priceMin);
        Set<String> extrasSet = getDreamExtrasSet();
        Provenance provenance = getDreamProvenance();
        int numOfShots = getDreamNumOfShots();
        String sugarString = getDreamSugar();
        // boolean initialises to false otherwise. No Try/Catch or null check because the output is known.
        boolean sugar = sugarString.equalsIgnoreCase("Yes");

        Coffee dreamCoffee = new Coffee("", "", -1, numOfShots, sugar, drinkType, provenance, milkSet, extrasSet, "");
        dreamCoffee.setPriceMin(priceMin);
        dreamCoffee.setPriceMax(priceMax);

        return dreamCoffee;
    }

//TODO FINISH THIS JAVADOC
//TODO FIX ASSIGNMENT OF EXTRAS AND MILK FOR NON-DREAM COFFEE
//TODO IMPROVE SELECTION OF DREAM COFFEES ? PUT THEM FIRST IN ARRAY LIST AND DEFAULT TO FIRST MATCH COFFEE CHOICE?

    /**
     * GUI for viewing coffee matches and ordering a matched coffee.
     *
     * @param dreamCoffee Coffee passed on to helper method coffeeMatcher() for
     *                    deriving Set of matched coffees.
     * @return the Coffee selected by the customer from the GUI, or null if there were no matches.
     * The returned coffee has the following parameters dynamically set:
     * (1) the milk preference of the customer, and
     * (2) the intersection of the menu coffee's extras and the customer's indicated extras preferences
     * (no preference = accept all menu extras)
     */
    private static Coffee viewMatchesAndOrder(Coffee dreamCoffee) {
        Set<Coffee> matches = new HashSet<>(menu.coffeeMatcher(dreamCoffee)); // Helper method in Menu matches the coffees.
        String coffeeMatcherText = buildCoffeeMatchString(matches); // Helper method builds String.
        Coffee[] matchedCoffeesArray = matches.toArray(new Coffee[0]);
        Arrays.sort(matchedCoffeesArray, Comparator.comparing(coffee -> coffee.getMenuItemName()));


        // All coffees menu GUI with scrollbar.
        JTextArea textArea = new JTextArea(coffeeMatcherText);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize(new Dimension(500, 500));


        Coffee selectedCoffee =
                (Coffee) JOptionPane.showInputDialog(null, scrollPane, APP_NAME,
                        JOptionPane.INFORMATION_MESSAGE, ICON,
                        matchedCoffeesArray, null);
        if (selectedCoffee == null) {
            mainMenuGui();
            return null;
        }


        // Explicitly define parameters because milkSet comes from dreamCoffee and extrasSet is a
        // derived value of the exclusive intersection of dreamCoffee's and the Menu Coffee
        // selectedCoffee's extrasSets.
        Coffee coffeeToOrder =  new Coffee(
                selectedCoffee.getMenuItemId(),
                selectedCoffee.getMenuItemName(),
                selectedCoffee.getPrice(),
                selectedCoffee.getNumOfShots(),
                selectedCoffee.getSugar(),
                selectedCoffee.getDrinkType(),
                selectedCoffee.getProvenance(),
                dreamCoffee.getMilkSet(),
                dreamCoffee.overlapExtrasSet(selectedCoffee),
                selectedCoffee.getDescription()
        );
        return coffeeToOrder;
    }

    /**
     * Obtain the user's info via GUI.
     * <p>
     * Adapted from COSC120 Tute 4 solution 3_4, FindADog.java, ln82-160. Regex patterns found therein.
     * However,regex Pattern compilations moved to this calling method to avoid recompilation in helpers at each loop.
     *
     * @return geekOrdering, a Geek record with the user's name, email and phone number.
     */
    private static Geek getUserInfo() {
        //**********GET USER NAME**********
        // A regex pattern to check that the input is two words, separated by a space. Word1 Letter1
        // uppercase and following letters lowercase. Word2 must begin with an uppercase, though
        // subsequent letters may be of either case.
        Pattern fullNamePattern = Pattern.compile("^[A-Z][a-zA-Z]+\\s[A-Z][a-zA-Z]+$");

        String fullName;
        do {
            fullName = (String) JOptionPane.showInputDialog(null,
                    "Please enter your first and last names."
                            + "\nThe first letter of each name must be capitalised."
                            + "\nInput letters only, separating your first and last names by a space."
                            + "\nEg. MaryJane Parker",
                    APP_NAME, JOptionPane.QUESTION_MESSAGE, ICON, null, null);
            if (fullName == null) {
                mainMenuGui();
                return null; //End method because exiting to different GUI method.
            }
        } while (!matchValidInputString(fullNamePattern, fullName));

        //**********GET USER EMAIL**********
        // A regex pattern to check that the email complies with RFC 5322.
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

        String email;
        do {
            email = (String) JOptionPane.showInputDialog(null,
                    "Please enter your email address (eg. leetgeek@javajunky.com)",
                    APP_NAME, JOptionPane.QUESTION_MESSAGE, ICON, null, null);
            if (email == null) {
                mainMenuGui();
                return null; //End method because exiting to different GUI method.
            }
        } while (!matchValidInputString(emailPattern, email));

        //**********GET USER PHONE NUMBER**********
        // A regex pattern to check that phone numbers start with a 0 and are followed by 9 digits.
        // Adapted from COSC120 Tute 4 solution 3_4, FindADog.java, ln94-98

        Pattern phonePattern = Pattern.compile("^0\\d{9}$");
        String phoneNo;
        do {
            phoneNo = (String) JOptionPane.showInputDialog(null,
                    "Please enter your phone number in 10-digit format (eg. 0400133700)",
                    APP_NAME, JOptionPane.QUESTION_MESSAGE, ICON, null, null);
            if (phoneNo == null) {
                mainMenuGui();
                return null; //End method because exiting to different GUI method.
            }
        } while (!matchValidInputString(phonePattern, phoneNo));

        Geek geekOrdering = new Geek(fullName, email, phoneNo);
        return geekOrdering;
    }

    /**
     * Populate an Order record with all attribute values relevant to customer order
     *
     * @param geekOrdering                     Geek record containing the customer's personal details.
     * @param selectedCoffeeWithCustomisations Coffee instance containing the attributes
     *                                         of the customer's desired coffee.
     * @return customerOrder, an Order record instance with the relevant attribute values for
     * writing the order to file.
     */
    private static Order createCustomerOrderRecord(Geek geekOrdering, Coffee selectedCoffeeWithCustomisations) {
        // Assumes that normal flow always returns exactly one milk in milkSet; requirement
        // to store dreamCoffee as a standard coffee (not a class extension), and the reuse
        // of the class' Set<Milk> parameter means this could change on refactoring.
        // Possible future business logic exception not handled, but iterating through an empty set is handled.
        Milk selectedMilk;
        try {
            selectedMilk = selectedCoffeeWithCustomisations.getMilkSet().iterator().next();
        } catch (Exception e) {
            System.out.println(
                    "An error occurred when iterating the milkSet when creating an Order "
                    +"for 'createCustomerOrderRecord'."
                    +"\nThis was likely caused by an empty milkSet. A placeholder of 'None' "
                            +"has been added for the order milk to avoid a NullPointerException on the iterator."
                    +"\n\nException details: "+e);
            selectedMilk = Milk.NONE;
        }

        Order customerOrder = new Order(
                geekOrdering.name(),
                geekOrdering.phoneNo(),
                geekOrdering.email(),
                selectedCoffeeWithCustomisations.getMenuItemName(),
                selectedCoffeeWithCustomisations.getMenuItemId(),
                selectedCoffeeWithCustomisations.getDrinkType(),
                selectedMilk,
                selectedCoffeeWithCustomisations.getExtrasSet()
        );
        return customerOrder;
    }

    /**
     * Create customer order text file saved to system.
     * Check directory access, allocate an unused filename and write the file to the directory.
     * Calls a helper method to build the order String.
     * <p>
     * Uses ideas adapted from several sources:
     * File existence check code from: https://www.baeldung.com/java-file-directory-exists .
     * isWritable() method idea from:
     * https://www.geeksforgeeks.org/java/files-iswritable-method-in-java-with-examples/
     * Files.writeString() method idea from Roberto's answer of Feb 24 2014:
     * https://stackoverflow.com/questions/7366266/best-way-to-write-string-to-file-using-java-nio
     *
     * @param customerOrder the record holding all attribute values relevant to the order.
     * @return orderString contents of the order txt. Can be displayed to user in following GUI.
     */
    private static String writeCustomerOrderToTxt(Order customerOrder) {
        // Check write permissions for directory. "./" must exist because it's the program's root
        // directory, but if the write out path was moved then there should also be a dir.exists()
        // check.
        Path writeOutDir = Paths.get("./");
        if (!Files.isWritable(writeOutDir)) {
            System.err.println("Error: File " + writeOutDir + " is not writable.");
            JOptionPane.showMessageDialog(null,
                    "Error: Your order could not be written to the system.The program will exit when "
                            + "you exit this dialogue box.",
                    APP_NAME, JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        int requestNo = 0; // Start counting at 0 because do-while always runs at least once.

        // Short circuit the request to avoid unreasonable wait time. This problem would need to be
        // resolved if any user has ordered 10000 coffees off the same phone number (yay!).
        final int maxRequestsNo = 10000;
        String pathString;
        Path fullOutputPath;
        // Loop to find a valid filepath that doesn't overwrite an existing order file.
        do {
            requestNo++;
            pathString = "./Order_" + customerOrder.phoneNo() + "_" + requestNo + ".txt";
            fullOutputPath = Paths.get(pathString);
        } while (Files.exists(fullOutputPath) || requestNo > maxRequestsNo);

        if (requestNo == maxRequestsNo) {
            System.err.println("When attempting to write order to output text, maxRequestsNo was "
                    + "exceeded for the user's phone number of " + customerOrder.phoneNo() + ".");
            JOptionPane.showMessageDialog(null,
                    "Error: It looks like you've ordered you've ordered " + maxRequestsNo
                            + " coffees with this phone number.\n"
                            + "You're amazing, but our ordering system is not built to handle this level of devotion."
                            + "\n\nPlease go speak with management to claim a prize if this is the case."
                            + "\nYou can still order off a different phone number while we wait for our "
                            + "dev team to fix this for you.",
                    APP_NAME, JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        String orderString = orderStringToWriteOut(customerOrder); // Helper method builds String.

        // WRITE OUT ORDER TO THE FILEPATH DETERMINED ABOVE
        try {
            Files.writeString(
                    fullOutputPath,
                    orderString
            );
        } catch (IOException e) {
            System.err.println("Error writing output file: " + fullOutputPath + "\n" + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error: Your order could not be saved to our system. We're really sorry!"
                            + "\nYou're welcome to try again, or else go order at the front counter.",
                    APP_NAME, JOptionPane.ERROR_MESSAGE);
            mainMenuGui();
            return null; //Early exit from method.
        }
        System.out.println("Order has been saved to " + fullOutputPath + "\n");
        return orderString; // Returns orderString, which can be displayed in success GUI window.
    }

    /**
     * Capitalise the first letter of each word in a string, and make other letters lowercase.
     * <p>
     * Adapted with minor modification from this tutorial:
     * https://www.geeksforgeeks.org/java/java-program-to-capitalize-the-first-letter-of-each-word-in-a-string/
     *
     * @param input the String to be modified.
     * @return the String in the desired (capitalised[0]lowercase[1:]) format.
     */
    private static String capitaliseFirstLettersOnly(String input) {
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

    /**
     * A regex matcher that ensures that the user's entry matches the provided regex pattern.
     * <p>
     * Adapted from COSC120 Tute 4 solution 3_4, FindADog.java, ln126-160.
     * Patterns compilations moved to calling method to avoid recompilation at each loop.
     *
     * @param userInput the candidate String entered by the user
     * @return true if String matches regex/false if not
     */
    private static boolean matchValidInputString(Pattern regexPattern, String userInput) {
        Matcher matcher = regexPattern.matcher(userInput);
        return matcher.matches();
    }

    /**
     * Helper method to create a String formatted to meet the order details txt requirements.
     *
     * @param customerOrder record containing all attribute values necessary to record an order.
     * @return a String of the customer's order.
     */
    private static String orderStringToWriteOut(Order customerOrder) {
        StringBuilder sb = new StringBuilder();
        sb.append("Order details:\n");
        sb.append("\tName: ").append(customerOrder.name())
                .append(" (").append(customerOrder.phoneNo()).append(")\n");
        sb.append("\tEmail: ").append(customerOrder.email());
        sb.append("\tItem: ").append(customerOrder.menuItemName()).append(" (")
                .append(customerOrder.menuItemId()).append(") - ").append(customerOrder.drinkType()).append("\n");
        sb.append("\tMilk: ").append(customerOrder.milk()).append("\n");
        sb.append("\tExtras: ");

        //Output extrasSet in appropriate String format.
        //Code adapted from Christopher Perry's response of July 25, 2014 at
        // https://stackoverflow.com/questions/6622974/convert-string-to-comma-separated-string-in-java
        String extrasOutputString = customerOrder.extrasSet().isEmpty() ? "None" : String.join(", ", customerOrder.extrasSet());

        sb.append(extrasOutputString);
        return sb.toString();
    }

    /**
     * Helper method. Builds a String of coffee matches based on a set of matched coffees.
     * Matched coffee set constructed by coffeeMatcher, a Menu helper method called by
     * MenuSearcher viewMatchesAndOrder.
     * Called by viewMatchesAndOrder.
     * Informs the user of all matches OR the situation of no match.
     *
     * @param matches a Set of Coffees containing the user's matched coffees.
     * @return a String informing the user how to order, whether they've matched with any coffees,
     * and if so, their details.
     */
    private static String buildCoffeeMatchString(Set<Coffee> matches) {
//        Set<Coffee> matches = new HashSet<>(menu.coffeeMatcher(dreamCoffee));
        StringBuilder viewMatchesSB = new StringBuilder();
        viewMatchesSB.append("**********Java Bean Coffee Matcher and Ordering System**********\n\n");
        if (matches.isEmpty()) {
            viewMatchesSB.append("No matching coffee found."
                    + "\n\nBut you can still order any coffee you'd like off our drinks menu."
                    +"\n\nJust exit this window to return to the main menu.");
        } else {
            viewMatchesSB.append("Congratulations, you've matched!")
                    .append("\nYour coffee matches are shown below.")
                    .append("\nYou can order one of your matches from the drop-down list at the bottom of this window."
                            + "\n\n If you'd like to order a coffee you haven't yet matched with, "
                            + "\njust exit this window to return to the main menu.")
                    .append("\n********************\n\n");
            for (Coffee coffee : matches) {
                viewMatchesSB.append("\n").append(coffee.coffeeDetailsString())
                        .append("\n").append("\t**********");
            }
        }
        return viewMatchesSB.toString();
    }

    /**
     * Create a Coffee Array of all coffees. Helper method for viewMatchesAndOrder().
     * Used to populate drop-down GUI selection Array.
     * <p>
     * Uses an array of custom objects with custom toString representation invoked on Coffee class.
     * Idea from: https://stackoverflow.com/questions/4078714/swing-selecting-among-a-set-of-objects
     * <p>
     * Adds Ids after plain language names, as these are the unique identifiers and could
     * potentially help people ordering if multiple coffees of the same plain language name exist.
     *
     * @return coffeeNamesAndID, a Coffee[] with all coffees listed alphabetically in the
     * format "[name] - [menuItemId]"
     */

    private static Coffee[] allCoffeesNameAndId() {
        // toArray() with type specification and right-sized array per
        // https://stackoverflow.com/questions/28392705/difference-between-toarrayt-a-and-toarray?rq=3 and
        // https://stackoverflow.com/questions/174093/toarraynew-myclass0-or-toarraynew-myclassmylist-size?noredirect=1&lq=1
        // Array of all coffees; Coffee.toString() auto-gives appropriate names.
        Coffee[] coffeeNamesAndId = menu.getMenu().values().toArray(new Coffee[0]);

        // Sort array alphabetically for GUI use.
        // Can't sort an array of custom objects by default comparison. Need to specify a comparator.
        // Code from https://www.geeksforgeeks.org/java/sort-an-array-in-java-using-comparator/ .
        Arrays.sort(coffeeNamesAndId, Comparator.comparing(coffee -> coffee.getMenuItemName()));

        // Immutable array return idea from https://www.geeksforgeeks.org/java/immutable-array-in-java/
        return coffeeNamesAndId.clone();
    }


    /**
     * Round String to 2 decimal places. Rounds up at halves. BigDecimal offers best precision for monetary calculations.
     * User might input a figure with many decimal places, but standard commercial transactions are in 2f.
     * <p>
     * Idea inspired from discussion at
     * https://stackoverflow.com/questions/153724/how-to-round-a-number-to-n-decimal-places-in-java and
     * https://www.baeldung.com/java-bigdecimal-biginteger
     *
     * @param value a String of digits with, optionally, a point to indicate a decimal
     * @return a float of the input string, rounded to 2f.
     */
    private static float roundStringTo2fFloat(String value) {
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * Helper method that groups two helpers and passes an object between them (selectedCoffeeWithAttributes).
     * Facilitates returning to coffee selection if user changes their mind
     * about their desired menu coffee at attribute selection stage.
     * @return Coffee of the user's selected coffee with all parameters set (including modifiable attributes).
     */
    private static Coffee orderAnyCoffeeParent(){
        Coffee selectedCoffee = orderAnyCoffeeSelectedCoffee();
        if (selectedCoffee == null) return null; // Potentially already sent to mainMenuGui in child method above.
        //Attribute selection only reached if a coffee is selected in the preceding method call
        Coffee selectedCoffeeWithAttributes = orderAnyCoffeeSelectAttributes(selectedCoffee);
        return selectedCoffeeWithAttributes;
    }

    /**
     * GUI for selecting any menu coffee to purchase. Calls helper methods to populate options.
     * @return selected coffee with default values as read from menu.txt (incl complete Sets).
     */
     private static Coffee orderAnyCoffeeSelectedCoffee() {
        //TODO ENSURE ANY POSSIBLE CONFLICTING COFFEE OBJECTS TO PASS TO ORDER ARE OVERWRITTEN AS NULL********************************
//        if (geekOrdering != null) geekOrder = null;
//        if (selectedCoffeeWithCustomisations != null) selectedCoffeeWithCustomisations = null;

         // Coffee selector pane
         String paneInstruction =
                 "Just want to order a coffee without matching?"
                 + "\n\nSelect your coffee type, then select its milk and extras in the next menu.";
         Coffee[] allCoffeesNameAndId = allCoffeesNameAndId();
         // Default choice first coffee on the list; changing this condition would oblige a state
         // check when populating attribute drop-down panels.
         Coffee selectedCoffee =
                 (Coffee) JOptionPane.showInputDialog(null, paneInstruction, APP_NAME, JOptionPane.OK_CANCEL_OPTION,
                         ICON, allCoffeesNameAndId, allCoffeesNameAndId[0]);
         if (selectedCoffee==null) {
             mainMenuGui();
             return null; //Early exit from method.
         }
         return selectedCoffee;
     }

    /**
     * Select the attributes (milk and extras) for ordering a menu coffee that wasn't a custom match.
     * Called from parent, orderAnyCoffeeParent().
     * @param selectedCoffee Coffee object representing the menu coffee the user would like to order.
     * @return selectedCoffeeWithAttributes, a Coffee object with user-defined extras and milk,
     * and otherwise the param Coffee's attributes.
     * OR returns null if no coffee was finalised.
     */
    private static Coffee orderAnyCoffeeSelectAttributes(Coffee selectedCoffee){
        String paneInstruction =
                "Pick the extra(s) you'd like, your coffee's milk option, and then proceed to order.";

        // Populate selected coffee extras set Array
        Set<String> selectedCoffeeExtrasSet = new TreeSet<>(selectedCoffee.getExtrasSet());
        String[] selectedCoffeeExtrasArray = selectedCoffeeExtrasSet.toArray(new String[0]);
        JComboBox<String> extrasDropDown = new JComboBox<>(selectedCoffeeExtrasArray);

        // Populate selected coffee milk set Array
        Set<Milk> selectedCoffeeMilkSet = new TreeSet<>(selectedCoffee.getMilkSet());
        Milk[] selectedCoffeeMilkArray = selectedCoffeeMilkSet.toArray(new Milk[0]);
        JComboBox<Milk> milksDropDown = new JComboBox<>(selectedCoffeeMilkArray);


        String[] attributeSelectorButtons =
                {"Add Selected Extra", "Skip Adding Extras\n(clear any chosen extras)", "Select Milk",
                        "Order With These Selections", "Pick a different coffee"};

        Object[] attributesMessageAndDropDown = {paneInstruction, extrasDropDown, milksDropDown};

        Set<String> extrasSet = new HashSet<>(); // Hold all user extras choices.
        String extraToAddToSet; // Container for individual extra selections.
        Set<Milk> milkSet; // User milk choice.
        Milk selectedMilk; //Necessary intermediate var because Coffee milks are sets.

        Coffee selectedCoffeeWithAttributes = null; // Initialise Coffee to create and return

        attributesLoop:
        while (true) {
            int buttonChoice = JOptionPane.showOptionDialog(null,
                    attributesMessageAndDropDown,
                    APP_NAME, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, ICON, attributeSelectorButtons, null);
            extraToAddToSet = (String) extrasDropDown.getSelectedItem();
            selectedMilk = (Milk) milksDropDown.getSelectedItem();
            milkSet = EnumSet.of(selectedMilk);

            // Switch on button choice (showOptionDialog returns int).
            switch (buttonChoice) {
                // User closed window.
                case -1 -> {
                    mainMenuGui(); // Go to main menu.
                    return null;// Exiting to different GUI method.
                }
                //Attempt to add selected extra, either notifying that the element has already been
                //added, or adding it to the user's Set.
                case 0 -> {
                    if (extrasSet.contains(extraToAddToSet)) {
                        JOptionPane.showMessageDialog(null,
                                "You've already selected that extra. Your selected extras include: "
                                        + String.join(", ", extrasSet)
                                        + "\nPlease select another extra, or click 'Finished Adding Extras'"
                                        + " to move to the next screen.",
                                APP_NAME, JOptionPane.INFORMATION_MESSAGE, ICON);
                    } else {
                        extrasSet.add(extraToAddToSet);
                    }
                }
                // User skips adding extras--ensure their extras set is cleared.
                case 1 -> {
                    extrasSet.clear();
                }
                case 2 -> {
                    //This doesn't actually do anything, but it would be confusing to UI if they had
                    //to click to add extras but not to add milk. Mlk assignment could be moved here
                    //from the start of the loop, but it seems more fool-proof that if a user clicks
                    //a milk it's automatically added.
                    continue;
                }
                case 3 -> {
                    if (milkSet.isEmpty()){
                        JOptionPane.showMessageDialog(null,
                                "You've tried to finish this selection without selecting your milk."
                                        +"\nAll coffees need a milk choice, even if it's 'None'",
                                APP_NAME, JOptionPane.INFORMATION_MESSAGE, ICON);
                    } else {
                        selectedCoffeeWithAttributes = new Coffee(
                                selectedCoffee.getMenuItemId(),
                                selectedCoffee.getMenuItemName(),
                                selectedCoffee.getPrice(),
                                selectedCoffee.getNumOfShots(),
                                selectedCoffee.getSugar(),
                                selectedCoffee.getDrinkType(),
                                selectedCoffee.getProvenance(),
                                milkSet,
                                extrasSet,
                                selectedCoffee.getDescription());
                        break attributesLoop;
                    }
                }
                case 4 -> {
                    // Restart menu coffee selection process. Ensure all values cleared and loop broken.
                    extrasSet.clear();
                    milkSet.clear();
                    orderAnyCoffeeParent();
                    break attributesLoop;
                }
            }
        }
        if (selectedCoffeeWithAttributes != null) {
            return selectedCoffeeWithAttributes;
        } else {
            return null;
        }
    }

    //**************************************GET DREAMCOFFEE PREFERENCES HELPER METHODS************************************

    /**
     * Get user DrinkType preference via GUI.
     * @return DrinkType Enum value ('hot coffee'/'frappe')
     */
    private static DrinkType getDreamDrinkType() {
        DrinkType drinkType = (DrinkType) JOptionPane.showInputDialog(null,
                "For starters, what sort of drink would you like? Hot Coffee or Frappe?",
                APP_NAME, JOptionPane.QUESTION_MESSAGE, ICON, DrinkType.values(), null);
        if (drinkType == null) {
            mainMenuGui();
            return null;
        }
        return drinkType;
    }

    /**
     * Get user milk choice via GUI.
     * User only selects one milk--milk choices are usually exclusive preferences.
     *
     * IntelliJ prompted wrapping the assignment in Collections.singleton() to assign the Enum
     * value to my desired Collection type. I did some digging and decided EnumSet.of() was a
     * better option in case the program design changes to require muteability of the user's
     * milk choices. Sources:
     * https://www.geeksforgeeks.org/java/collections-singleton-method-java/ &
     * https://www.geeksforgeeks.org/java/enumset-of-method-in-java/
     * @return Set <Milk>, the user's milk choice.
     * Set because Coffee parameters expect a Set, even though the Set only contains one value.
     */
    private static Set<Milk> getDreamMilkSet() {
        Milk selectedMilk = (Milk) JOptionPane.showInputDialog(null,
                "What sort of milk would you like?",
                APP_NAME, JOptionPane.QUESTION_MESSAGE, ICON, Milk.values(), null);
        if (selectedMilk == null) {
            mainMenuGui();
            return null;
        }

        Set<Milk> milkSet = EnumSet.of(selectedMilk);
        return Set.copyOf(milkSet);
    }

    /**
     * Gets user input minimum price for a coffee.
     *
     * null-checkable input var idea adapted from my solution to COSC120 Tute 4, FindADog.java
     * ln112-129.
     *
     *  The expanded JOptionPane with ICON placement returns an object, not String. Requires
     *  explicit casting. Idea from response by selofain, Nov 20, 2019, at
     *  https://stackoverflow.com/questions/33961793/custom-icon-joptionpane-showinputdialog
     *
     * @param priceMin float declared in calling method to use for comparison with priceMax;
     * should help maintainability and contextual visibility in calling method.
     * @return priceMin Float value, rounded to 2f decimal.
     * Would ideally return float primitive, but wrapper necessary for null early return.
     */
    private static Float getDreamPriceMin(float priceMin){
        String priceMinInput; //Declared outside the loop to avoid recreating the object.
        do {
            priceMinInput = (String) JOptionPane.showInputDialog(null,
                    "What's the minimum you'd like to spend on your drink?",
                    APP_NAME, JOptionPane.QUESTION_MESSAGE, ICON, null, null);
            if (priceMinInput == null) {
                mainMenuGui();
                return null; //Exiting to different GUI method.
            }
            try {
                priceMin = roundStringTo2fFloat(priceMinInput);
                if (priceMin < 0) {
                    JOptionPane.showMessageDialog(null,
                            "Sorry, input must be in whole digit or decimal format, eg. 3.5\n"
                                    + "Please try again.", APP_NAME, JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Sorry, input must be in whole digit or decimal format, eg. 3.5\n"
                                + "Please try again.", APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        } while (priceMin < 0);         // Allow 0 min price.
        return priceMin;
    }

    /**
     * Gets user input regarding maximum price for a coffee.
     * priceMax must exceed priceMin.
     * NB. 2f conversion means that in situations where the priceMin is 1 rounded decimal up
     * from the priceMax, priceMin = priceMax, and the program proceeds as intended.
     * Eg. If priceMinInput= 2 and priceMaxInput= 1.999, they're both considered to = 2.00.
     * @param priceMin float passed from calling method local variable to allow Max>Min comparison.
     * @return Float (2f rounded of the user's maximum price.
     * Would ideally return float primitive, but wrapper necessary for null return on exit to menu.
     */
    private static Float getDreamPriceMax(float priceMin) {
        float priceMax = -1;
        String priceMaxInput;
        do {
            priceMaxInput = (String) JOptionPane.showInputDialog(null,
                    "What's the maximum you'd like to spend on your drink?",
                    APP_NAME, JOptionPane.QUESTION_MESSAGE, ICON, null, null);
            if (priceMaxInput == null) {
                mainMenuGui();
                return null; //End loop because exiting to different GUI method.
            }
            try {
                priceMax = roundStringTo2fFloat(priceMaxInput);
                if (priceMax < priceMin) {
                    JOptionPane.showMessageDialog(null,
                            "Sorry, your maximum spend must be more than or equal to your minimum spend.\n"
                                    + "Please try again.", APP_NAME, JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Sorry, input must be in whole digit or decimal format, eg. 4.5\n"
                                + "Please try again.", APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        } while (priceMax < priceMin);
        return priceMax;
    }

    /**
     * Gets user input to compose their set of desired extras.
     * GUI with drop-down menu of alphabetically sorted extras derived from all menu extras, and custom buttons.
     * Excessive hours spent trying to make this work based on ideas here:
     * https://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html#input
     * https://stackoverflow.com/questions/23715241/java-swing-joptionpane-combine-showinputdialog-and-showoptiondialog
     * https://coderanch.com/t/671124/java/Textbox-Combobox-time-JOptionPane
     * JPanel version doesn't quite look as nice.
     * @return a Set of Strings of the user's desired extras.
     */
    private static Set<String> getDreamExtrasSet() {
        // Alphabetically sorted TreeSet of menu extras--alphabetical for consistent user experience.
        Set<String> extrasSetWithMenuOptions = new TreeSet<>(menu.getAllMenuExtras());

        //**********Objects to populate GUI params**********
        // toArray() requires explicit return type for the array type required. Though the COSC 120
        // Tute 4 3_4 FindADog.java ln170 example indicates it's not required if called in
        // JOptionPane parameters.
        String[] extrasSelectionArray = extrasSetWithMenuOptions.toArray(new String[0]);

        Set<String> extrasSet = new HashSet<>(); // Hold all user extras choices.
        String extraToAddToSet; // Container for individual extra selections.

        String extrasDialogMessage =
                "Which extras would you like?"
                        + "\n\nWhen you're done, select Finished Adding Extras' to move to your dream " +
                        "coffee's next attribute."
                        + "\n\nSelect 'Skip' to skip adding any extras preferences; " +
                        "this will also clear any extras you chose before changing your mind."
                        +"\n\nNote, 'skip' means you want to skip filtering based on extras, " +
                        "and will happily match with any extra that comes your way";

        JComboBox<String> extrasDropDown = new JComboBox<>(extrasSelectionArray);
        String[] extrasButtons = {"Add Selected Extra", "Skip Adding Extras", "Finished Adding Extras"};
        // Object array stores the dialogue prompt and the JComboBox. Magic!
        Object[] extrasMessageAndDropDown = {extrasDialogMessage, extrasDropDown};

        // Labelled loop to break from within switch. Idea from betteroutthanin's response of April 2, 2014:
        // https://stackoverflow.com/questions/22823395/java-how-can-i-break-a-while-loop-under-a-switch-statement
        extrasLoop:
        while (true) {
            int buttonChoice = JOptionPane.showOptionDialog(null,
                    extrasMessageAndDropDown,
                    APP_NAME, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, ICON, extrasButtons, null);
            extraToAddToSet = (String) extrasDropDown.getSelectedItem();

            // Switch on button choice (showOptionDialog returns int).
            switch (buttonChoice) {
                // User closed window.
                case -1 -> {
                    extrasSet.clear(); // Ensure extrasSet is cleared before return.
                    mainMenuGui(); // Go to main menu.
                    return null; // Exiting to different GUI method.
                }
                //Attempt to add selected extra, either notifying that the element has already been
                //added, or adding it to the user's Set.
                case 0 -> {
                    if (extrasSet.contains(extraToAddToSet)) {
                        JOptionPane.showMessageDialog(null,
                                "You've already selected that extra. Your selected extras include: "
                                        + String.join(", ", extrasSet)
                                        + "\nPlease select another extra, or click 'Finished Adding Extras'"
                                        + " to move to the next screen.",
                                APP_NAME, JOptionPane.INFORMATION_MESSAGE, ICON);
                    } else {
                        extrasSet.add(extraToAddToSet);
                    }
                }
                // User skips adding extras--ensure their extras set is cleared.
                case 1 -> {
                    extrasSet.clear();
                    break extrasLoop;
                }
                // User wants their selected extras. Ensure it's not erroneously clicked on an empty set.
                case 2 -> {
                    if (extrasSet.isEmpty()){
                        JOptionPane.showMessageDialog(null,
                                "You've tried to finish this selection with an empty extras set."
                                        +"\nIf that's what you intended, then please click 'Skip'."
                                        +"\nOtherwise, please add extras before moving to the next screen.",
                                APP_NAME, JOptionPane.INFORMATION_MESSAGE, ICON);
                    } else {
                        break extrasLoop;
                    }
                }
            }
        }
        return Set.copyOf(extrasSet);
    }

    /**
     * Gets user input coffee provenance preference via GUI.
     * @return Provenance Enum
     */
    private static Provenance getDreamProvenance() {
        Provenance provenance = (Provenance) JOptionPane.showInputDialog(null,
                "Terroir is important. What's the provenance of the beans you're after?",
                APP_NAME, JOptionPane.QUESTION_MESSAGE, ICON, Provenance.values(), null);
        if (provenance == null) {
            mainMenuGui();
            return null; // Early exit to another method.
        }
        return provenance;
    }

    /**
     * Gets user input number of shots preference via GUI.
     *
     * @return Integer numOfShots. Would ideally return int, but primitives not nullable.
     * Null return needed for early exit to main menu.
     */
    private static Integer getDreamNumOfShots() {
        // Largely repeats get price min code and relevant attributions.
        int numOfShots = -1; //Placeholder value used for looping.

        String numOfShotsInput;
        do {
            numOfShotsInput = (String) JOptionPane.showInputDialog(null,
                    "How many shots of coffee would you like?",
                    APP_NAME, JOptionPane.QUESTION_MESSAGE, ICON, null, null);
            if (numOfShotsInput == null) {
                mainMenuGui();
                return null; //End method because exiting to different GUI method.
            }
            try {
                numOfShots = Integer.parseInt(numOfShotsInput);
                if (numOfShots < 0) {
                    JOptionPane.showMessageDialog(null,
                            "Sorry, the number of shots cannot be negative!\n"
                                    + "Please try again.", APP_NAME, JOptionPane.ERROR_MESSAGE);
                    continue; // Next iteration.
                }
                // Avoids situations of accidentally excessive input, unsafe choices and the
                // possibility that a user inputs a number that exceeds the integer datatype range.
                if (numOfShots > 100) {
                    JOptionPane.showMessageDialog(null,
                            "Sorry, we don't feel safe selling you a coffee with that much caffeine!\n"
                                    + "Please try again.", APP_NAME, JOptionPane.ERROR_MESSAGE);
                    numOfShots = -1; // Reset counter if successful parseInt but exceeds limit.
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Sorry, input must be in whole numbers, eg. 2\n"
                                + "Please try again.", APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        } while (numOfShots < 0);         // Allow 0 min shots in case non-caffeinated drinks are sold.
        return numOfShots;
    }

    /**
     * Gets user input sugar preference ('Yes'/'No') via GUI.
     * Ideally this would return boolean, but then the early exit return call doesn't work properly.
     * @return String value of sugar preference, to convert to boolean in calling method.
     */
    private static String getDreamSugar() {
        String[] sugarOptions = {"Yes", "No"};
        String sugarString = (String) JOptionPane.showInputDialog(null,
                "Would you like sugar?",
                APP_NAME, JOptionPane.QUESTION_MESSAGE, ICON, sugarOptions, "No");
        if (sugarString == null) {
            mainMenuGui();
            return null; // End method on call to new method.
        }
        return sugarString;
    }

    /**
     * Calls helper methods to get user info to create a new Geek,
     * create a new Order based on the Geek and the dream coffee, and
     * write out the customer order and return a string of the order written out.
     * @return String of the customer's final order.
     */
    private static String createGeekAndOrderParent(){
        Geek geekOrdering = getUserInfo();
        Order customerOrder = createCustomerOrderRecord(geekOrdering, dreamCoffee);
        String orderOutString = writeCustomerOrderToTxt(customerOrder);
        return orderOutString;
    }

    /**
     * Show the customer their final order when everything's been successful.
     * @param orderOutString String containing the details of the order written out to text.
     */
    private static void showCustomerOrder(String orderOutString){
        JOptionPane.showMessageDialog(null, orderOutString, APP_NAME, JOptionPane.PLAIN_MESSAGE, ICON);
        dreamCoffee = null; // Reset dream coffee for next construction
        mainMenuGui();
    }

    /**
     * Load app ICON. Helper method allows loading it as final in class field.
     * <p>
     *  Error check ImageIcon load idea from reading the documentation:
     *  https://docs.oracle.com/en/java/javase/24/docs/api/java.desktop/javax/swing/ImageIcon.html#getImageLoadStatus()
     *  Discussion on StackOverflow indicates that ImageIcon's failure to load from file will not cause a
     *  fatal error--it will just produce a blank placeholder box. StackOverflow source:
     *  https://stackoverflow.com/questions/77634130/i-cant-get-the-imageicon-to-display-properly
     * <p>
     * @return ImageIcon to display on GUIs.
     */
    private static ImageIcon loadIcon() {
        ImageIcon guiIcon = new ImageIcon(ICON_PATH);

        if (guiIcon.getImageLoadStatus() == MediaTracker.ERRORED
                || guiIcon.getImageLoadStatus() == MediaTracker.ABORTED) {
            System.err.println("Error: The ICON image failed to load. Check that the image is in the path "
                    + ICON_PATH + " and that you have permission to access this path.");
        }
        return guiIcon;
    }
    

    private static void testDreamCoffeeCreation(){
        System.out.println("\n\nCoffee you're about to use to match is:");
        System.out.println(dreamCoffee.hashCode());
        for (Milk m : dreamCoffee.getMilkSet()) System.out.println(m);
        System.out.println(dreamCoffee.getDrinkType());
        for (String s : dreamCoffee.getExtrasSet()) System.out.println(s);
        System.out.println(dreamCoffee.getNumOfShots());
        System.out.println(dreamCoffee.getSugar() ? "yes" : "no");
        System.out.println("min" + dreamCoffee.getPriceMin() + " max" + dreamCoffee.getPriceMax());
        System.out.println(dreamCoffee.getProvenance());

    }
}
