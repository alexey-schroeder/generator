# Rare combinations and transitions

This document describes number combinations and transitions between lottery draws that are considered unlikely enough to be rejected or penalized by the generator.

The goal is not to assume that every number is equally likely in every context. The working hypothesis of the project is that real lottery draws may follow an unknown distribution, and that some structural patterns or transitions are significantly rarer than others.

The rules below should be treated as hypotheses. Whenever possible, each rule should later be checked against historical data and assigned an observed frequency or rarity score.

## 1. Large overlap with the previous draw

A candidate is unlikely if too many of its numbers were present in the immediately previous draw.

Examples:

```text
previous: 3, 11, 22, 34, 47
candidate: 3, 11, 22, 34, 49
```

Four repeated numbers should be considered extremely unlikely. Three repeated numbers should receive a strong penalty. Two repeated numbers may be allowed depending on historical frequency.

Possible implementation: `PreviousDrawIntersectionFilter`.

## 2. Large overlap with several recent draws

A candidate may be unlikely even if it does not strongly overlap with one specific draw, but is almost completely composed of numbers from the last few draws.

Example:

```text
draw t-1: 3, 11, 22, 34, 47
draw t-2: 5, 14, 25, 37, 49
candidate: 3, 14, 22, 37, 49
```

The candidate takes five numbers from the union of only two recent draws.

Possible implementation: `RecentDrawUnionFilter`.

## 3. Repeated pair from recent draws

A pair of numbers that appeared together very recently should be considered less likely to appear together again immediately.

Example:

```text
previous: 7, 19, 25, 31, 44
candidate: 7, 19, 28, 36, 48
```

The pair `7, 19` repeats.

Pairs should be tracked independently from individual numbers.

Possible implementation: `RepeatedPairFilter`.

## 4. Repeated triple from recent draws

A triple that appeared in a recent draw and appears again shortly afterwards should receive a very strong penalty.

Example:

```text
previous: 7, 19, 31, 36, 48
candidate: 7, 19, 31, 40, 45
```

Possible implementation: `RepeatedTripleFilter`.

## 5. Number appearing several draws in a row

A number appearing in two consecutive draws and then appearing again in the third draw should be treated as a streak and penalized.

Example:

```text
t-2: ... 17 ...
t-1: ... 17 ...
candidate: ... 17 ...
```

A longer streak should receive a stronger penalty.

Possible implementation: `NumberStreakFilter`.

## 6. Several numbers continuing their streak together

This is stronger than a streak of one number.

Example:

```text
t-2: 5, 17, 23, 34, 46
t-1: 5, 17, 28, 36, 49
candidate: 5, 17, 30, 39, 44
```

Both `5` and `17` would appear three draws in a row.

Possible implementation: `MultiNumberStreakFilter`.

## 7. Candidate too similar to the previous sorted draw

Two draws can be structurally very similar without sharing exact numbers.

Example:

```text
previous: 4, 11, 25, 38, 47
candidate: 5, 12, 24, 39, 46
```

After sorting, calculate:

```text
sum(abs(candidate[i] - previous[i]))
```

A very small distance means that the whole draw is almost a shifted copy of the previous one.

Possible implementation: `DrawDistanceFilter`.

## 8. Almost constant shift of the previous draw

A candidate may look like a translated copy of a recent draw.

Example:

```text
previous: 5, 13, 21, 34, 45
candidate: 7, 15, 23, 36, 47
shift:    +2, +2, +2, +2, +2
```

Four or five nearly identical shifts should be considered unlikely.

Possible implementation: `VectorShiftFilter`.

## 9. Similar gap pattern

For a sorted draw, define gaps between neighboring numbers.

Example:

```text
numbers: 4, 11, 25, 38, 47
gaps:       7, 14, 13, 9
```

A candidate with almost the same gap vector as one of the recent draws may represent a rare structural repetition.

Possible implementation: `GapSimilarityFilter`.

## 10. Arithmetic or almost arithmetic progression

Highly regular spacing should be considered unlikely.

Examples:

```text
3, 13, 23, 33, 43
5, 10, 15, 20, 25
```

Also penalize near-progressions where the gaps differ only slightly.

Possible implementation: `ArithmeticProgressionFilter`.

## 11. Very dense local cluster

Too many numbers concentrated inside a small interval may be rare.

Example:

```text
21, 22, 23, 25, 27
```

Useful features:

- total span: `max - min`
- smallest interval containing 3 numbers
- smallest interval containing 4 numbers
- number of adjacent or near-adjacent pairs

