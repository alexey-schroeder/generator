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

## Reproduction

The exact current-state enumeration and historical actual-next backtest run as part of the normal local test suite:

```bash
./mvnw test
```

A full search-space enumeration for every historical state is available separately because it requires roughly 985 times more candidate evaluations:

```bash
./mvnw -Drarity.fullBacktest=true -Dtest=RaritySearchSpaceAnalyzerTest test
```

Normal CI intentionally does not run that expensive mode.
