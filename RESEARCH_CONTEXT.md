# Research context and current conclusions

This document is the persistent context for the EuroJackpot rarity/filtering research in this repository. It is intentionally broader than the individual experiment reports so that future work does not depend on recovering decisions, caveats, or negative results from chat history.

## Research goal

The working hypothesis is that EuroJackpot draws may contain unknown non-uniform structure. The project therefore investigates rules that identify structurally or historically rare candidates/transitions and measures whether those rules can reduce candidate space while retaining a chosen fraction of real historical next draws.

This is an experimental hypothesis. Historical rarity or a useful search-space trade-off is **not** evidence by itself that the physical lottery mechanism is non-random.

Two distinct optimization objectives must not be conflated:

1. **Draw-rarity / draw-structure objective:** find candidate regions that appear less compatible with historical or conditional draw behavior.
2. **Human-choice objective:** prefer combinations that other players are less likely to choose, with the goal of reducing prize sharing if a win occurs. This does not increase the physical probability of the draw under an independent lottery mechanism.

## Current game space and archive

Current EuroJackpot rules used by the modern experiments:

- 5 main numbers from `1..50`;
- 2 Euro numbers from `1..12`;
- main-number space: `C(50,5) = 2,118,760`;
- Euro-pair space: `C(12,2) = 66`;
- full 5+2 space: **139,838,160** combinations.

The normalized committed archive is `src/main/resources/eurojackpot_archiv.csv`.

At the time of this document:

- draws: **987**;
- first draw: **2012-03-23**;
- latest draw: **2026-09-04**.

`scripts/update_eurojackpot_archive.py` refreshes the archive from the LOTTO Bayern archive source. Ordinary tests use committed local data only; web acquisition is separate from the test suite.

## Main empirical rule catalog

The repository contains rule families R01-R32 in `RARE_COMBINATIONS.md` and measured frequencies in `RARE_COMBINATIONS_RESULTS.md`.

Particularly rare observed transition examples include:

- overlap >=3 with previous draw: **4 / 986 = 0.405680%**;
- >=4 values from union of previous two draws: **3 / 985 = 0.304569%**;
- high-sum (`sum >=175`) followed by high-sum: **3 / 986 = 0.304260%**;
- low-sum (`sum <=80`) followed by low-sum: **8 / 986 = 0.811359%**;
- narrow span (`<=20`) followed by narrow span: **5 / 986 = 0.507099%**;
- full structural-state repeat: **1 / 986 = 0.101420%**;
- exact 5-main-number historical repeat: **0 / 987**.

The most useful conceptual lesson from this catalog is that **conditional/transition rarity is often more interesting than rarity of a single state**. For example, low sum by itself appears in 7.29% of draws while low -> low appears in only 0.81% of transitions.

High-dimensional state encodings are dangerous: R21 produced 599 unique structural states in only 987 draws, so nearly every state looks “rare.” Sparse-state rarity is therefore not automatically useful.

## Exact overlap baseline

For independent 5-of-50 draws, exact overlap probability is

`C(5,k) * C(45,5-k) / C(50,5)`.

Relevant values:

- `P(k=2) ~= 6.6973%`;
- `P(k=3) ~= 0.4673%`;
- `P(k=4) ~= 0.01062%`;
- `P(k=5) ~= 0.0000472%`.

Thus two repeated numbers are not extreme under the uniform model; three are rare; four or five are extremely rare. Observed rarity must always be compared with this type of theoretical baseline before being interpreted as possible machine bias.

## Hard rarity search-space layer

`RaritySearchSpaceAnalyzerTest` enumerates all 2,118,760 main-number candidates for the latest state and backtests actual next draws.

Current hard-rule union:

- candidate space rejected: **54,293 = 2.562489%**;
- remaining: **2,064,467 = 97.437511%**;
- historical actual-next draws rejected: **19 / 985 = 1.928934%**;
- historical keep: **98.071066%**.

If Euro pairs are unrestricted, the same percentage applies to the full 5+2 space.

This is a conservative layer. Incremental contribution depends on rule order because rules overlap.

### Known analyzer issues to fix before serious global optimization

- **R30 exact repeat semantics:** the current analyzer checks `candidate.equals(previous)` rather than “candidate equals any historical main combination.” The conceptual rule is any historical exact repeat. With 987 unique historical main draws, the true full-history repeat filter would reject up to 987 current candidates rather than only one.
- **R20 semantics:** original descriptive R20 reports one full structural-state repeat, while the analyzer historical actual-next implementation reports zero. Definitions/window/state encoding should be reconciled before relying on R20 in subset optimization.
- **R32 threshold consistency:** wide span is currently treated as `>=45` in the extended rule family. Keep this definition consistent when editing analyzers or docs.

