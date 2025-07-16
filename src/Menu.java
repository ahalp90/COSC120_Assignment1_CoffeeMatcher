import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Menu {
    //datatype?? maybe this should be an ArrayList depending on interaction manner?
    private final Map<String, Coffee> menu = new HashMap<>();

    /**
     * Default constructor to initialise the class without specific parameters or initial values.
     */
    public Menu() {
    }

    /**
     * Setter: Add Coffee objects to the Menu's data structure (Map).
     *
     * @param coffee object holding all the fields relevant to attribute data of coffees.
     */
    public void addCoffee(Coffee coffee) {
        this.menu.put(coffee.getMenuItemID(), coffee);
    }

    //

    /**
     * Compares the values of the menu coffees against the criteria in a user-defined dream coffee to return matches.
     * Set collection for efficient uniqueness enforcement and content equivalence checks.
     *
     * @param dreamCoffee is the user's parametrised ideal coffee template
     * @return a HashSet of coffees matching the user's dream coffee parameters.
     */
    public Set<Coffee> coffeeMatcher(Coffee dreamCoffee) {
        Set<Coffee> matchedCoffeeSet = new HashSet<>();
        for (Coffee i : this.menu.values()) {
            if ((i.getPrice() >= dreamCoffee.getPriceMin() && i.getPrice() <= dreamCoffee.getPriceMax())
                    && (i.getNumOfShots() == dreamCoffee.getNumOfShots())
                    && (i.getSugar() == dreamCoffee.getSugar())
                    && (i.getDrinkType() == dreamCoffee.getDrinkType())
                    && (i.getProvenance() == dreamCoffee.getProvenance())
                    && (i.getMilkSet().containsAll(dreamCoffee.getMilkSet()))
                    && (i.getExtrasSet().containsAll(dreamCoffee.getExtrasSet()))
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
     * @return a set of Strings identifying all extras on the current menu.txt.
     */
    public Set<String> getAllMenuExtras() {
        Set<String> allMenuExtras = new HashSet<>();
        for (Coffee i : this.menu.values()) {
            allMenuExtras.addAll(i.getExtrasSet());
        }
        for (int j; j< allMenuExtras.size(); j++) {
            allMenuExtras[i] =
        }
        return allMenuExtras;
    }

    /**
     * Getter to access the Map object (menu) in the Menu object's field.
     * @return the Map menu.
     */
    public Map<String, Coffee> getMenu() {
        return menu;
    }

}