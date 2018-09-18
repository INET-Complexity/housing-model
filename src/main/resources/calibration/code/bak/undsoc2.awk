#!/usr/bin/awk -f

BEGIN {
    OFS="\t"
}
NR==1 {
    for (i=1; i<=NF; i++) {
        if($i=="pidp") col[0] = i;
#	if($i=="d_ageif") col[1] = i;
	if($i=="d_birthy") col[1] = i;
	if($i=="d_sex") col[2] = i;
    }
    print "pidp\tage\tsex"
#    print col[0],col[1],col[2]
    
}

NR>1 {
    print $col[0], $col[1], $col[2]
}
