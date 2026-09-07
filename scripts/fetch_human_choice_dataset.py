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
    m = re.search(r'Klasse\s*1\s+5\s*\+\s*2\s*EZ.*?\s([0-9.]+)\s+(?:[0-9.]+,[0-9]{2}\s*€|unbesetzt)', plain)
    if not m:
        # Fallback around the first class table row.
        m = re.search(r'Klasse\s*1.*?\|?\s([0-9.]+)\s+(?:[0-9.]+,[0-9]{2}\s*€|unbesetzt)', plain)
    if not m:
        # explicit prose for zero case
        if re.search(r'Gewinnklasse\s*1.*?(?:keinen Gewinn|unbesetzt)', plain, re.I):
            winners = 0
        else:
            raise ValueError('class-1 winners not found')
    else:
        winners = int(m.group(1).replace('.', ''))
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
        'euro_low': sum(x <= 6 for x in euro),
        'euro_contains7': int(7 in euro),
    }


def fetch(url):
    req = urllib.request.Request(url, headers={'User-Agent':'Mozilla/5.0 rarity-research/1.0'})
    with urllib.request.urlopen(req, timeout=30) as r:
        return r.read().decode('utf-8', errors='replace')


def rate_ratio(rows, key, predicate):
    a = [r for r in rows if predicate(r[key])]
    b = [r for r in rows if not predicate(r[key])]
    def stat(g):
        obs = sum(r['winners'] for r in g)
        exp = sum(r['expected_uniform_winners'] for r in g)
        ratio = obs/exp if exp else float('nan')
        return len(g), obs, exp, ratio
    return stat(a), stat(b)


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
        r = {'date':d.isoformat(),'main':'-'.join(map(str,main_nums)),'euro':'-'.join(map(str,euro_nums)),
             'stake':stake,'winners':winners,'expected_uniform_winners':(stake/TIP_PRICE)/ODDS}
        r.update(features(main_nums,euro_nums))
        rows.append(r)
        if i % 50 == 0:
            print(f'HUMAN_FETCH_PROGRESS|done={i}|kept={len(rows)}')
        time.sleep(0.03)

    fieldnames = list(rows[0].keys()) if rows else []
    with OUT.open('w',newline='',encoding='utf-8') as f:
        w=csv.DictWriter(f,fieldnames=fieldnames); w.writeheader(); w.writerows(rows)

    total_obs=sum(r['winners'] for r in rows)
    total_exp=sum(r['expected_uniform_winners'] for r in rows)
    print(f'HUMAN_DATASET|rows={len(rows)}|observedJackpotWins={total_obs}|uniformExpected={total_exp:.3f}|overallRatio={total_obs/total_exp:.4f}')

    checks = [
        ('MAIN_LE31_GE4','main_le31',lambda x:x>=4),
        ('CONTAINS_7','contains7',lambda x:x>=1),
        ('ROUND_GE1','round_count',lambda x:x>=1),
        ('CONSECUTIVE_GE1','consecutive_pairs',lambda x:x>=1),
        ('SAME_LAST_DIGIT_PAIR','same_last_digit_pairs',lambda x:x>=1),
        ('LOW_SUM_LE100','main_sum',lambda x:x<=100),
        ('NARROW_SPAN_LE25','span',lambda x:x<=25),
        ('EURO_LOW_BOTH','euro_low',lambda x:x>=2),
        ('EURO_CONTAINS_7','euro_contains7',lambda x:x>=1),
    ]
    for name,key,pred in checks:
        hi,lo=rate_ratio(rows,key,pred)
        print('HUMAN_FEATURE|name=%s|groupN=%d|groupObs=%d|groupExp=%.3f|groupRatio=%.4f|otherN=%d|otherObs=%d|otherExp=%.3f|otherRatio=%.4f|ratioOfRatios=%.4f' %
              (name,hi[0],hi[1],hi[2],hi[3],lo[0],lo[1],lo[2],lo[3],hi[3]/lo[3] if lo[3] else float('nan')))

if __name__ == '__main__':
    main()
