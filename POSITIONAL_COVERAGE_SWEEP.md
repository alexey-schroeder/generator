# Positional coverage sweep

This experiment generalizes the positional-range rule for sorted 5-number EuroJackpot main combinations.

For a chosen central empirical coverage `c`, an independent central interval is learned for each sorted position. A candidate is rejected when at least **two of its five sorted positions** fall outside their learned intervals.

Two measurements are reported for every coverage value:

- **Search-space rejected**: exact enumeration of all `C(50,5) = 2,118,760` main-number combinations using intervals learned from the full committed archive.
- **Rolling keep rate**: out-of-sample historical backtest. After an initial 50 draws, each next draw is evaluated using intervals learned only from earlier draws, so future information is not used.

The sweep uses coverage values from 100% down to 80%, with extra half-step points at 97.5%, 92.5%, 87.5%, and 82.5%.

## Sweep results

| Central coverage | Search space rejected | Rolling winners kept |
|---:|---:|---:|
| 100.0% | 0.072590% | 99.893276% |
| 99.0% | 0.509449% | 99.679829% |
| 98.0% | 1.121694% | 99.252935% |
| 97.5% | 1.423474% | 98.719317% |
| 97.0% | 1.903708% | 98.505870% |
| 96.0% | 2.292520% | 97.545358% |
| 95.0% | 3.598661% | 96.371398% |
| 94.0% | 4.346929% | 95.517609% |
| 93.0% | 5.333355% | 94.130203% |
| 92.5% | 5.424210% | 93.596585% |
| 92.0% | 6.142083% | 92.956243% |
| 91.0% | 7.499670% | 92.209178% |
| 90.0% | 8.713021% | 91.355390% |
| 89.0% | **9.376994%** | **90.608324%** |
| 88.0% | 9.834243% | 89.007471% |
| 87.5% | 10.858804% | 88.260406% |
| 87.0% | 11.224584% | 87.620064% |
| 86.0% | 11.671591% | 86.766275% |
| 85.0% | 13.149436% | 86.019210% |
| 84.0% | 14.523353% | 84.311633% |
| 83.0% | 15.412317% | 82.710779% |
| 82.5% | 15.412317% | 81.430096% |
| 82.0% | 18.633635% | 80.469584% |
| 81.0% | 20.424966% | 79.509072% |
| 80.0% | 20.797636% | 77.694771% |

## Best tested points for keep-rate targets

| Minimum rolling keep rate | Best tested central coverage | Search space rejected | Rolling keep rate |
|---:|---:|---:|---:|
| 99.0% | **98.0%** | **23,766 = 1.121694%** | **99.252935%** |
| 97.5% | **96.0%** | **48,573 = 2.292520%** | **97.545358%** |
| 95.0% | **94.0%** | **92,101 = 4.346929%** | **95.517609%** |
| 90.0% | **89.0%** | **198,676 = 9.376994%** | **90.608324%** |

For the user's main target of retaining at least 90% of historical winners, the strongest tested positional rule is therefore the **89% central-coverage rule**.

Its current full-history positional intervals are:

- 1st position: **1-20**
- 2nd position: **5-32**
- 3rd position: **11-40**
- 4th position: **19-46**
- 5th position: **29-50**

A candidate is rejected when at least two sorted values fall outside those intervals. It rejects exactly **198,676 of 2,118,760 combinations (9.376994%)**. In the rolling backtest it rejected **88 of 937** real next draws and retained **849 of 937 (90.608324%)**.

The next tested step, 88% central coverage, rejects slightly more of the search space (**9.834243%**) but falls below the 90% keep-rate target (**89.007471%**). This places the observed knee of this particular rule family between 88% and 89% central coverage.

## Reproduction

Run:

```bash
./mvnw -Dtest=PositionalCoverageSweepTest test
```

The test prints one `POSITION_SWEEP` line per coverage and one `POSITION_SWEEP_BEST` line for each target keep rate.

These are descriptive historical backtests, not evidence that future lottery draws follow the same non-uniform pattern. The rolling design avoids direct future leakage, but threshold selection still uses the same overall history to choose the operating point. A later time-split validation remains useful.