## Broad structural rules

Exact broad-rule measurements show the trade-off between candidate-space reduction and historical retention. A notable same-sample point is:

- `HIGH_OR_SAME_HALF`: rejects **9.469076%** of main-number space and keeps **90.780142%** of historical draws.

This result is useful descriptively but should not be compared as if equivalent to a rolling or fixed-holdout result; it uses the same history to define/evaluate the rule.

## Positional-range family

The strongest well-characterized rule family so far is based on sorted positional ranges.

For each sorted position 1..5, learn a central empirical interval. Reject a candidate when at least two positions are outside their intervals.

Full sweep results are in `POSITIONAL_COVERAGE_SWEEP.md`.

Best tested operating points:

| Minimum rolling keep | Central coverage | Exact main space rejected | Rolling keep |
|---:|---:|---:|---:|
| 99% | 98% | 1.121694% | 99.252935% |
| 97.5% | 96% | 2.292520% | 97.545358% |
| 95% | 94% | 4.346929% | 95.517609% |
| 90% | **89%** | **9.376994%** | **90.608324%** |

For the 90% target, current intervals are:

- position 1: `1-20`;
- position 2: `5-32`;
- position 3: `11-40`;
- position 4: `19-46`;
- position 5: `29-50`.

The next tested point, 88% coverage, rejects 9.834243% but keeps only 89.007471%, so the observed knee is between 88% and 89%.

Important caveat: rolling evaluation avoids direct future leakage, but selecting 89% after inspecting the whole sweep is still second-level tuning on the same overall history. A nested or later-period validation is preferable before treating this as a production hard filter.

## Advanced idea experiment

`ADVANCED_RARITY_IDEAS.md` uses a fixed chronological 70/30 split: 690 train, 297 holdout.

The only tested advanced rule that clearly cleared the >=90% holdout keep target at its tested threshold was:

- **high/low-count transition**: holdout keep **91.582492%**, current exact main-space reduction **5.015198%**.

Near-90 candidates worth threshold tuning:

- gap entropy: 89.225589% keep / 11.461987% reduction;
- robust positional median/IQR: 88.888889% / 10.307066%;
- inter-arrival regularity: 88.888889% / 9.499755%;
- age vector: 87.241379% / 11.255498%.

Poor hard-filter candidates at tested thresholds included pair compatibility, high-dimensional structural transitions, divisibility transitions, hot/cold recent-frequency tails, and nearest-history distance. These may still be soft ranking features.

### Advanced-test methodological caveats

- `HOT_COLD_OVERDUE` threshold derivation in the exploratory test used score history more broadly than the intended training-only split. Treat that result as contaminated by leakage until fixed. It already performs poorly, so it is not a leading candidate.
- Some current-space measurements use a model/state different from the strict training model used for holdout. When a rule is promoted, separate **evaluation model** from **production/current full-history model** explicitly.
- Transition cutoff semantics should be inspected before production promotion. High/low is currently the most robust because the state is deliberately low-dimensional.
- Robust positional empirical quantile ties can cause realized rejection to differ from the nominal target.
- Inter-arrival regularity currently uses a simple CV; trimmed-CV, MAD, and IQR variants remain useful future tests.

## Adaptive threshold recalculation

See `ADAPTIVE_THRESHOLDS.md`.

`AdaptiveRarityThresholdService` recalculates five feature thresholds from the history known after each draw. It is history-only and therefore operationally appropriate for “draw completed -> recalculate for next draw.”

Current rolling results at nominal target keep 90%:

- robust positional: **89.619377%** keep;
- gap entropy: **88.350634%**;
- inter-arrival regularity: **89.388697%**;
- age vector: **85.236448%**;
- high/low transition: **90.772780%**.

Current exact union of all five rejects **34.098482%** of main space and is not validated as a 90%-keep union.

The implementation currently performs in-sample empirical quantile calibration, not nested validation. A 90% target is therefore not a guarantee of 90% future retention. The preferred future version chooses thresholds on a past validation window and then refits on all known history.

The service exists and is tested, but it is **not yet integrated into the main generation/filter runtime**.

## Scientific / theoretical audit

See `SCIENTIFIC_AUDIT.md`.

A Fourier/spectral hypothesis was tested on binary occurrence series for all 50 main numbers plus aggregate series. Discovery used 690 train draws and replication used 297 unseen holdout draws.

Main findings:

- no family-wise significant Fourier peak was found;
- the strongest family-wise train result was fully compatible with random 5-of-50 simulation (`p ~= 0.877`);
- number 37 showed an interesting period near 9 draws and lag-8 autocorrelation, but a selection-aware replication Monte Carlo gave `p ~= 0.248`; count-based replication gave `p ~= 0.694`;
- therefore Fourier is currently a diagnostic/monitoring layer, **not** a hard filter.

