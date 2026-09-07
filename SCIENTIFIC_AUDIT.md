# Scientific randomness and spectral audit

This report adds a more formal statistical layer to the EuroJackpot rarity experiments.

The current committed archive contains **987 draws**. Spectral discovery uses the first **690 draws (70%)** and validation uses the final **297 draws (30%)** without refitting. Candidate-structure baselines are computed exactly over all `C(50,5) = 2,118,760` possible main-number combinations.

## Spectral / Fourier analysis

For each main number `1..50`, build a binary time series: `1` when the number appeared in a draw, otherwise `0`. Scan periods from **4 to 100 draws**. The strongest period is discovered on the training segment and the same period is evaluated unchanged on the holdout segment.

Aggregate series are also audited: sum, span, high-number count, five sorted positions, and four sorted gaps.

The strongest number-series training peak was number **3**, period **9**, normalized power **7.237331**. However a family-wise Monte-Carlo baseline of random 5-of-50 histories produced a median maximum peak of **8.402500**, a 95th percentile of **10.370981**, and an empirical family-wise `p = 0.876543`. Thus the strongest observed training peak is completely compatible with random 5-of-50 histories.

Number **37** was the most interesting replication candidate. Its training segment selected period **9** with normalized power **6.452667**, and that same period had normalized power **5.210658** on holdout and ranked **100th percentile** among tested holdout periods for that number.

A second Monte-Carlo test explicitly models this selection-and-replication procedure across all 50 numbers. Results:

- observed maximum holdout power at a train-selected period: **5.210658**
- Monte-Carlo median maximum: **4.103661**
- Monte-Carlo 95th percentile: **7.129686**
- family-wise replication `p = 0.247934`
- observed count of numbers whose train-selected period ranked >=95th percentile on holdout: **3**
- Monte-Carlo median count: **3**
- Monte-Carlo 95th percentile count: **6**
- count-based empirical `p = 0.694215`

Conclusion: the period-9 behavior of number 37 is worth monitoring, but after correcting for the fact that many numbers and periods were searched, the current archive does **not** provide significant evidence of a persistent Fourier-periodic signal.

## Autocorrelation

Across all 50 binary occurrence series and lags 1..30, the largest absolute observed autocorrelation was for number **37** at lag **8**, correlation **0.117832**. This is reported as a diagnostic rather than a significance claim; a family-wise Monte-Carlo autocorrelation baseline would be the next formal extension if this feature is used operationally.

## Exact uniform 5-of-50 baselines

For several structural rules, theoretical probability is measured by exact enumeration of all 2,118,760 combinations and compared with the 987 observed draws.

| Rule | Uniform probability | Observed | Expected count | z | nominal p |
|---|---:|---:|---:|---:|---:|
| sum >=175 | 6.582010% | 60 (6.079027%) | 64.964 | -0.6373 | 0.523955 |
| sum <=80 | 6.582010% | 72 (7.294833%) | 64.964 | 0.9031 | 0.366462 |
| span <=20 | 7.591893% | 72 (7.294833%) | 74.932 | -0.3523 | 0.724576 |
| all five in one half | 5.015198% | 52 (5.268490%) | 49.500 | 0.3646 | 0.715414 |
| maximum adjacent gap >=30 | 3.841681% | 40 (4.052685%) | 37.917 | 0.3449 | 0.730169 |
| consecutive run >=3 | 2.346939% | 21 (2.127660%) | 23.164 | -0.4551 | 0.649071 |
| extreme parity (0/1/4/5 odd) | 34.867564% | 381 (38.601824%) | 344.143 | **2.4618** | **0.013824** |
| all five same mod-3 class | 0.790274% | 8 (0.810537%) | 7.800 | 0.0719 | 0.942684 |

Only **extreme parity** is nominally unusual at the ordinary 5% level. However eight structural rules were inspected here, so a simple Bonferroni correction would require `p < 0.00625`; the observed `p = 0.013824` does not clear that threshold. It should therefore be treated as a follow-up hypothesis rather than evidence of non-randomness.

## What survived the scientific audit

The audit currently gives three practical conclusions:

1. **No family-wise significant Fourier signal** is present in the 50 number-occurrence series under the tested period range 4..100.
2. **Number 37 / period about 9** is the most interesting individual replication candidate, but it is not significant after selection correction.
3. Among the tested static structural distributions, **extreme parity** is the only nominal deviation worth following; the other tested rules are strikingly close to exact uniform expectations.

This is important for the filtering project: a pattern can be historically rare yet perfectly consistent with the theoretical 5-of-50 model. Such a pattern may still be useful as a search-space heuristic, but it should not be described as evidence of lottery-machine bias.

## Reproduction

Run the scientific audit:

```bash
./mvnw -Dtest=ScientificRarityAuditTest test
```

Run the stricter spectral replication Monte Carlo:

```bash
./mvnw -Dtest=SpectralReplicationMonteCarloTest test
```

Both are also part of the normal test suite. Current CI result after adding these experiments: **61 tests, 0 failures, 0 errors, BUILD SUCCESS**.
