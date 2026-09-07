# Human choice audit

This experiment tests two distinct hypotheses:

1. **Human-choice model:** lottery players choose combinations non-uniformly, so the number of winners in the 12 EuroJackpot prize classes should depend on structural features of the winning combination.
2. **Anti-human draw hypothesis:** actual EuroJackpot winning combinations might systematically avoid combinations that humans are more likely to choose.

The second hypothesis is much stronger and is not implied by the first.

## Data

The collector `scripts/fetch_human_choice_dataset.py` uses the current EuroJackpot 5/50 + 2/12 era starting on 2022-03-25. For each draw it records:

- winning 5 main + 2 Euro numbers;
- `Spieleinsatz`;
- estimated number of played lines (`Spieleinsatz / EUR 2`);
- observed winner counts in prize classes 1..12;
- exact uniform expected winner counts for each class;
- structural human-choice features.

Current dataset: **465 draws**, through 2026-09-04.

Across all draws, observed / uniform-expected winner ratios were:

| Class | Observed | Expected | Ratio |
|---|---:|---:|---:|
| 1 | 79 | 69.956 | 1.1293 |
| 2 | 1,439 | 1,399.117 | 1.0285 |
| 3 | 3,258 | 3,148.013 | 1.0349 |
| 4 | 15,767 | 15,740.066 | 1.0017 |
| 5 | 315,017 | 314,801.312 | 1.0007 |
| 6 | 700,625 | 692,562.885 | 1.0116 |
| 7 | 709,203 | 708,302.951 | 1.0013 |
| 8 | 9,995,802 | 9,926,734.692 | 1.0070 |
| 9 | 13,851,531 | 13,851,257.709 | 1.0000 |
| 10 | 31,130,163 | 31,165,329.846 | 0.9989 |
| 11 | 52,338,968 | 52,115,357.131 | 1.0043 |
| 12 | 198,554,495 | 198,534,693.832 | 1.0001 |

The lower classes are extremely close to the exact uniform-ticket baseline in aggregate, while the upper classes are noisier because winner counts are small.

## HumanChoiceModel

`scripts/human_choice_full_audit.py` constructs a stabilized all-class human-popularity signal from observed-vs-uniform winner counts. It then fits a ridge model on the first 70% of draws and evaluates on the last 30% without refitting.

Split:

- train: **325 draws**;
- holdout: **140 draws**.

Predictive result:

- train correlation: **0.5742**;
- holdout correlation: **0.6852**.

Thus the structural features contain genuine out-of-sample information about how many player tickets cluster near a winning combination. This supports the ordinary human-choice-bias hypothesis.

Largest standardized coefficients in the current model:

| Feature | Beta |
|---|---:|
| Euro sum | -0.03612 |
| main-number span | -0.03120 |
| consecutive pairs | -0.03099 |
| main-number sum | -0.02855 |
| Euro contains 7 | +0.02713 |
| high-number count | -0.02505 |
| round-number count | -0.01732 |
| odd count | +0.01468 |
| main contains 7 | +0.01348 |
| same-last-digit pairs | +0.01140 |

The signs are model-specific conditional effects, not universal psychological claims.

## Exact full-space percentile test

Because the fitted score is additive between main-number and Euro-number features, the audit computes each holdout draw's exact percentile among all

`C(50,5) * C(12,2) = 139,838,160`

possible 5+2 combinations without sampling or materializing the full Cartesian product.

For the 140 unseen holdout draws:

- mean human-score percentile: **52.36%**;
- median: **48.93%**;
- below median: **51.43%**;
- below 25th percentile: **21.43%**;
- above 75th percentile: **26.43%**.

This is close to what a uniform draw mechanism would produce. There is no visible tendency for real winning combinations to avoid human-like regions of the space.

## Anti-human filtering result

If the most human-like part of the full 5+2 space is rejected, holdout behavior is:

| Space rejected | Actual holdout draws rejected | Holdout keep |
|---:|---:|---:|
| 5% | 9 / 140 | 93.57% |
| 10% | 20 / 140 | 85.71% |
| 15% | 21 / 140 | 85.00% |
| 20% | 30 / 140 | 78.57% |
| 25% | 37 / 140 | 73.57% |
| 30% | 46 / 140 | 67.14% |

For a useful anti-pattern filter, rejecting the top 10% human-like region would ideally reject fewer than 10% of future real draws. Instead it rejected **14.29%** of the holdout draws. Therefore the current data do **not** support using human-likeness as a draw-probability filter.

## Interpretation

The experiment separates two ideas that are easy to conflate:

- **Supported:** people do not choose tickets uniformly. Structural features predict winner-density out of sample.
- **Not supported:** the physical EuroJackpot draw avoids combinations that people prefer.

The HumanChoiceModel may still be useful for a different objective: among equally likely lottery combinations, prefer combinations with lower predicted player popularity in order to reduce the chance of sharing a prize if a win occurs. That is an expected-payout / crowd-avoidance strategy, not a higher draw-probability strategy.

## Reproduction

```bash
python3 scripts/fetch_human_choice_dataset.py
python3 scripts/human_choice_full_audit.py
```

The dedicated `Human choice audit` GitHub Actions workflow performs both steps and uploads the resulting CSV artifact.
