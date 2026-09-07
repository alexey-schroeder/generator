# Dependency baseline

The project was originally built on a 2020-era stack. The rarity refresh modernizes the build so the analysis can run on a currently supported JDK without the old Lombok/JDK compatibility failure.

## Current versions

| Component | Version | Notes |
|---|---:|---|
| Java | 21 | Current LTS baseline used by CI |
| Spring Boot | 4.1.1 | Current stable Spring Boot release when this refresh was made |
| Lombok | 1.18.48 | Explicit annotation processor configuration |
| JFreeChart | 1.5.6 | Updated from 1.5.0 |
| Maven compiler/surefire | managed by Spring Boot parent | Avoids pinning stale plugin versions |
| JUnit / Mockito / AssertJ | managed by Spring Boot starter-test | Kept version-aligned through Boot dependency management |

Spring Boot dependency management is intentionally used for the libraries it manages. Explicit versions are kept only where the project directly needs to select one (`lombok`, `jfreechart`).

## Migration notes

The old project used Spring Boot 2.2.6.RELEASE, Java 14, Lombok 1.18.12 and JFreeChart 1.5.0. Lombok 1.18.12 failed under modern JDKs during the first CI analysis. Moving the project to Java 21 and the current dependency line removes that historical toolchain constraint.

`EuroJackpotLotteryResultMapper` now accepts both the historical five-column CSV and the normalized three-column archive produced by `scripts/update_eurojackpot_archive.py`. This keeps old data files readable while allowing the current official archive to be stored locally without inventing jackpot-value columns that are not used by the generator.

Two old `CategoryPredictorTest` range maps had been commented out, leaving an empty category and making the tests fail independently of the dependency upgrade. The intended test ranges have been restored.

## Upgrade policy

Dependency upgrades should be followed by:

```bash
./mvnw test
```

The test suite must use the committed local lottery archive and must not depend on network availability. Archive updates are handled separately by `scripts/update_eurojackpot_archive.py` / the archive-update workflow.
