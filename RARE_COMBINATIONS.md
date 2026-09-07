# Rare combinations and transitions

This document is the working catalogue of structural patterns that the generator can measure and potentially penalize.

The project hypothesis is that useful information may exist in the empirical distribution of EuroJackpot draws and, especially, in transitions between consecutive draws. This is a hypothesis to test, not an assumption that any listed pattern is impossible. Every rule must be measured on history before it becomes a filter.

## Data and reproducibility

The committed dataset is `src/main/resources/eurojackpot_archiv.csv`. It currently contains 987 draws from 2012-03-23 through 2026-09-04 and is generated from the public LOTTO Bayern EuroJackpot archive.

Tests read only the committed local CSV. They do not download data. To refresh the archive explicitly, run:

```bash
python3 scripts/update_eurojackpot_archive.py
```

The separate `Update EuroJackpot archive` GitHub Actions workflow can also refresh and commit the file. The normal test workflow remains network-independent.

## Severity convention

Observed historical frequency is currently labelled as:

```text
< 0.5%  -> VERY_RARE
< 2%    -> RARE
< 5%    -> UNCOMMON
>= 5%   -> COMMON
```

These labels describe frequency, not proof of predictability. A future filter should distinguish `ALLOW`, `SOFT_PENALTY`, `STRONG_PENALTY`, and `REJECT` rather than turning every rare event into a hard ban.

## R01 — Large overlap with the previous draw

Measure how many main numbers are shared with the immediately previous draw. Two-number overlap is fairly common; three or more is a much stronger event.

Example:

```text
previous: 3 11 22 34 47
candidate: 3 11 22 34 49
```

## R02 — Saturation from the previous two draws

Measure how many candidate numbers belong to the union of the last two draws. Four or five numbers coming only from that recent union is the interesting tail.

## R03 — Repeated recent pair

Track whether any pair in the candidate occurred together in the previous draw or recent window. The immediate-pair rule is measurable, but history shows that pair repetition is too common to use blindly as a reject.

## R04 — Repeated recent triple

A triple repeated immediately is much rarer than a pair. A triple found anywhere in a long lookback window is much less informative, so recency matters.

## R05 — Single-number streak

Measure whether a number appears in three or four consecutive draws. Three-draw streaks are not exceptional enough for a hard reject; four-draw streaks are rarer.

## R06 — Multi-number streak

Measure whether two or more main numbers survive across three consecutive draws. This is much stronger than a streak of one number.

## R07 — Sorted draw distance

Sort both draws and calculate:

```text
L1 = sum(abs(candidate[i] - previous[i]))
```

A tiny L1 distance catches draws that are nearly the same geometric shape even when exact numbers differ.

## R08 — Constant positional shift

For sorted draws, calculate `candidate[i] - previous[i]`. Four or five identical shifts mean that the candidate is almost a translated copy of the previous draw.

## R09 — Gap-vector similarity

For sorted numbers `x1..x5`, define gaps:

```text
[x2-x1, x3-x2, x4-x3, x5-x4]
```

Compare consecutive gap vectors by L1 distance. Very small distance means unusually similar spacing.

## R10 — Arithmetic or near-arithmetic progression

Measure the range between largest and smallest adjacent gap. Exact arithmetic progression has gap range zero; a very small gap range is a near-progression.

## R11 — Dense local cluster

Useful features include total span, the smallest interval containing three numbers, and the smallest interval containing four numbers.

## R12 — Consecutive-number run

Measure maximum consecutive run length. A run of four is much rarer than a single adjacent pair.

## R13 — Extreme sum and sum transitions

The sum of five main numbers is useful primarily as a state. Current working tails are:

```text
LOW  = sum <= 80
HIGH = sum >= 175
```

A single LOW or HIGH draw is not especially rare. The transition is more interesting:

```text
LOW  -> LOW
HIGH -> HIGH
```

On the 987-draw archive, LOW→LOW occurs 8/986 (0.811%) and HIGH→HIGH occurs 3/986 (0.304%). Therefore HIGH→HIGH is currently a strong rarity candidate; LOW→LOW is a weaker penalty candidate.

## R14 — Extreme span and span transitions

Define:

```text
span = max(main) - min(main)
NARROW = span <= 20
WIDE   = span >= 45
```

Isolated NARROW/WIDE draws are common, while NARROW→NARROW and WIDE→WIDE are much less frequent. This is a good example of why transition rarity can be more useful than state rarity.

## R15 — Extreme odd/even structure

Track odd-count and positional parity signature. Extreme parity by itself is common; an exact positional parity signature repeated in consecutive sorted draws is less common but still usually a soft feature.

## R16 — Distribution across decades

Split main numbers into `1-10`, `11-20`, `21-30`, `31-40`, `41-50` and represent a draw by a five-bin count vector. Exact repetition of the vector is a transition feature.

