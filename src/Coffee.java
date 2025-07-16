import java.util.Set;
import java.util.stream.Collectors;

public class Coffee {
    private final String menuItemID;
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

    // Member variables for user dreamCoffee search.
    // No need for final as a search object's fields should probably be mutable to reuse
    // (even if in this case it's not because it's a shared class with the pre-defined menu coffees).
    private float priceMin;
    private float priceMax;

    Coffee(String menuItemID,
           String menuItemName,
           float price,
           int numOfShots,
           boolean sugar,
           DrinkType drinkType,
           Provenance provenance,
           Set<Milk> milkSet,
           Set<String> extrasSet,
           String description) {
        this.menuItemID = menuItemID;
        this.menuItemName = menuItemName;
        this.price = price;
        this.numOfShots = numOfShots;
        this.sugar = sugar;
        this.drinkType = drinkType;
        this.provenance = provenance;
        this.milkSet = milkSet;
        this.extrasSet = extrasSet;
        this.description = description;
    }

    public String coffeeDetailsString() {
        StringBuilder sb = new StringBuilder();
        sb.append(menuItemName).append(" (").append(menuItemID).append(")").append("\n");
        sb.append(description).append("\n");
        sb.append("Ingredients:\n\n");
        //case change drink type to match example?
        sb.append("- Type: ").append(drinkType).append("\n");
        sb.append("- Number of shots: ").append(numOfShots).append("\n");
        sb.append("- Sugar: ").append(sugar ? "yes" : "no").append("\n");
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

    //**********GETTERS**********
    // Filter out unneeded ones after.
    public String getMenuItemID() {
        return menuItemID;
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
        return milkSet;
    }

    public Set<String> getExtrasSet() {
        return extrasSet;
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

    //**********SETTERS**********

    public void setPriceMin(float priceMin) {
        this.priceMin = priceMin;
    }

    public void setPriceMax(float priceMax) {
        this.priceMax = priceMax;
    }
}
