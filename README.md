# Generic Patterns

A small Java library that allows analyzing, testing, and parsing sequences (lists or arrays) of arbitrarily typed objects with expression patterns. 

The patterns are created in Java code and have virtually the same semantics as a RegExp expression.

#### Features

* Ability to create and reuse a pattern applicable to an array or a list of any type;
* Performs a single or a sequential search within the array/list with `Matcher`;
* Allows specifying an element ("token") within an expression by a sample value, a predicate, or a wildcard (roughly analogous to, e.g., `a`, `\w` and `.` in RegExp);
* Supports token quantifiers (similar to `*`, `?`, and `+` in RegExp);
* Supports capturing groups;
* Supports pattern alternation (similar to `[abc]` and `(abc|def)` in RegExp);
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
*Finding matches*

```
public static void main(String args) {
    Integer[] sequence = ArrayUtils.toObject(new int[] {4, 3, 8, 5, 6, 3, 8, 5, 6, 3, 8, 8, 25});
    
    GenericPattern<Integer> pattern = GenericPattern
            .<Integer>instance()
            .token(3)
            .token(8).oneOrMore()
            .token(num -> num % 5 == 0)
            .build();
    
    Matcher<Integer> matcher = pattern.matcher(sequence);
    while (matcher.find()) {
        assert matcher.getGroup() != null;
        Group group = matcher.getGroup();
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
        Group at position 9: [3, 8, 8, 25]
     */
}
```

*Grouping and adding alternatives*
``` 
public static void main(String args) {
        Integer[] sequence = ArrayUtils.toObject(new int[] {4, 3, 8, 5, 6, 3, 8, 5, 6, 3, 8, 8, 5});

        GenericPattern<Integer> pattern = GenericPattern
                .<Integer>instance()
                .any()
                .token(
                    GenericPattern.<Integer>instance()
                    .token(3).or(4)
                    .token(8).oneOrMore()
                    .token(5)
                )
                .build();

        Matcher<Integer> matcher = pattern.matcher(sequence);

        while (matcher.find()) {
            System.out.printf(
                    "Full sequence is [%s] and the capturing group is [%s]%n",
                    matcher.getGroup().getHits(sequence).stream().map(Object::toString).collect(Collectors.joining(", ")),
                    matcher.getGroups().get(1).getHits(sequence).stream().map(Object::toString).collect(Collectors.joining(", ")));
        }

    /*
        Output:
        Full sequence is [4, 3, 8, 5] and the capturing group is [3, 8, 5]
        Full sequence is [6, 3, 8, 5] and the capturing group is [3, 8, 5]
        Full sequence is [6, 3, 8, 8, 5] and the capturing group is [3, 8, 8, 5]
    */
}

```

*Replacing*

```
public static void main(String args) {
    Integer[] sequence = ArrayUtils.toObject(new int[] {4, 3, 8, 5, 6, 3, 8, 5, 6, 3, 8, 8, 5});

    GenericPattern<Integer> pattern = GenericPattern
            .<Integer>instance()
            .token(3)
            .token(8).oneOrMore()
            .token(5)
            .build();

    Matcher<Integer> matcher = pattern.matcher(sequence);

    // Replacing with a pre-defined value
    List<Integer> newSequence = matcher.replaceWithList(Arrays.asList(30, 80, 50));
    System.out.printf(
            "New sequence is [%s]%n",
            newSequence.stream().map(String::valueOf).collect(Collectors.joining(", ")));

    // Replacing with a transformer function
    List<Integer> newSequence2 = matcher.replaceWith(match -> {
        Group group = match.getGroup(0);
        assert group != null;
        return group.getHits(sequence).stream().map(i -> i * 100).collect(Collectors.toList());
    });
    System.out.printf(
            "New sequence is [%s]%n",
            newSequence2.stream().map(String::valueOf).collect(Collectors.joining(", ")));
    /*
        Output:
        New sequence is [4, 30, 80, 50, 6, 30, 80, 50, 6, 30, 80, 50]
        New sequence is [4, 30, 80, 50, 6, 30, 80, 50, 6, 30, 80, 50]
     */
}
```

*Splitting*

```
public static void main(String args) {
    Integer[] sequence = ArrayUtils.toObject(new int[] {4, 3, 8, 5, 6, 3, 8, 5, 6, 3, 8, 8, 7});

    GenericPattern<Integer> pattern = GenericPattern
            .<Integer>instance()
            .token(t -> t == 8 || t == 5).oneOrMore()
            .build();

    Matcher<Integer> matcher = pattern.matcher(sequence);
    Iterator<List<Integer>> iterator = matcher.split();

    while (iterator.hasNext()) {
        List<Integer> subsequence = iterator.next();
        System.out.printf(
                "Subsequence is [%s]%n",
                subsequence.stream().map(String::valueOf).collect(Collectors.joining(", ")));
    }
    /*
        Output:
        Subsequence is [4, 3]
        Subsequence is [6, 3]
        Subsequence is [6, 3]
        Subsequence is [7]
     */
}
```

---
See the [tests folder](src/test/java/com/paperspacecraft/scripting/pattern) for more usage examples.

See Javadoc for the complete explanation.

### Licensing

The project is distributed under the Apache 2.0 license. See [LICENSE](LICENSE) for details. 
