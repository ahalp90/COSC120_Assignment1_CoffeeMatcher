import java.util.Set;

public record Order(
        String name,
        long phoneNo,
        String email,
        String menuItemName,
        String menuItemId,
        DrinkType drinkType,
        Milk milk, //Make sure to unpack from Set<Milk> when received.
        Set<String> extrasSet
) {
}
// TODO - ORDER DETAILS IN BELOW FORMAT********************
//Order details:
//
//       Name: Walter Shepman (0486756465)
//       Email: drshepman@une.edu.au
//       Item: Mocha (C30213) - hot coffee
//       Milk: Full-cream
//       Extras: Vanilla ice-cream, Chocolate powder