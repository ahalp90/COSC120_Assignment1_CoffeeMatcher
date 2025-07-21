import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ariel Halperin. Made for University of New England course COSC120, Assignment 1.
 * Holds all coffees loaded from menu.txt in a field Map.
 * Map datatype for efficient lookup when populating user matches with direct access to the coffees;
 * dropdown selectors of coffees to order return a Coffee[] rather than a String[], making this an efficient choice.
 * <p>Has methods for adding coffee to field Map, matching coffees based on requirements,
 * returning details of instance extras, and returning copy of instance Menu.
 */
public class Menu {
    private final Map<String, Coffee> menu = new HashMap<>();

    /**
     * Default constructor to initialise the class without specific parameters or initial values.
     */
    public Menu() {
    }

    /**
     * Setter: Add Coffee objects to the Menu's data structure (Map).
     * @param coffee holding all the attribute fields relevant to a Coffee.
     */
    public void addCoffee(Coffee coffee) {
        this.menu.put(coffee.getMenuItemId(), coffee);
    }


    /**
     * Compares the values of the Menu coffees against the criteria in a user-defined dream coffee to return matches.
     * Set collection for efficient uniqueness enforcement and content equivalence checks.
     *
     * @param dreamCoffee is the user's parametrised ideal coffee template
     * @return Set (HashSet) of coffees matching the user's dream coffee parameters.
     */
    public Set<Coffee> coffeeMatcher(Coffee dreamCoffee) {
        Set<Coffee> matchedCoffeeSet = new HashSet<>();
        // Boolean flag for whether the dreamCoffee has extras preferences; avoid redundant isEmpty() checks.
        boolean dreamCoffeeNoExtrasPreference = dreamCoffee.getExtrasSet().isEmpty();
        for (Coffee i : this.menu.values()) {
            // Filter based on price range.
            if ((i.getPrice() >= dreamCoffee.getPriceMin() && i.getPrice() <= dreamCoffee.getPriceMax())
                    && (i.getNumOfShots() == dreamCoffee.getNumOfShots())
                    && (i.getSugar() == dreamCoffee.getSugar())
                    && (i.getDrinkType() == dreamCoffee.getDrinkType())
                    && (i.getProvenance() == dreamCoffee.getProvenance())
                    // dreamCoffee milkSet will only ever contain one Milk in current design.
                    && (i.getMilkSet().containsAll(dreamCoffee.getMilkSet()))
                    // Add the coffee if (1) the user's dreamCoffee has no extras preference, or (2)
                    // there is >=1 overlapping element between the user's extras choices and the
                    // menu coffee's extras options.
                    && (
                            dreamCoffeeNoExtrasPreference ||
                            !i.overlapExtrasSet(dreamCoffee).isEmpty()
                    )
            ) {
                matchedCoffeeSet.add(i);
            }
        }
        return matchedCoffeeSet;
    }

    /**
     * Derive a set of all menu extras found across all menu items.
     * Extras must be dynamically derived as they can change with each version of menu.txt.
     * Iterate through each coffee in the menu and unpack its extras into the allMenuExtras set.
     * Set items are unique by definition, so no need to de-duplicate.
     *
     * Code structure inspired by COSC120 Tute 4 Solution 3_4,AllDogs.java ln32-38.
     * addAll() method idea from: https://www.geeksforgeeks.org/java/merge-two-sets-in-java/
     *
     * @return defensive copy of a Set of Strings identifying all extras on the current menu.txt.
     */
    public Set<String> getAllMenuExtras() {
        Set<String> allMenuExtras = new HashSet<>();
        for (Coffee i : this.menu.values()) {
            allMenuExtras.addAll(i.getExtrasSet());
        }
        return Set.copyOf(allMenuExtras);
    }

    /**
     * Find the minimum and maximum prices in the Menu.
     * @return float Array of menuPriceMin (index 0) and menuPriceMax (index 1).
     */
    public float[] getMenuPriceMinAndMax() {
        float menuPriceMin = 0;
        float menuPriceMax = 0;
        int menuCounter = 0;
        for (Coffee i : this.menu.values()) {
            if (menuCounter == 0) {
                menuPriceMin = i.getPrice();
                menuPriceMax = i.getPrice();
            } else {
                if (i.getPrice() < menuPriceMin) menuPriceMin = i.getPrice();
                if (i.getPrice() > menuPriceMax) menuPriceMax = i.getPrice();
            }
            menuCounter++;
        }
        return new float[]{menuPriceMin, menuPriceMax};
    }


    /**
     * Getter to access the Map object (menu) in the Menu object's field.
     * @return defensive copy of Map menu.
     */
    public Map<String, Coffee> getMenu() {
        return new HashMap<>(this.menu);
    }

}