Possible implementation: `DenseClusterFilter`.

## 12. Too many consecutive numbers

Candidates with long consecutive runs should be penalized.

Examples:

```text
11, 12, 13, 34, 47
21, 22, 23, 24, 49
```

A run of four consecutive numbers should be treated much more strongly than a single pair.

Possible implementation: `ConsecutiveNumbersFilter`.

## 13. Repeating an extreme sum state

The absolute sum of a draw is useful, but transitions may be more informative than the value itself.

Example:

```text
previous draw: extremely low sum
candidate:     extremely low sum
```

Two consecutive draws in the same extreme tail may be rarer than one isolated extreme draw.

Possible implementation: `SumTransitionFilter`.

## 14. Repeating an extreme span state

Define span as:

```text
max(number) - min(number)
```

Two consecutive draws with unusually narrow or unusually wide span may be unlikely.

Possible implementation: `SpanTransitionFilter`.

## 15. Repeating an extreme odd/even pattern

Examples of extreme states:

```text
5 odd, 0 even
0 odd, 5 even
4 odd, 1 even
```

The main target is not necessarily a single extreme draw, but repeating the same extreme parity structure in consecutive draws.

Possible implementation: `ParityTransitionFilter`.

## 16. Repeating the same distribution across number ranges

Split the number space into ranges, for example:

```text
1-10
11-20
21-30
31-40
41-50
```

Represent each draw by counts per range.

Example:

```text
[1, 1, 1, 1, 1]
```

or

```text
[0, 0, 4, 1, 0]
```

An unusual range-distribution pattern repeating immediately may be unlikely.

Possible implementation: `RangeDistributionTransitionFilter`.

## 17. Mirror-like transformation

A candidate may be structurally related to a previous draw by reflection around the number range.

For a 1..50 game, one possible transformation is:

```text
x -> 51 - x
```

Example:

```text
previous: 3, 10, 21, 35, 48
mirror:   3, 16, 30, 41, 48
```

Exact or near mirror patterns should be tested historically.

Possible implementation: `MirrorPatternFilter`.

## 18. Repeated appearance interval

A number may create an overly regular periodic pattern.

Example:

```text
17 appears
6 draws absent
17 appears
6 draws absent
candidate contains 17
```

The same idea can be applied to pairs and triples.

Possible implementation: `RepeatedPauseFilter`.

## 19. Repeated pair appearance interval

A pair that repeatedly appears after the same or nearly the same number of draws may form an unlikely periodic pattern.

Possible implementation: `RepeatedPairPauseFilter`.

## 20. Rare transition between structural states

Instead of evaluating only the candidate, evaluate the transition from the previous draw to the candidate.

Examples:

```text
5 odd -> 5 odd
very low sum -> very low sum
very narrow span -> very narrow span
3 consecutive numbers -> 3 consecutive numbers
```

The probability of a transition should ideally be learned from historical data.

Possible implementation: `ConditionalTransitionRarityFilter`.

## 21. General historical rarity score

Many rules above should eventually use one common rarity mechanism instead of hard-coded thresholds.

For a feature or transition, calculate its observed historical frequency:

```text
observedCount / possibleCount
```

Suggested interpretation:

```text
< 0.5%  -> very strong reject candidate
< 2%    -> strong penalty
< 5%    -> moderate penalty
>= 5%   -> normally allowed
```

These thresholds are provisional and should be calibrated from data.

Possible implementation: `HistoricalRarityFilter`.

## 22. Conditional rarity

The most important long-term direction is to estimate probabilities conditionally rather than globally.

Examples:

```text
P(candidate has 3 odd | previous draw had 5 odd)
P(candidate repeats 2 numbers | previous draw had very low sum)
P(candidate has narrow span | previous draw also had narrow span)
```

This allows the project to model an unknown distribution of transitions between draws rather than only frequencies of isolated combinations.

Possible implementation: `ConditionalRarityModel`.

## Rule severity

Rules should eventually return a rarity score or severity instead of only `true/false`.

Suggested levels:

```text
ALLOW
SOFT_PENALTY
STRONG_PENALTY
REJECT
```

A combination may accumulate several individually weak anomalies and become unlikely overall.

## Open questions

For every rule we should later answer:

1. How often does the pattern occur in historical EuroJackpot data?
2. How often does it occur in a purely random simulation?
3. Does its frequency change over time?
4. Is the rule stable across different historical windows?
5. Should the rule be a hard rejection or only contribute to a combined rarity score?
