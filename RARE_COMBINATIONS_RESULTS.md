# Historical rarity analysis

Empirical results for the hypotheses in `RARE_COMBINATIONS.md`, calculated from `src/main/resources/eurojackpot_archiv.csv`.

## Dataset

- Draws: **434**
- Period: **2012-03-23 through 2020-10-23**
- Main numbers only: **5 of 50**
- Transitions between consecutive draws: **433**

Severity used by the test harness:

- `< 0.5%` — VERY_RARE
- `< 2%` — RARE
- `< 5%` — UNCOMMON
- `>= 5%` — COMMON

These labels describe observed frequency in this historical sample. They do not by themselves prove causality or non-randomness.

## Strong candidates for filters

| Rule | Operational definition | Observed |
|---|---|---:|
| R01 | >= 3 numbers repeated from previous draw | 1 / 433 = **0.231%** |
| R01 | >= 4 numbers repeated from previous draw | 0 / 433 = **0%** |
| R02 | >= 4 numbers from union of previous 2 draws | 1 / 432 = **0.231%** |
| R02 | all 5 numbers from union of previous 2 draws | 0 / 432 = **0%** |
| R06 | >= 2 numbers appearing in 3 consecutive draws | 0 / 432 = **0%** |
| R07 | sorted L1 distance <= 5 from previous draw | 0 / 433 = **0%** |
| R07 | sorted L1 distance <= 10 | 2 / 433 = **0.462%** |
| R08 | >= 4 identical positional shifts | 1 / 433 = **0.231%** |
| R08 | 5 identical positional shifts | 0 / 433 = **0%** |
| R09 | gap-vector L1 distance <= 5 | 1 / 433 = **0.231%** |
| R10 | exact arithmetic progression | 0 / 434 = **0%** |
| R10 | near progression, gap range <= 2 | 0 / 434 = **0%** |
| R11 | total number span <= 10 | 1 / 434 = **0.230%** |
| R12 | run of >= 4 consecutive numbers | 1 / 434 = **0.230%** |
| R13 | high sum >= 175 repeated in consecutive draws | 1 / 433 = **0.231%** |
| R14 | narrow span <= 20 repeated | 2 / 433 = **0.462%** |
| R17 | mirror L1 distance <= 5 | 0 / 433 = **0%** |
| R17 | mirror L1 distance <= 10 | 2 / 433 = **0.462%** |
| R20 | exact full structural state repeated | 0 / 433 = **0%** |

## Rare / uncommon but not hard-reject candidates

| Rule | Operational definition | Observed |
|---|---|---:|
| R05 | same number appears in 4 consecutive draws | 4 / 431 = **0.928%** |
| R12 | run of >= 3 consecutive numbers | 10 / 434 = **2.304%** |
| R13 | low sum <= 80 repeated | 3 / 433 = **0.693%** |
| R14 | wide span >= 45 repeated | 6 / 433 = **1.386%** |
| R16 | exact decade distribution repeated | 5 / 433 = **1.155%** |
| R19 | pair completes an equal-pause pattern | 12 / 433 = **2.771%** |

## Hypotheses that are too common under the tested definition

| Rule | Operational definition | Observed |
|---|---|---:|
| R01/R03 | pair / >=2 numbers repeated from previous draw | 35 / 433 = **8.083%** |
| R03 | a pair repeated within previous 5 draws | 142 / 429 = **33.100%** |
| R04 | a triple found somewhere in previous 13 draws | 25 / 421 = **5.938%** |
| R05 | any number appears in 3 consecutive draws | 21 / 432 = **4.861%** |
| R11 | 3 numbers inside width <= 4 | 58 / 434 = **13.364%** |
| R13 | isolated low sum <= 80 | 34 / 434 = **7.834%** |
| R13 | isolated high sum >= 175 | 27 / 434 = **6.221%** |
| R14 | isolated narrow span <= 20 | 26 / 434 = **5.991%** |
| R14 | isolated wide span >= 45 | 41 / 434 = **9.447%** |
| R15 | isolated extreme parity (0/1/4/5 odd) | 162 / 434 = **37.327%** |
| R15 | same extreme odd count repeated | 26 / 433 = **6.005%** |
| R18 | number completes equal-pause pattern | 90 / 433 = **20.785%** |
| R20 | same coarse sum+span state repeated | 247 / 433 = **57.044%** |

## Conditional checks

The first conditional definitions tested here did **not** produce rare events:

- `P(overlap >= 2 | previous sum <= 80)` = 2 / 34 = **5.882%**
- `P(narrow span <= 20 | previous narrow span <= 20)` = 2 / 26 = **7.692%**
- `P(>=4 odd | previous >=4 odd)` = 19 / 85 = **22.353%**

This does not invalidate conditional modelling; it means these three specific conditions are not useful rarity filters at the current thresholds.

## Interpretation

The most promising family is **transition geometry**, not isolated draw shape. Very small sorted-vector distance, repeated gap shape, near-constant positional shift, very high overlap with the immediately preceding draws, and repeated extreme states are all historically scarce in this sample.

Several intuitive rules are much weaker than expected. A repeated pair in the immediately previous draw occurs about 8% of the time, a single number lasting three draws occurs about 4.9%, and periodicity of a single number is common under the simple equal-pause definition. These should not be hard rejects.

The next useful step is to compare each rule with a large 5-of-50 Monte Carlo baseline and calculate confidence intervals / expected frequencies. That distinguishes "rare because combinatorics makes it rare" from "rarer in the observed EuroJackpot history than a uniform draw model would predict".

## Reproduction

Run:

```bash
./mvnw -Dtest=RareCombinationHypothesesTest test
```

The test prints machine-readable `REPORT|...` rows for all hypotheses.
