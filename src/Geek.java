import java.util.Set;

public record Geek(
        //should datatypes all be String?

        String name,
        String email,
        //Must convert to String if used for file write-out.
        Coffee coffeeOrdered,
        //Already toString within Enum.
        Milk milk,
        // Not sure of type for extrasSet
        // May want them here in TreeSet (if keeping set) for easy read (alphabetical order) by staff.
        Set<String> extrasSet

) {
}
