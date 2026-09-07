# Search-space impact of VERY_RARE rules

This report measures a different quantity from historical rarity. Historical frequency answers how often a pattern occurred in the archive. Search-space impact answers how many of the `C(50,5) = 2,118,760` possible main-number candidates are rejected for a given previous state.

The hard candidate set here contains rules whose observed historical frequency was below 0.5%. Rules in the 0.5%-2% soft-penalty band, such as LOW->LOW, are intentionally excluded.

## Exact impact for the latest committed state

Previous draw: **2026-09-04**.

- Total main-number combinations: **2,118,760**
- Rejected by at least one VERY_RARE rule: **54,293 (2.562489%)**
- Remaining: **2,064,467 (97.437511%)**

The table distinguishes total matches from incremental rejection. Incremental means that the candidate was not already rejected by an earlier rule in the configured rule order.

| Rule | Total matched | Total % | Incremental rejected | Incremental % |
|---|---:|---:|---:|---:|
| R01 overlap >=3 with previous | 10,126 | 0.477921% | 10,126 | 0.477921% |
| R02 >=4 from union previous 2 | 2,996 | 0.141403% | 1,480 | 0.069852% |
| R06 >=2 continuing 3-draw streaks | 17,296 | 0.816327% | 14,063 | 0.663737% |
| R07 sorted L1 <=5 | 1,336 | 0.063056% | 900 | 0.042478% |
| R08 >=4 equal positional shifts | 1,041 | 0.049133% | 940 | 0.044366% |
| R09 gap-vector L1 <=5 | 6,786 | 0.320282% | 5,685 | 0.268317% |
| R10 near arithmetic progression | 16,088 | 0.759312% | 16,005 | 0.755395% |
| R12 consecutive run >=4 | 2,116 | 0.099870% | 1,878 | 0.088637% |
| R13 HIGH->HIGH | 0 | 0.000000% | 0 | 0.000000% |
| R17 mirror L1 <=5 | 1,336 | 0.063056% | 1,325 | 0.062537% |
| R20 same structural state | 1,800 | 0.084955% | 1,531 | 0.072259% |
| R24 all five in one decade | 1,260 | 0.059469% | 360 | 0.016991% |
| R30 exact previous combination | 1 | 0.000047% | 0 | 0.000000% |
| R32 HIGH+wide -> HIGH+wide | 0 | 0.000000% | 0 | 0.000000% |

Two observations matter. First, historical rarity and search-space impact are not the same quantity. R06 never occurred historically in the archive, yet for the current previous-two-draw state it excludes 0.816% of all candidates. R10 occurred in only about 0.405% of historical draws but globally excludes about 0.759% of the 5-of-50 search space.

Second, some rules are state-dependent. HIGH->HIGH currently removes nothing because the latest previous draw is not HIGH. When the previous draw is HIGH, this rule can remove a much larger region of the candidate space.

## Historical next-draw backtest

Every real next draw for which two previous draws were available was checked against the same hard rule set.

- Historical transitions checked: **985**
- Real next draws rejected by at least one hard rule: **19 (1.928934%)**
- Real next draws retained: **966 (98.071066%)**

Per-rule hits on actual next draws:

| Rule | Actual next draws rejected | % of 985 |
|---|---:|---:|
| R01 overlap >=3 | 4 | 0.406091% |
| R02 recent union >=4 | 3 | 0.304569% |
| R06 multi-streak | 0 | 0.000000% |
| R07 sorted L1 <=5 | 0 | 0.000000% |
| R08 equal shifts >=4 | 2 | 0.203046% |
| R09 gap L1 <=5 | 3 | 0.304569% |
| R10 near progression | 4 | 0.406091% |
| R12 run >=4 | 1 | 0.101523% |
| R13 HIGH->HIGH | 3 | 0.304569% |
| R17 mirror L1 <=5 | 2 | 0.203046% |
| R20 same structural state | 0 | 0.000000% |
| R24 one decade | 0 | 0.000000% |
| R30 exact repeat | 0 | 0.000000% |
| R32 HIGH+wide repeated | 0 | 0.000000% |

