#!/usr/bin/env python3
import csv
import math
from pathlib import Path

DATA = Path('target/eurojackpot_human_choice.csv')
TRAIN_FRACTION = 0.70
RIDGE = 8.0

FEATURES = [
    'main_le31','contains7','round_count','consecutive_pairs','same_last_digit_pairs',
    'main_sum','span','odd_count','high_count','euro_low','euro_contains7','euro_sum','euro_gap'
]


def read_rows():
    out=[]
    with DATA.open(encoding='utf-8') as f:
        for r in csv.DictReader(f):
            q=dict(r)
            for k,v in r.items():
                if k not in ('date','main','euro'):
                    q[k]=float(v)
            out.append(q)
    out.sort(key=lambda r:r['date'])
    return out


def human_signal(r):
    # Combine all 12 classes.  Log ratios are stabilized for sparse upper classes;
    # information weights make upper/middle classes matter without letting class 12 dominate.
    num=den=0.0
    for cls in range(1,13):
        obs=r[f'class{cls}_winners']
        exp=r[f'class{cls}_expected_uniform']
        alpha=max(0.5, min(20.0, math.sqrt(max(exp, 0.0))))
        lr=math.log((obs+alpha)/(exp+alpha))
        info=max(1.0, -math.log10(max(exp/max(r['estimated_tips'],1.0), 1e-15)))
        stability=math.sqrt(max(exp,0.0))/(1.0+math.sqrt(max(exp,0.0)))
        w=info*stability
        num += w*lr
        den += w
    return num/den if den else 0.0


def mean(xs): return sum(xs)/len(xs)

def corr(a,b):
    ma,mb=mean(a),mean(b)
    va=sum((x-ma)**2 for x in a); vb=sum((y-mb)**2 for y in b)
    if va<=0 or vb<=0: return 0.0
    return sum((x-ma)*(y-mb) for x,y in zip(a,b))/math.sqrt(va*vb)


def solve(a,b):
    n=len(b)
    m=[a[i][:]+[b[i]] for i in range(n)]
    for col in range(n):
        pivot=max(range(col,n), key=lambda r:abs(m[r][col]))
        m[col],m[pivot]=m[pivot],m[col]
        d=m[col][col]
        if abs(d)<1e-12: continue
        for j in range(col,n+1): m[col][j]/=d
        for r in range(n):
            if r==col: continue
            f=m[r][col]
            if f==0: continue
            for j in range(col,n+1): m[r][j]-=f*m[col][j]
    return [m[i][n] for i in range(n)]


def fit_ridge(rows):
    mus=[]; sds=[]
    for f in FEATURES:
        vals=[r[f] for r in rows]; mu=mean(vals)
        sd=math.sqrt(sum((x-mu)**2 for x in vals)/max(1,len(vals)-1)) or 1.0
        mus.append(mu); sds.append(sd)
    p=1+len(FEATURES)
    xtx=[[0.0]*p for _ in range(p)]; xty=[0.0]*p
    ys=[human_signal(r) for r in rows]
    for r,y in zip(rows,ys):
        x=[1.0]+[(r[f]-mu)/sd for f,mu,sd in zip(FEATURES,mus,sds)]
        for i in range(p):
            xty[i]+=x[i]*y
            for j in range(p): xtx[i][j]+=x[i]*x[j]
    for i in range(1,p): xtx[i][i]+=RIDGE
    beta=solve(xtx,xty)
    return beta,mus,sds


def predict(r,model):
    beta,mus,sds=model
    return beta[0]+sum(beta[i+1]*(r[f]-mus[i])/sds[i] for i,f in enumerate(FEATURES))


def main_features(main):
    s=sorted(main); gaps=[b-a for a,b in zip(s,s[1:])]
    return {
        'main_le31':sum(x<=31 for x in s),'contains7':int(7 in s),'round_count':sum(x%10==0 for x in s),
        'consecutive_pairs':sum(g==1 for g in gaps),
        'same_last_digit_pairs':sum(1 for i in range(5) for j in range(i+1,5) if s[i]%10==s[j]%10),
        'main_sum':sum(s),'span':s[-1]-s[0],'odd_count':sum(x%2 for x in s),'high_count':sum(x>=26 for x in s)
    }


