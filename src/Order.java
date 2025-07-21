import java.util.Set;

/**
 * @author Ariel Halperin. Made for University of New England course COSC120, Assignment 1.
 * Record of the customer's order, containing all details relevant to order file write-out.
 * @param name String of customer's name
 * @param phoneNo String of customer's phone number in 10 digit format. String due to leading '0' requirement.
 * @param email String of customer's email address.
 * @param menuItemName String of the menuItemName of the Coffee ordered.
 * @param menuItemId String of the menuItemID of the Coffee ordered.
 * @param drinkType DrinkType Enum (with plain-text toString) of the Coffee ordered.
 * @param milk Milk Enum (with plain-text toString) of the Coffee ordered.
 * @param extrasSet Set of Strings of the extras assigned to the Coffee ordered.
 */
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


    /**
     * Modifies default constructor to return defensive copy of extrasSet. Only extrasSet is varied by this.
     * <p>Defensive copy of mutable HashSet after a bit of a Google about mutable types in (otherwise
     * immutable) Records and finding this helpful reply from M A from May 19, 2021 at
     * https://stackoverflow.com/questions/67604105/enforce-immutable-collections-in-a-java-record?noredirect=1&lq=1
     *
     * @param name String of customer's name
     * @param phoneNo String of customer's phone number in 10 digit format. String due to leading '0' requirement.
     * @param email String of customer's email address.
     * @param menuItemName String of the menuItemName of the Coffee ordered.
     * @param menuItemId String of the menuItemID of the Coffee ordered.
     * @param drinkType DrinkType Enum (with plain-text toString) of the Coffee ordered.
     * @param milk Milk Enum (with plain-text toString) of the Coffee ordered.
     * @param extrasSet Set of Strings of the extras assigned to the Coffee ordered.
     */
    public Order {
        extrasSet = Set.copyOf(extrasSet);
    }
}