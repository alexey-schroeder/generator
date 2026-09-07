#!/usr/bin/env python3
import csv
import html
import math
import re
import sys
import time
import urllib.request
from datetime import date
from pathlib import Path

ARCHIVE = Path('src/main/resources/eurojackpot_archiv.csv')
OUT = Path('target/eurojackpot_human_choice.csv')
BASE = 'https://lottozahlen.de/eurojackpot/{date}/'
START = date(2022, 3, 25)  # current 5/50 + 2/12 rules
ODDS = 139_838_160
TIP_PRICE = 2.0

# (main hits, euro hits) for EuroJackpot classes 1..12.
CLASS_HITS = {
    1:(5,2), 2:(5,1), 3:(5,0), 4:(4,2), 5:(4,1), 6:(3,2),
    7:(4,0), 8:(2,2), 9:(3,1), 10:(3,0), 11:(1,2), 12:(2,1),
}


def strip_tags(s: str) -> str:
    s = re.sub(r'<script.*?</script>|<style.*?</style>', ' ', s, flags=re.S|re.I)
    s = re.sub(r'<[^>]+>', ' ', s)
    s = html.unescape(s)
    return re.sub(r'\s+', ' ', s)


def parse_money(x: str) -> float:
    return float(x.replace('.', '').replace(',', '.'))


def parse_page(text: str):
    plain = strip_tags(text)
    m = re.search(r'Spieleinsatz\s+([0-9.]+,[0-9]{2})\s*€', plain)
    if not m:
        raise ValueError('Spieleinsatz not found')
    stake = parse_money(m.group(1))

    winners = {}
    for cls in range(1, 13):
        # The flattened table has: Klasse N ... <winner-count> <quote or unbesetzt>.
        pat = rf'Klasse\s*{cls}\s+.*?\s([0-9.]+)\s+(?:[0-9.]+,[0-9]{{2}}\s*€|unbesetzt)'
        m = re.search(pat, plain)
        if not m:
            raise ValueError(f'class-{cls} winners not found')
        winners[cls] = int(m.group(1).replace('.', ''))
    return stake, winners


def load_draws():
    rows = []
    with ARCHIVE.open(encoding='utf-8') as f:
        for row in csv.reader(f):
            d = date.fromisoformat(row[0][:10])
            if d < START:
                continue
            main = [int(x) for x in row[1].split('-')]
            euro = [int(x) for x in row[2].split('-')]
            rows.append((d, main, euro))
    rows.sort()
    return rows


def features(main, euro):
    s = sorted(main)
    e = sorted(euro)
    gaps = [b-a for a,b in zip(s,s[1:])]
    return {
        'main_le31': sum(x <= 31 for x in s),
        'contains7': int(7 in s),
        'round_count': sum(x % 10 == 0 for x in s),
        'consecutive_pairs': sum(g == 1 for g in gaps),
        'same_last_digit_pairs': sum(1 for i in range(5) for j in range(i+1,5) if s[i] % 10 == s[j] % 10),
        'main_sum': sum(s),
        'span': s[-1] - s[0],
        'odd_count': sum(x % 2 for x in s),
        'high_count': sum(x >= 26 for x in s),
        'euro_low': sum(x <= 6 for x in e),
        'euro_contains7': int(7 in e),
        'euro_sum': sum(e),
        'euro_gap': e[1]-e[0],
    }


def fetch(url):
    req = urllib.request.Request(url, headers={'User-Agent':'Mozilla/5.0 rarity-research/1.0'})
    with urllib.request.urlopen(req, timeout=30) as r:
        return r.read().decode('utf-8', errors='replace')


def class_probability(main_hits, euro_hits):
    # One uniformly selected 5+2 line against one fixed winning 5+2 result.
    main = math.comb(5, main_hits) * math.comb(45, 5-main_hits) / math.comb(50, 5)
    euro = math.comb(2, euro_hits) * math.comb(10, 2-euro_hits) / math.comb(12, 2)
    return main * euro


def main():
    draws = load_draws()
    OUT.parent.mkdir(parents=True, exist_ok=True)
    rows = []
    for i,(d,main_nums,euro_nums) in enumerate(draws,1):
        url = BASE.format(date=d.isoformat())
        try:
            text = fetch(url)
            stake, winners = parse_page(text)
        except Exception as e:
            print(f'HUMAN_FETCH_ERROR|date={d}|error={e}', file=sys.stderr)
            continue
        tips = stake / TIP_PRICE
        r = {
            'date':d.isoformat(), 'main':'-'.join(map(str,main_nums)), 'euro':'-'.join(map(str,euro_nums)),
            'stake':stake, 'estimated_tips':tips,
        }
        for cls,(mh,eh) in CLASS_HITS.items():
            p = class_probability(mh,eh)
            r[f'class{cls}_winners'] = winners[cls]
            r[f'class{cls}_expected_uniform'] = tips * p
        r.update(features(main_nums,euro_nums))
        rows.append(r)
        if i % 50 == 0:
            print(f'HUMAN_FETCH_PROGRESS|done={i}|kept={len(rows)}')
        time.sleep(0.03)

    fieldnames = list(rows[0].keys()) if rows else []
    with OUT.open('w',newline='',encoding='utf-8') as f:
        w=csv.DictWriter(f,fieldnames=fieldnames); w.writeheader(); w.writerows(rows)

    print(f'HUMAN_DATASET_ALL_CLASSES|rows={len(rows)}')
    for cls in range(1,13):
        obs=sum(r[f'class{cls}_winners'] for r in rows)
        exp=sum(r[f'class{cls}_expected_uniform'] for r in rows)
        print(f'HUMAN_CLASS|class={cls}|observed={obs}|expectedUniform={exp:.3f}|ratio={obs/exp:.6f}')

if __name__ == '__main__':
    main()
