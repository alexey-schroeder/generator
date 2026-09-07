# Advanced rarity ideas: time-split evaluation

This experiment evaluates additional structural and historical ideas suggested after the positional-percentile work.

Unlike the earlier full-history descriptive backtests, this test uses a fixed chronological time split:

- total archive: **987 draws**
- training period: first **690 draws (70%)**
- holdout period: last **297 draws (30%)**

Thresholds/models are fitted on the training period. The holdout period is then evaluated without refitting. Search-space impact is measured against all `C(50,5) = 2,118,760` main-number combinations for the current state where the rule is state-dependent.

This is still exploratory analysis, but it is more resistant to threshold overfitting than measuring on the same sample used to define a rule.

## Results

| Idea | Holdout winners kept | Current search space rejected | Assessment |
|---|---:|---:|---|
| positional robust median/IQR score | 88.888889% | 10.307066% | close to 90%, worth tuning |
| gap percentile: >=2 gap positions outside 90% intervals | 100.000000% | 0.000991% | safe but uselessly weak |
| rare decade signature | 83.164983% | 16.604523% | too aggressive / unstable |
| gap entropy tails | 89.225589% | 11.461987% | close to 90%, promising as penalty |
| mirror symmetry score | 84.511785% | 11.825407% | too aggressive |
| last-digit concentration | 84.848485% | 13.274746% | too aggressive |
| center-of-gravity delta vs previous draw | 88.513514% | 1.741066% | weak trade-off at tested threshold |
| low pair-compatibility graph score | 41.750842% | 42.303659% | severe non-stationarity; reject as hard rule |
| inter-arrival regularity (CV of number gaps) | 88.888889% | 9.499755% | close to 90%, worth softer threshold |
| structural-state transition rarity | 1.010101% | 96.709868% | extreme sparsity/overfit; reject this representation |
| sum-bucket transition rarity | 88.215488% | 2.043035% | slightly below target, modest value |
| **high/low-count transition rarity** | **91.582492%** | **5.015198%** | **passes 90% holdout target; strongest new clean candidate** |
| divisibility transition rarity | 28.619529% | 57.463375% | sparse/overfit; reject as hard rule |
| age-vector tails | 87.241379% | 11.255498% | interesting but tested threshold too aggressive |
| hot/cold recent-frequency tails | 83.161512% | 17.826417% | unstable; not suitable as hard rule |
| nearest historical sorted-L1 distance | 84.175084% | ~20.267% in deterministic 100k sample | too aggressive at tested cutoff; full space intentionally not enumerated |

## Most useful finding

The best additional idea that clears the user's current `>=90%` holdout keep-rate requirement is the **high/low transition** model.

For each draw, encode only how many of the five main numbers are above 25 (`0..5`). Learn transition counts such as `2 high -> 3 high`, `4 high -> 1 high`, etc. Rare training transitions are filtered.

On the final 297-draw holdout:

- rejected real draws: **25**
- retained real draws: **272**
- holdout keep rate: **91.582492%**

For the current latest-draw state it rejects exactly **106,260 of 2,118,760 candidates = 5.015198%**.

This rule is attractive because its state representation is deliberately low-dimensional. The much richer structural-transition state failed catastrophically: almost every unseen holdout transition looked rare, a textbook sparse-state problem.

## Ideas that are close to the 90% line

Three ideas missed the 90% holdout target only narrowly and are candidates for a softer threshold sweep rather than rejection:

- gap entropy tails: **89.225589% keep**, **11.461987% space reduction**
- positional robust median/IQR score: **88.888889% keep**, **10.307066% reduction**
- inter-arrival regularity: **88.888889% keep**, **9.499755% reduction**

These have substantially larger search-space impact than the high/low transition rule. A threshold sweep analogous to `PositionalCoverageSweepTest` may find operating points just above 90% keep with useful reductions.

Age-vector tails are also interesting (**87.241379% keep**, **11.255498% reduction**) but need a more conservative threshold before they can be considered for a hard-filter layer.

## Negative results are useful

Several ideas should not be promoted as hard filters in their current form:

- pair compatibility based on historical co-occurrence counts changed too much between train and holdout;
- high-dimensional structural transition states were overwhelmingly unseen in holdout;
- divisibility-transition signatures were similarly sparse;
- hot/cold recent-frequency tails were unstable;
- nearest-history distance rejected too many holdout winners at the tested cutoff.

These features may still be useful as soft ranking features, but the current evidence argues against hard rejection.

## Implementation

`AdvancedRarityIdeasTest` contains the experimental implementations and prints one machine-readable `ADV|...` line per idea.

The nearest-history rule is the only one whose candidate-space impact is not exactly enumerated: exact nearest-L1 against roughly 1,000 historical draws for all 2.1M candidates would be unnecessarily expensive for normal CI, so it uses a deterministic 100,000-candidate sample. All other reported candidate-space percentages are exact enumerations.

Run:

```bash
./mvnw -Dtest=AdvancedRarityIdeasTest test
```

The full normal test suite also includes this experiment and currently passes.