## R17 — Mirror-like transformation

For 1..50, mirror a number with:

```text
x -> 51 - x
```

Compare the mirrored previous draw with the candidate using sorted L1 distance.

## R18 — Repeated appearance interval

For a number, test whether its latest appearance completes a repeated gap/pause pattern. Simple equal-pause rules are common and should not be hard rejects.

## R19 — Repeated pair appearance interval

Apply the same gap idea to pairs. Pair periodicity is less common than single-number periodicity but still requires calibration.

## R20 — Structural-state transition

Represent a draw by several coarse structural features and compare states between consecutive draws. A state must not be so coarse that almost everything matches, nor so detailed that every state is unique.

## R21 — Historical rarity score

A general mechanism should estimate empirical frequency instead of duplicating thresholds in every filter. The result should include rule name, observed frequency, severity, and explanation.

## R22 — Conditional rarity

Estimate events as conditional transitions, for example:

```text
P(overlap >= 2 | previous sum was LOW)
P(NARROW | previous was NARROW)
P(candidate has >=4 odd | previous had >=4 odd)
```

Conditional rarity is the long-term direction, but a condition is useful only when the denominator is large enough and the event is actually rare.

## R23 — High/low half concentration

Split the pool into `1..25` and `26..50`. Measure 5/0, 0/5 and 4/1-type splits. Current history shows that even all five numbers in one half is above 5%, so this is descriptive rather than a reject rule.

## R24 — Decade concentration

Measure whether four or five numbers fall into the same decade. On current history, four-or-more in one decade is rare enough to be interesting; all five in one decade has not occurred.

## R25 — Repeated decade/parity signatures

Compare exact decade count vectors and exact positional parity strings across consecutive draws. These are compact transition signatures that preserve more structure than a single odd count.

## R26 — Multiple consecutive adjacencies

Besides maximum consecutive run, count the number of adjacent `n,n+1` relations in a draw. Two or more adjacencies can occur without one long run and form a separate structural feature.

## R27 — Last-digit concentration

Measure the maximum amount of numbers sharing the same final decimal digit. Example: `7, 17, 27` has three numbers ending in 7. This is uncommon, but not currently rare enough for a strong reject.

## R28 — Modulo-class concentration

Measure concentration in residue classes such as modulo 3. Four numbers in the same class is common; all five in the same modulo-3 class is much rarer. Modular rules should be kept only when validated, to avoid arbitrary pattern mining.

## R29 — Extreme adjacent gap

Measure the maximum gap between adjacent sorted main numbers. A gap >=30 is uncommon; less extreme thresholds are common. This can complement total span because one huge empty region is different from a generally wide draw.

## R30 — Exact historical combination repeat

Track whether the exact five-number main set has appeared before. There are no exact repeats in the current 987-draw history. This is mathematically expected to be very rare and should eventually be compared with the exact combinatorial baseline before treating it as evidence of any non-uniform process.

## R31 — Euro-number repetition

Analyze the two Euro numbers separately because their pool is much smaller. At least one Euro number repeating immediately is common; both repeating is uncommon. Main-number thresholds must not be copied to Euro numbers.

## R32 — Compound extreme transitions

Combine multiple state constraints when the simple states are too common. Examples:

```text
LOW sum  -> LOW sum
HIGH sum -> HIGH sum
NARROW   -> NARROW
(HIGH sum + WIDE span) -> (HIGH sum + WIDE span)
```

The last compound event occurred 0/986 times in the current history. Compound rules are promising, but they also increase the multiple-comparisons/data-mining risk, so they need out-of-sample validation.

## Additional research directions

Public EuroJackpot statistics commonly analyze pairs/triples, even/odd composition, sum, number groups/decades, consecutive numbers and Euro-number frequencies. Those families are now represented above. Further experiments worth implementing include robust gap regularity (coefficient of variation, MAD and IQR), sum-bucket transition matrices, high/low transition matrices, nearest-historical-draw distance, and exact/random-baseline comparisons.

The most important next step is not inventing more thresholds. For every rule, compare observed frequency with the probability expected from a uniform 5-of-50 process, using exact combinatorics where possible and Monte Carlo otherwise. A pattern can be historically rare simply because the underlying combinatorics make it rare.

## Research discipline

To reduce overfitting, define a rule on one historical period and validate it on a later period. Do not choose thresholds after looking at the full history and then claim the same history as independent confirmation. For large sets of candidate rules, account for multiple comparisons.

## Implementation

Reusable structural calculations live in `src/main/java/com/lottery/generator/rarity/RarityRules.java`.

Empirical checks live in:

```text
src/test/java/com/lottery/generator/RareCombinationHypothesesTest.java
src/test/java/com/lottery/generator/ExtendedRareCombinationHypothesesTest.java
```

Run everything locally with:

```bash
./mvnw test
```

No network access is required for the tests.
