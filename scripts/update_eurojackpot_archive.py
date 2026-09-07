#!/usr/bin/env python3
"""Download the current official LOTTO Bayern EuroJackpot archive and store it locally.

The repository keeps a normalized CSV in src/main/resources/eurojackpot_archiv.csv
so all tests are reproducible and can run without network access.
"""
from __future__ import annotations

import io
import re
import urllib.request
import zipfile
from datetime import datetime
from pathlib import Path

SOURCE_URL = "https://www.lotto-bayern.de/static/gamebroker_2/de/download_files/archiv_eurojackpot.zip"
OUTPUT = Path("src/main/resources/eurojackpot_archiv.csv")


def download() -> bytes:
    request = urllib.request.Request(SOURCE_URL, headers={"User-Agent": "generator-rarity-analysis/1.0"})
    with urllib.request.urlopen(request, timeout=60) as response:
        return response.read()


def decode(raw: bytes) -> str:
    for encoding in ("utf-8-sig", "utf-8", "latin-1", "cp1252"):
        try:
            return raw.decode(encoding)
        except UnicodeDecodeError:
            pass
    return raw.decode("utf-8", errors="replace")


def parse(zip_bytes: bytes):
    with zipfile.ZipFile(io.BytesIO(zip_bytes)) as zf:
        txt_files = [name for name in zf.namelist() if name.lower().endswith(".txt")]
        preferred = [name for name in txt_files if "eurojackpot" in Path(name).name.lower()]
        if not txt_files:
            raise RuntimeError("No text file found in LOTTO Bayern archive")
        raw = zf.read(preferred[0] if preferred else txt_files[0])

    rows = []
    for raw_line in decode(raw).splitlines():
        line = raw_line.strip()
        if not re.match(r"^\d{1,2}\s+\d{1,2}\s+\d{4}\b", line):
            continue
        tokens = re.findall(r"\d+", line)
        if len(tokens) < 10:
            continue
        day, month, year = map(int, tokens[:3])
        values = list(map(int, tokens[3:10]))
        main, euro = values[:5], values[5:7]
        if len(set(main)) != 5 or not all(1 <= n <= 50 for n in main):
            continue
        if len(set(euro)) != 2 or not all(1 <= n <= 12 for n in euro):
            continue
        rows.append((datetime(year, month, day), sorted(main), sorted(euro)))

    by_date = {row[0].date(): row for row in rows}
    rows = sorted(by_date.values(), key=lambda row: row[0], reverse=True)
    if len(rows) < 900:
        raise RuntimeError(f"Archive unexpectedly small: {len(rows)} draws")
    return rows


def write(rows) -> None:
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(
        "".join(
            f"{date.date().isoformat()}T00:00:00Z,{'-'.join(map(str, main))},{'-'.join(map(str, euro))}\n"
            for date, main, euro in rows
        ),
        encoding="utf-8",
    )


def main() -> None:
    rows = parse(download())
    write(rows)
    print(f"CURRENT_ARCHIVE|draws={len(rows)}|from={rows[-1][0].date()}|to={rows[0][0].date()}|source={SOURCE_URL}")


if __name__ == "__main__":
    main()
