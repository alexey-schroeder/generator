# Historical rarity analysis

Empirical results for `RARE_COMBINATIONS.md`, calculated only from the committed local `src/main/resources/eurojackpot_archiv.csv`.

## Dataset

- Draws: **987**
- Period: **2012-03-23 through 2026-09-04**
- Main pool: **5 of 50**
- Consecutive transitions: **986**
- Source used to refresh the committed file: **LOTTO Bayern EuroJackpot archive**

Severity labels:

```text
< 0.5%  VERY_RARE
< 2%    RARE
< 5%    UNCOMMON
>= 5%   COMMON
```

These labels mean observed historical frequency only. They do not by themselves establish non-randomness or predictive power.

## Strongest observed rarity candidates

| Rule | Definition | Observed |
|---|---|---:|
| R01 | >=4 main numbers repeated from previous draw | 0 / 986 = **0.000%** |
| R01/R04 | >=3 main numbers repeated from previous draw | 4 / 986 = **0.406%** |
| R02 | >=4 main numbers from union of previous two draws | 3 / 985 = **0.305%** |
| R02 | all five from union of previous two draws | 0 / 985 = **0.000%** |
| R06 | >=2 numbers appear in three consecutive draws | 0 / 985 = **0.000%** |
| R07 | sorted L1 distance <=5 | 0 / 986 = **0.000%** |
| R08 | >=4 equal positional shifts | 2 / 986 = **0.203%** |
| R08 | all five positional shifts equal | 0 / 986 = **0.000%** |
| R09 | gap-vector L1 distance <=3 | 2 / 986 = **0.203%** |
| R09 | gap-vector L1 distance <=5 | 3 / 986 = **0.304%** |
| R10 | exact arithmetic progression | 0 / 987 = **0.000%** |
| R10 | near progression, gap range <=2 | 4 / 987 = **0.405%** |
| R12 | run of >=4 consecutive numbers | 1 / 987 = **0.101%** |
| R13/R32 | HIGH sum >=175 -> HIGH sum >=175 | 3 / 986 = **0.304%** |
| R17 | mirror L1 distance <=5 | 2 / 986 = **0.203%** |
| R20 | exact full structural state repeated | 1 / 986 = **0.101%** |
| R24 | all five numbers in one decade | 0 / 987 = **0.000%** |
| R30 | exact five-number main combination repeated | 0 / 987 = **0.000%** |
| R32 | (HIGH sum + wide span) repeated consecutively | 0 / 986 = **0.000%** |

## Rare / soft-penalty candidates

| Rule | Definition | Observed |
|---|---|---:|
| R05 | a number appears in four consecutive draws | 5 / 984 = **0.508%** |
| R07 | sorted L1 distance <=10 | 7 / 986 = **0.710%** |
| R11 | total span <=10 | 5 / 987 = **0.507%** |
| R13/R32 | LOW sum <=80 -> LOW sum <=80 | 8 / 986 = **0.811%** |
| R14/R32 | NARROW span <=20 -> NARROW | 5 / 986 = **0.507%** |
| R14 | WIDE span >=45 -> WIDE | 14 / 986 = **1.420%** |
| R16/R25 | exact decade distribution repeated | 15 / 986 = **1.521%** |
| R17 | mirror L1 distance <=10 | 7 / 986 = **0.710%** |
| R24 | >=4 numbers in one decade | 15 / 987 = **1.520%** |
| R28 | all five numbers in same modulo-3 class | 8 / 987 = **0.811%** |

## Uncommon but weak rules

| Rule | Definition | Observed |
|---|---|---:|
| R11 | four numbers inside width <=8 | 42 / 987 = **4.255%** |
| R12/R26 | consecutive run >=3 | 21 / 987 = **2.128%** |
| R19 | pair completes equal-pause pattern | 34 / 986 = **3.448%** |
| R25 | exact positional parity signature repeated | 32 / 986 = **3.245%** |
| R26 | at least two consecutive adjacencies | 43 / 987 = **4.357%** |
| R27 | >=3 main numbers share the same last digit | 49 / 987 = **4.965%** |
| R29 | maximum adjacent gap >=30 | 40 / 987 = **4.053%** |
| R31 | both Euro numbers repeated immediately | 20 / 986 = **2.028%** |