Exact theoretical enumeration was also used for several static structural rules. Most observed frequencies matched uniform 5-of-50 expectations closely. The only nominally unusual tested feature was extreme parity (0/1/4/5 odd): observed 38.601824% versus theoretical 34.867564%, nominal `p = 0.013824`. This does not survive a simple Bonferroni correction across the eight inspected rules (`p < 0.00625` required), so it remains only a follow-up hypothesis.

Scientific direction: for each proposed filter, prefer reporting theoretical/Monte-Carlo expectation, observed/expected ratio, uncertainty or empirical p-value, and strict time-split validation.

## Human-choice experiment

See `HUMAN_CHOICE_AUDIT.md`.

A separate dataset was collected for the current 5/50 + 2/12 era beginning 2022-03-25. It contains 465 draws through 2026-09-04 with:

- winning numbers;
- `Spieleinsatz`;
- estimated played lines;
- winner counts for all 12 prize classes;
- exact uniform expected winner counts;
- structural human-choice features.

A ridge `HumanChoiceModel` was fitted on 325 older draws and evaluated on 140 later draws.

Results:

- train correlation with the all-class human-popularity signal: **0.5742**;
- holdout correlation: **0.6852**.

This is evidence that structural combination features contain out-of-sample information about **player choice / winner density**.

However the stronger “physical draws avoid human-like combinations” hypothesis failed:

- mean exact holdout HumanScore percentile among all 139,838,160 combinations: **52.36%**;
- median: **48.93%**;
- rejecting top 10% most human-like space rejected **20 / 140 = 14.29%** of actual holdout draws, leaving only **85.71%**.

Therefore HumanScore must **not** currently be used as a draw-probability anti-pattern filter.

It can still be useful as a secondary expected-payout/crowd-avoidance ranking: among candidates already considered acceptable by draw-structure criteria, prefer lower predicted human popularity to potentially reduce prize sharing.

Recommended conceptual separation:

`DrawRarityScore(candidate)` and `HumanPopularityScore(candidate)` should remain distinct.

## Research methodology rules

These principles should guide future experiments:

- Historical rarity does not prove non-randomness.
- Zero observations do not prove suppression; compare with expected counts under the exact game model.
- Do not multiply feature probabilities as if features were independent.
- Correct for multiple comparisons when scanning many features/frequencies/states.
- Prefer exact combinatorial baselines where feasible and deterministic Monte Carlo otherwise.
- Prefer chronological holdout, rolling, or nested walk-forward validation to same-sample fitting/evaluation.
- Separate main-number and Euro-number analyses because Euro rules/pool changed historically.
- Keep state representations low-dimensional enough to avoid sparse-state overfit.
- Record negative results; they prevent rediscovering failed ideas.
- If a threshold is chosen after looking at a sweep, that selection itself must eventually be validated on unseen data.

## Current practical priorities

The most useful next technical work is:

1. fix R30/R20 analyzer semantics before global hard-rule subset optimization;
2. build nested/walk-forward threshold selection for adaptive near-90 rules;
3. integrate adaptive snapshots into the actual generation/filter runtime only after validation policy is decided;
4. evaluate unions of validated rules, because individual 90% keep rates do not imply a 90% union keep rate;
5. add theoretical/conditional baselines for transition rules such as high->high, low->low, narrow->narrow and high/low-count state transitions;
6. keep HumanChoiceModel as a separate crowd-avoidance ranking objective rather than a physical-draw filter;
7. continue monitoring Fourier number 37 / period ~9 and extreme parity only as pre-registered follow-up hypotheses, not as discovered filters.

## Documentation map

- `RARE_COMBINATIONS.md` — rule definitions R01-R32.
- `RARE_COMBINATIONS_RESULTS.md` — empirical frequencies on the current archive.
- `SEARCH_SPACE_ANALYSIS.md` — exact hard-rule and broad-rule candidate-space analysis.
- `POSITIONAL_COVERAGE_SWEEP.md` — positional threshold sweep and operating points.
- `ADVANCED_RARITY_IDEAS.md` — fixed 70/30 evaluation of 16 additional ideas.
- `ADAPTIVE_THRESHOLDS.md` — per-draw recalculation service and its rolling behavior.
- `SCIENTIFIC_AUDIT.md` — Fourier, Monte Carlo, autocorrelation and exact theoretical baselines.
- `HUMAN_CHOICE_AUDIT.md` — all-class human-choice model and anti-human hypothesis test.
- `DEPENDENCIES.md` — dependency/runtime modernization notes.

When a major experiment changes a conclusion, update both its dedicated report and this context file.