def euro_features(euro):
    e=sorted(euro)
    return {'euro_low':sum(x<=6 for x in e),'euro_contains7':int(7 in e),'euro_sum':sum(e),'euro_gap':e[1]-e[0]}


def score_parts(model):
    beta,mus,sds=model
    intercept=beta[0]
    main_scores=[]
    for a in range(1,47):
      for b in range(a+1,48):
       for c in range(b+1,49):
        for d in range(c+1,50):
         for e in range(d+1,51):
          r=main_features((a,b,c,d,e)); sc=0.0
          for i,f in enumerate(FEATURES[:9]): sc+=beta[i+1]*(r[f]-mus[i])/sds[i]
          main_scores.append(sc)
    euro_scores=[]
    for a in range(1,12):
      for b in range(a+1,13):
        r=euro_features((a,b)); sc=0.0
        for i,f in enumerate(FEATURES[9:],start=9): sc+=beta[i+1]*(r[f]-mus[i])/sds[i]
        euro_scores.append(sc)
    return intercept,main_scores,euro_scores


def percentile_from_parts(score, intercept, main_scores_sorted, euro_scores):
    # Exact full 5+2 percentile without materializing 139,838,160 scores.
    import bisect
    count=0
    target=score-intercept
    for es in euro_scores:
        count += bisect.bisect_right(main_scores_sorted, target-es)
    return count/(len(main_scores_sorted)*len(euro_scores))


def quantile(xs,q):
    s=sorted(xs); return s[min(len(s)-1,max(0,int(q*(len(s)-1))))]


def main():
    rows=read_rows(); split=int(len(rows)*TRAIN_FRACTION)
    train,hold=rows[:split],rows[split:]
    model=fit_ridge(train)
    train_y=[human_signal(r) for r in train]; hold_y=[human_signal(r) for r in hold]
    train_p=[predict(r,model) for r in train]; hold_p=[predict(r,model) for r in hold]
    beta,_,_=model
    print(f'HUMAN_MODEL_SETUP|rows={len(rows)}|train={len(train)}|holdout={len(hold)}|ridge={RIDGE}')
    print(f'HUMAN_MODEL_FIT|trainCorr={corr(train_y,train_p):.6f}|holdoutCorr={corr(hold_y,hold_p):.6f}')
    for f,b in sorted(zip(FEATURES,beta[1:]),key=lambda x:abs(x[1]),reverse=True):
        print(f'HUMAN_COEF|feature={f}|standardizedBeta={b:.8f}')

    intercept,main_scores,euro_scores=score_parts(model)
    main_scores.sort()
    pct=[percentile_from_parts(p,intercept,main_scores,euro_scores) for p in hold_p]
    avg=mean(pct)
    below50=sum(x<0.5 for x in pct)/len(pct)
    below25=sum(x<0.25 for x in pct)/len(pct)
    above75=sum(x>0.75 for x in pct)/len(pct)
    print(f'HUMAN_HOLDOUT_PERCENTILES|mean={avg:.6f}|median={quantile(pct,.5):.6f}|below50={below50:.6f}|below25={below25:.6f}|above75={above75:.6f}')

    # Test anti-human filters: reject the most human-like top X% of full 5+2 space.
    for reject_top in (0.05,0.10,0.15,0.20,0.25,0.30):
        cutoff=1.0-reject_top
        rejected=sum(x>=cutoff for x in pct)
        keep=1-rejected/len(pct)
        print(f'HUMAN_FILTER|spaceRejectedPct={100*reject_top:.1f}|holdoutRejected={rejected}|holdoutN={len(pct)}|holdoutKeepPct={100*keep:.6f}')

if __name__=='__main__': main()