## Rules that are too common for rejection

| Rule | Definition | Observed |
|---|---|---:|
| R01/R03 | >=2 main numbers repeated from previous draw | 78 / 986 = **7.911%** |
| R03 | pair seen anywhere in previous five draws | 315 / 982 = **32.077%** |
| R04 | triple seen anywhere in previous 13 draws | 57 / 974 = **5.852%** |
| R05 | any number appears in three consecutive draws | 47 / 985 = **4.772%** |
| R11 | three numbers inside width <=4 | 116 / 987 = **11.753%** |
| R13 | isolated LOW sum <=80 | 72 / 987 = **7.295%** |
| R13 | isolated HIGH sum >=175 | 60 / 987 = **6.079%** |
| R14 | isolated NARROW span <=20 | 72 / 987 = **7.295%** |
| R14 | isolated WIDE span >=45 | 101 / 987 = **10.233%** |
| R15 | isolated extreme odd/even count | 381 / 987 = **38.602%** |
| R18 | single number completes equal-pause pattern | 224 / 986 = **22.718%** |
| R20 | same coarse sum+span state repeated | 543 / 986 = **55.071%** |
| R23 | all five main numbers in same 1..25 / 26..50 half | 52 / 987 = **5.268%** |
| R23 | >=4 main numbers in same half | 358 / 987 = **36.272%** |
| R28 | >=4 numbers in one modulo-3 class | 112 / 987 = **11.348%** |
| R29 | maximum adjacent gap >=25 | 121 / 987 = **12.259%** |
| R31 | at least one Euro number repeated immediately | 355 / 986 = **36.004%** |

## Why LOW->LOW and HIGH->HIGH matter

An isolated extreme state and a repeated transition answer different questions. LOW sums occur in **7.295%** of draws, but LOW→LOW occurs in only **0.811%** of transitions. HIGH sums occur in **6.079%** of draws, while HIGH→HIGH occurs in **0.304%** of transitions.

This does not prove dependence between draws. It does show that candidate evaluation should preserve transition features instead of discarding temporal context. The correct follow-up is to compare these transition frequencies with an exact or simulated independent-draw baseline.

The compound rule `(HIGH sum + wide span) -> (HIGH sum + wide span)` occurred **0 / 986** times. This is interesting as a rarity rule but especially vulnerable to post-hoc threshold selection, so it should be validated on an unseen time window before becoming a hard rejection.

## Conditional checks that did not become rare

- `P(overlap >=2 | previous sum <=80)` = **6 / 72 = 8.333%**
- `P(NARROW | previous NARROW)` = **5 / 72 = 6.944%** when expressed conditionally rather than over all transitions
- `P(>=4 odd | previous >=4 odd)` = **43 / 203 = 21.182%**

This distinction is important: `NARROW -> NARROW` is only 5/986 of all transitions, but conditional on already being in NARROW state it is 5/72. Both numbers are valid; they answer different questions.

## New ideas collected from public statistics practice

EuroJackpot statistics sites commonly analyze pairs/triples, even/odd balance, sums, tens groups, consecutive numbers and Euro numbers. Those families are covered by R03/R04, R15, R13/R32, R16/R24/R25, R12/R26 and R31.

Additional experiments retained for future work are robust inter-arrival-gap statistics (CV, MAD, IQR), sum-bucket transition matrices, high/low transition matrices, nearest-historical-draw distance and comparison with exact/random baselines.

## Reproduction

Normal tests require no network access:

```bash
./mvnw test
```

To refresh the committed archive separately:

```bash
python3 scripts/update_eurojackpot_archive.py
```

After refreshing, commit the CSV and run the tests again. This keeps data acquisition separate from statistical verification and makes every test run reproducible.
