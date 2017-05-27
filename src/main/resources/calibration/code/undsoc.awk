#!/usr/bin/awk -f

BEGIN {
    OFS="\t"
}
NR==1 {
    for (i=1; i<=NF; i++) {
        if($i=="pidp") col[0] = i;
	if($i=="a_hidp") col[1] = i;
	if($i=="a_pno") col[2] = i;
	if($i=="a_paygl") col[3] = i;
	if($i=="a_paygwc") col[4] = i;
	if($i=="a_mvyr") col[5] = i;
	if($i=="a_mvmnth") col[6] = i;
	if($i=="a_birthy") col[7] = i;
	if($i=="a_sex") col[8] = i;
    }
    print "pidp\thidp\tpno\tincome\tmovedate\tbirthy\tsex"
#    print col[0],col[1],col[2]
    
}

NR>1 {
    income = $col[3]
    if($col[4] >= 0 && $col[4] != 52) {
	if($col[4] == 5) {
	    income = income * 12.0;
	} else if($col[4] == 0) {
	    income = income *40.0 * 50.0;
	} else if($col[4] < 5) {
	    income = income * 52.0/$col[4];
	} else if($col[4]) {
	    income = -1;
	}
    }

    print $col[0], $col[1], $col[2], income, $col[5]*100+$col[6],$col[7],$col[8]
}