This is the critical trade-off: the current hard set shrinks today's main-number space by **2.56%**, but historically it would also have discarded **1.93% of the actual observed next draws**. Therefore the full VERY_RARE set should not automatically be treated as a single unconditional hard reject layer. A more defensible next step is to rank rules by search-space reduction versus historical false-rejection rate and use some as penalties rather than hard filters.

## Broad rules with at least about 90% historical keep rate

A second optimization target is more practical than looking only for `<0.5%` historical events: maximize search-space reduction while keeping at least about 90% of historical winners.

Exact enumeration over all `2,118,760` main-number combinations and direct evaluation against all 987 historical draws gives:

| Rule | Search space rejected | Historical winners kept |
|---|---:|---:|
| sum >=175 | **6.582010%** | **93.920973%** |
| sum <=80 | **6.582010%** | **92.705167%** |
| span <=20 | **7.591893%** | **92.705167%** |
| all 5 in one half (1-25 or 26-50) | **5.015198%** | **94.731510%** |
| maximum adjacent gap >=30 | **3.841681%** | **95.947315%** |
| consecutive run >=3 | **2.346939%** | **97.872340%** |
| same-half OR run>=3 | **6.904227%** | **92.806484%** |
| HIGH sum OR same-half | **9.469076%** | **90.780142%** |
| narrow span OR max gap>=30 | **11.433574%** | **88.652482%** |

The strongest currently verified combination that still clears the 90% historical keep-rate target is `HIGH sum >=175 OR all five numbers in one half`. It removes **200,627 combinations (9.469076%)** while retaining **896 of 987 historical draws (90.780142%)**. The `narrow OR max-gap` combination removes more, **11.433574%**, but falls below the 90% target and therefore should not be treated as a qualifying hard filter.

These figures are descriptive backtests on the same historical sample used to define the thresholds. They are useful for search-space engineering, but thresholds should still be checked on a time-split/out-of-sample period before being treated as predictive.

## Positional historical-range rule

For every historical winning combination, sort the five main numbers ascending and look at the minimum and maximum ever observed at each position. On the 987-draw archive the ranges are:

| Sorted position | Historical range |
|---|---:|
| 1st / smallest | **1-38** |
| 2nd | **2-42** |
| 3rd | **4-46** |
| 4th | **9-49** |
| 5th / largest | **13-50** |

The implemented rule rejects a candidate only when **at least two sorted positions are outside their respective historical ranges**.

Exact enumeration of all `C(50,5)` candidates gives:

| Positions outside historical range | Combinations | Share |
|---:|---:|---:|
| 0 | 2,107,581 | 99.472380% |
| 1 | 9,641 | 0.455030% |
| 2 | 1,412 | 0.066643% |
| 3 | 126 | 0.005947% |
| 4 | 0 | 0.000000% |
| 5 | 0 | 0.000000% |

Therefore the `>=2 positions outside range` rule rejects exactly **1,538 combinations = 0.072590%** and leaves **2,117,222 = 99.927410%** of the main-number search space.

A rolling historical backtest was also run without future leakage: after an initial 50 draws, each next draw was tested against positional ranges learned only from earlier draws. Of **937** checked next draws, only **1** would have been rejected. The rolling keep rate is therefore **99.893276%**. Another 12 draws had exactly one position outside the then-known ranges and were correctly retained by the `>=2` threshold.

This rule is exceptionally conservative: its historical false-rejection rate is very low, but its search-space reduction is also very small. It is better suited as a safe auxiliary hard rule than as a major source of search-space reduction.

Implementation: `PositionalRangeRule` derives the five ranges and counts out-of-range positions; `PositionalRangeRuleTest` performs the exact 2.1M-combination enumeration and the rolling time-ordered backtest. `BroadRuleSearchSpaceTest` measures the 90%-keep-rate candidates above.

## Reproduction

The exact current-state enumeration, broad-rule enumeration, positional-range analysis and historical backtests run as part of the normal local test suite:

```bash
./mvnw test
```

A full search-space enumeration for every historical state is available separately because it requires roughly 985 times more candidate evaluations:

```bash
./mvnw -Drarity.fullBacktest=true -Dtest=RaritySearchSpaceAnalyzerTest test
```

Normal CI intentionally does not run that expensive mode.
