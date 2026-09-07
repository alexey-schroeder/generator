# Combined candidate YES/NO lift audit

This experiment directly tests the proposed service model:

`candidate -> positional constraint -> structural/transition filters -> YES/NO`

The target metric is not merely search-space reduction. It is probability concentration:

`lift = actual-next-draw keep rate / accepted candidate-space rate`

A lift above 1 means historical future draws were more concentrated in the service's YES region than uniform candidate combinations. A lift below 1 means the service made the odds worse.

## Evaluation design

Implementation: `CombinedCandidateLiftAuditTest`.

- archive: 987 draws;
- warm-up: 120 draws;
- evaluated next draws: 867;
- rule-selection segment: first 520 evaluated draws (60%);
- untouched holdout: final 347 evaluated draws (40%);
- candidate-space rate during walk-forward evaluation: deterministic uniform reservoir sample of 10,000 of all `C(50,5)=2,118,760` main combinations, seed `20260908`;
- for every historical state, positional bounds are fitted using only earlier draws;
- rule subsets are greedily selected on the 520-draw selection segment only;
- the selected subset is then evaluated unchanged on the final 347 draws;
- final current-state accepted space for selected configurations is enumerated exactly over all 2,118,760 main combinations.

This is stricter than earlier same-sample or full-history trade-off tables. It explicitly tests whether a discovered YES/NO policy transfers to later unseen draws.

Known ambiguous/contaminated rules are intentionally excluded from subset optimization: analyzer R20/R30 semantics still need cleanup, and exploratory HOT_COLD has known leakage. The audit uses interpretable positional, transition and structural rules.

## Positional bases alone

| Positional base | Holdout space accepted | Holdout real draws kept | Holdout lift | Gain |
|---|---:|---:|---:|---:|
| none | 100.0000% | 100.0000% | 1.000000 | 0.00% |
| 98% central, reject >=2 outside | 98.9076% | 99.7118% | **1.008131** | **+0.813%** |
| 95% central, reject >=2 outside | 96.3569% | 96.5418% | **1.001918** | **+0.192%** |
| 95% central, require every position inside | 87.4106% | 87.8963% | **1.005556** | **+0.556%** |
| 89% central, reject >=2 outside | 90.6817% | 90.7781% | **1.001063** | **+0.106%** |

The important point is that stricter positional filtering does not create a large probability gain. The best holdout result in this family is the conservative 98%-coverage soft rule, about +0.81%.

## Rule subset selection results

The optimizer was allowed to add rules only while satisfying the requested keep target on the selection period. This produces attractive in-selection lifts, but the untouched holdout reveals how much of that gain was selection noise.

### Target keep 99%

| Base | Selected rules | Selection lift | Holdout lift | Holdout gain |
|---|---|---:|---:|---:|
| none | overlap3, recent-union4, multi-streak, sorted-L1, near-progression, run4, one-decade | 1.012866 | 0.999255 | **-0.075%** |
| 98% soft positional | none | 1.003921 | **1.008131** | **+0.813%** |
| 95% soft positional | none | 0.997620 | **1.001918** | **+0.192%** |

The selected rare-rule union looked +1.29% on the selection period but disappeared on holdout. The simple conservative positional base generalized better.

### Target keep 95%

| Base | Selected rules | Selection lift | Holdout lift | Holdout gain |
|---|---|---:|---:|---:|
| none | multi-streak, sorted-L1, near-progression, one-decade, max-gap30, run3 | 1.022436 | 0.980131 | **-1.987%** |
| 98% soft positional | overlap3, recent-union4, multi-streak, sorted-L1, near-progression, one-decade, run3 | 1.022640 | 1.000308 | **+0.031%** |
| 95% soft positional | overlap3, recent-union4, multi-streak, sorted-L1, near-progression, run4, one-decade, high-high | 1.009845 | **1.007873** | **+0.787%** |

The best 95%-target holdout configuration is the 95%-soft positional base plus a conservative rare-transition/structure subset. Its holdout lift is about +0.79%.

For the current latest state, exact enumeration accepts 2,001,533 main combinations = **94.467188%** of the space. Using the 95.389049% holdout keep rate gives a current-state implied lift of **1.009759 (+0.976%)**. This current-state implied number mixes a historical holdout numerator with today's exact denominator, so the more defensible validation statistic remains the holdout lift +0.787%.

### Target keep 90%

All tested optimized configurations failed to generalize:

| Base | Selection lift | Holdout lift | Holdout gain |
|---|---:|---:|---:|
| none | 1.028596 | 0.975354 | **-2.465%** |
| 98% soft positional | 1.033142 | 0.979451 | **-2.055%** |
| 95% soft positional | 1.025686 | 0.988519 | **-1.148%** |

This is a strong warning against aggressive filtering. The optimizer can easily find a historical subset that looks +2-3% better in-sample, but later draws do not preserve the gain.

## User-proposed manual combinations

The audit explicitly tested the idea "choose numbers from positional ranges, then ban intuitive structures such as all-even/all-odd, extreme sums/spans/gaps/runs."

### Only ban all-same parity after positional filtering

| Base | Holdout accepted space | Holdout keep | Lift | Gain |
|---|---:|---:|---:|---:|
| 95% strict positional | 82.6989% | 84.1499% | **1.017545** | **+1.755%** |
| 95% soft positional | 91.3361% | 92.2190% | **1.009666** | **+0.967%** |
| 89% soft positional | 85.8650% | 87.0317% | **1.013588** | **+1.359%** |

These are the largest positive holdout lifts in this particular manual table. However they were not pre-registered before seeing the final results and are based on only 347 holdout draws, so they should be treated as new hypotheses for future validation, not as established probability improvements.

### Intuitive multi-rule bundle

Rules: all-same parity + high sum >=175 + narrow span <=20 + max gap >=30 + run >=3.

| Base | Holdout lift | Gain |
|---|---:|---:|
| 95% strict positional | 0.973525 | **-2.648%** |
| 95% soft positional | 0.953744 | **-4.626%** |
| 89% soft positional | 0.955477 | **-4.452%** |

Adding many plausible-looking bans makes the service materially worse on unseen history. This directly confirms that rule count or search-space reduction is not the objective; validated lift is.

## Practical conclusion

The current evidence does **not** support an aggressive YES/NO probability service. The robust-looking gains are still small, around 0-1%.

The best defensible operating style today is conservative:

1. use a broad positional acceptance region rather than forcing all numbers into narrow buckets;
2. add only a small set of rules whose combined behavior is validated on later data;
3. target very high keep rates (about 95-99%), not 90%;
4. report lift and uncertainty, not only percentage of combinations rejected;
5. freeze promising configurations and validate them prospectively on genuinely new draws.

Interesting follow-up hypotheses from this audit are:

- 98% soft positional alone: holdout lift **+0.813%**;
- 95% soft positional + conservative rare-rule subset: **+0.787%** holdout lift;
- positional + all-same-parity ban: **+0.97% to +1.75%** depending on positional base, but this must be independently replicated because it was inspected after the fact.

At EuroJackpot jackpot scale even a true +1% lift remains a very small absolute probability change. The primary scientific question is therefore whether any lift remains above 1 after additional unseen draws and uncertainty correction.

## Reproduction

```bash
./mvnw -Dtest=CombinedCandidateLiftAuditTest test
```

The full CI run containing this audit passed with **62 tests, 0 failures, 0 errors**.
