set encoding "utf8"
set terminal postscript eps enhanced color font 'Helvetica,24';
set output "/home/gvsouza/Desktop/fault/throughput.eps"

set zeroaxis;
set grid ytics
set ytics 50
set grid

set style line 1 lc rgb "#778899"
set style line 2 lc rgb "#CD5C5C"

set style fill solid
set key right top
set key font "1"
set key spacing 1
set boxwidth 0.5

set format xy "%g"

set ylabel 'Throughput (consensus/s)'
set xlabel "Number of VNF Faults"
set yrange [0:400]

plot "throughput.dat" using 1:3:xtic(2) with boxes notitle ls 2