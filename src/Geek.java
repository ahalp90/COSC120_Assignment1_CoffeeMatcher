
/**
 * Record containing the details of the user (Geek) ordering a coffee.
 * @param name a String of the user's name in First+Last name format.
 * @param email a String of the user's email address.
 * @param phoneNo a String of the user's phone number in 10 digit format.
 *                Stored as String rather than long because the phone number must have a leading '0'.
 */
public record Geek(String name, String email, String phoneNo) {}