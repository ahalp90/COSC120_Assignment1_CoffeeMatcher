import java.util.HashSet;
import java.util.Set;

public record Order(
        String name,
        String phoneNo,
        String email,
        String menuItemName,
        String menuItemId,
        DrinkType drinkType,
        Milk milk, //Make sure to unpack from Set<Milk> when received.
        Set<String> extrasSet
) {
    // Defensive copy of mutable HashSet after a bit of a Google about mutable types in (otherwise
    // immutable) Records and finding this helpful reply from M A from May 19, 2021 at
    // https://stackoverflow.com/questions/67604105/enforce-immutable-collections-in-a-java-record?noredirect=1&lq=1
    public Order {
        extrasSet = Set.copyOf(extrasSet);
    }
}