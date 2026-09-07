# Adaptive rarity thresholds

`AdaptiveRarityThresholdService` recalculates empirical rarity thresholds after every completed draw. The caller passes only draws that are known at that moment; the returned snapshot is then used to evaluate candidates for the next draw.

This implements the operational loop:

`new draw -> append to archive -> recalculate thresholds from known history -> evaluate next-draw candidates`

No future draw is used by the service itself.

## Current adaptive rules

The service currently recalculates five main-number rules:

- `ROBUST_POSITIONAL`: median/IQR score across the five sorted positions;
- `GAP_ENTROPY`: central-tail rule for entropy of the four internal gaps;
- `INTERARRIVAL_REGULARITY`: lower-tail rule for average CV of number inter-arrival gaps;
- `AGE_VECTOR`: central-tail rule based on current ages since last occurrence;
- `HIGH_LOW_TRANSITION`: low-dimensional transition state defined by how many of the five main numbers are greater than 25.

Default target keep rate is `0.90`.

## Rolling no-future-leakage test

`AdaptiveRarityThresholdServiceTest.recalculatesAfterEveryDrawWithoutFutureLeakage()` uses a 120-draw warm-up. For every later historical draw it:

1. passes only earlier draws to `recalculate(..., 0.90)`;
2. evaluates the actual next draw;
3. records which adaptive rules would have rejected it.

Current archive: 987 draws. Checked next draws: **867**.

| Rule | Rolling rejected | Rolling keep |
|---|---:|---:|
| ROBUST_POSITIONAL | 90 | **89.619377%** |
| GAP_ENTROPY | 101 | **88.350634%** |
| INTERARRIVAL_REGULARITY | 92 | **89.388697%** |
| AGE_VECTOR | 128 | **85.236448%** |
| HIGH_LOW_TRANSITION | 80 | **90.772780%** |

Important conclusion: although the individual empirical cutoffs are calibrated to a nominal 90% in-sample target, only `HIGH_LOW_TRANSITION` actually clears 90% in this rolling evaluation. The other four rules are too aggressive at their current calibration.

## Current exact candidate-space impact

With a snapshot fitted on all 987 known draws, exact enumeration of all `C(50,5) = 2,118,760` main-number candidates gives:

| Rule | Candidates rejected | Space rejected |
|---|---:|---:|
| ROBUST_POSITIONAL | 210,149 | **9.918490%** |
| GAP_ENTROPY | 237,956 | **11.230909%** |
| INTERARRIVAL_REGULARITY | 173,248 | **8.176858%** |
| AGE_VECTOR | 223,778 | **10.561744%** |
| HIGH_LOW_TRANSITION | 106,260 | **5.015198%** |

The union of all five rejects **722,465 candidates = 34.098482%**.

That union must **not** be treated as a validated 90%-keep hard filter. The five rules are correlated, and four of them already miss the 90% rolling target individually.

## Important methodological distinction

The current service performs **empirical quantile calibration on all known history**. It does not yet perform nested threshold selection on a separate validation window.

Therefore `targetKeepRate=0.90` means “choose an empirical cutoff corresponding to a nominal 90% training-history keep target,” not “guarantee 90% future keep.” The rolling results above demonstrate the difference.

A stronger production-calibration design would, after every new draw:

1. split the currently known history into older training and recent validation portions;
2. sweep candidate thresholds/coverage parameters;
3. choose the strongest search-space reduction satisfying a validation keep constraint such as >=90%, >=95%, or >=99%;
4. refit the feature model on all currently known draws using the selected hyperparameter;
5. use that snapshot for the next draw.

This nested/walk-forward calibration is the preferred next refinement if adaptive rules become production hard filters.

## Integration status

The service and its tests exist and normal CI passes. The archive updater naturally causes tests to be rerun after the committed archive changes.

However, the service is **not yet wired into the main candidate-generation/filter runtime**. It must not be described as an automatic production filter until an application integration hook is added.

## Reproduction

```bash
./mvnw -Dtest=AdaptiveRarityThresholdServiceTest test
```

The CI run that introduced this service passed successfully with 59 tests, 0 failures and 0 errors at that point in the branch history.
