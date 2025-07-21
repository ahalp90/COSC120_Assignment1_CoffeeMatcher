import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates Coffee object instances and contains methods to return their values--
 * including a derived String representation of the Coffee to order and a comparison of extras overlaps.
 */
public class Coffee {
    private final String menuItemId;
    private final String menuItemName;
    private final float price;
    private final int numOfShots;
    private final boolean sugar;
    private final DrinkType drinkType;
    private final Provenance provenance;
    private final Set<Milk> milkSet;
    // Dynamically generated member--derived in .MenuSearcher, loadMenuItems() method.
    private final Set<String> extrasSet;
    private final String description;

    //Variables for dreamCoffee search object. These are set to a sentinel -1 for
    //non-dreamCoffees.In an ideal world I would have extended the class, but my number of classes
    //was limited by the assignment requirements.
    private final float priceMin;
    private final float priceMax;

    Coffee(String menuItemId,
           String menuItemName,
           float price,
           int numOfShots,
           boolean sugar,
           DrinkType drinkType,
           Provenance provenance,
           Set<Milk> milkSet,
           Set<String> extrasSet,
           String description,
           float priceMin,
           float priceMax) {
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.price = price;
        this.numOfShots = numOfShots;
        this.sugar = sugar;
        this.drinkType = drinkType;
        this.provenance = provenance;
        this.milkSet = milkSet;
        this.extrasSet = extrasSet;
        this.description = description;
        this.priceMin = priceMin;
        this.priceMax = priceMax;
    }

    /**
     * Describes the coffee in the format required by the Greek Geek for orders.
     * @return String containing all details required for a coffee order, formatted appropriately.
     */
    public String coffeeDetailsString() {
        StringBuilder sb = new StringBuilder();
        sb.append(menuItemName).append(" (").append(menuItemId).append(")").append("\n");
        sb.append(description).append("\n");
        sb.append("Ingredients:\n\n");
        //case change drink type to match example?
        sb.append("- Type: ").append(drinkType).append("\n");
        sb.append("- Number of shots: ").append(numOfShots).append("\n");
        sb.append("- Sugar: ").append(sugar ? "Yes" : "No").append("\n");
        sb.append("- Milk options: ").append(
                // Take input from Collection of Enums, use the Enum's toString() method, and output the
                // collection items separated by a comma and space. Code idea from StackOverflow:
                // https://stackoverflow.com/questions/59642844/java-concatenate-list-of-enum-to-string
                // I could have used a loop, but this seemed neater.
                milkSet.stream().map(Object::toString).collect(Collectors.joining(", ")))
                .append("\n");
        sb.append("- Extras: ").append(extrasSet.stream().collect(Collectors.joining(", ")))
                .append("\n");
        sb.append("- Provenance: ").append(provenance).append("\n\n");
        sb.append("Price: $").append(price);

        return sb.toString();
    }


    /**
     * Helper method that returns the overlapping extras of two coffees.
     * @param comparisonCoffee the Coffee to be compared against the calling Coffee instance.
     * @return a Set of the two coffees' common extras.
     */
    public Set<String> overlapExtrasSet(Coffee comparisonCoffee) {
        // Allocate dream coffee extras strings to a disposable set.
        Set<String> overlapExtrasSet = new HashSet<>(comparisonCoffee.getExtrasSet());
        overlapExtrasSet.retainAll(this.getExtrasSet());
        return Set.copyOf(overlapExtrasSet);
    }

    /**
     * Return a string representation of the Coffee in a format sensible for end users
     * (since Coffees would otherwise return their custom Object hashcode identifier).
     * Used to populate ordering menu object array.
     *
     * Override syntax from:
     * https://www.geeksforgeeks.org/java/overriding-tostring-method-in-java/
     *
     * @return String in the format 'menuItemName + " - " + menuItemId'
     */
    @Override
    public String toString(){
        return menuItemName + " - " + menuItemId;
    }

    //**********GETTERS**********
    // Filter out unneeded ones after.
    public String getMenuItemId() {
        return menuItemId;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public float getPrice() {
        return price;
    }

    public int getNumOfShots() {
        return numOfShots;
    }

    public boolean getSugar() {
        return sugar;
    }

    public DrinkType getDrinkType() {
        return drinkType;
    }

    public Provenance getProvenance() {
        return provenance;
    }

    public Set<Milk> getMilkSet() {
        return new HashSet<>(this.milkSet);
    }

    public Set<String> getExtrasSet() {
        return new HashSet<String>(this.extrasSet);
    }

    public String getDescription() {
        return description;
    }

    public float getPriceMin() {
        return priceMin;
    }

    public float getPriceMax() {
        return priceMax;
    }

}
