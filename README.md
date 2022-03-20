# Generic Patterns

A small Java library that allows analyzing, testing, and parsing sequences (lists or arrays) of arbitrarily typed objects with expression patterns. 

The patterns are created in Java code and have virtually the same semantics as a RegExp expression.

#### Features

* Ability to create and reuse a pattern applicable to an array or a list of any type;
* Performs a single or a sequential search within the array/list with `Matcher`;
* Allows specifying an element ("token") within an expression 
* Supports token quantifiers (similar to `*`, `?`, and `+` in RegExp);
* Supports capturing groups;
* Has positioning constraints (similar to `^` and `$` in RegExp).

*Not yet implemented*

- Look-ahead and look-behind groups;
- A non-greedy flavor of quantifiers.

#### Applications

* Testing arrays/lists for compliance;
* Extracting subarrays/sublists that match some criteria;
* Parsing syntactic structures (scripts, etc.)

Note: currently shipped without optimization; not recommended for high-load use cases.

#### Usage, examples

```java
import com.paperspacecraft.scripting.pattern.GenericPattern;
import com.paperspacecraft.scripting.pattern.Matcher;
import org.apache.commons.lang3.ArrayUtils;

public class Main {
    public static void main(String args) {
        Integer[] sequence = ArrayUtils.toObject(new int[] {4, 3, 8, 5, 6, 3, 8, 5, 6, 3, 8, 8, 5});
        
        GenericPattern<Integer> pattern = GenericPattern
                .<Integer>instance()
                .token(3)
                .token(8).oneOrMore()
                .token(num -> num % 5 == 0)
                .build();
        
        Matcher<Integer> matcher = pattern.matcher(sequence);
        while (matcher.find()) {
            assert matcher.getGroups() != null;
            CapturingGroup group = matcher.getGroups().get(0);
            List<String> numbersInGroup = group.getHits(sequence)
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
            System.out.printf(
                    "Group at position %d: [%s]%n",
                    matcher.getStart(),
                    String.join(", ", numbersInGroup));
        }
        
        /*
            Output:
            Group at position 1: [3, 8, 5]
            Group at position 5: [3, 8, 5]
            Group at position 9: [3, 8, 8, 5]
         */
    }
}
```

See the [tests folder](src/test/java/com/paperspacecraft/scripting/pattern) for more usage examples. See javadocs for the complete explanation.

### Licensing

The project is distributed under the Apache 2.0 license. See [LICENSE](LICENSE) for details. 
