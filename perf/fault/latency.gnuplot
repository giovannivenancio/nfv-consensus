set encoding "utf8"
set terminal postscript eps enhanced color font 'Helvetica,24';
set output "/home/gvsouza/projects/nfv-consensus/perf/fault/latency.eps"

set zeroaxis;
set grid ytics
set ytics 0.0005
set grid

set style line 1 lc rgb "#778899"
set style line 2 lc rgb "#CD5C5C"

set style fill solid
set key right top
set key font "1"
set key spacing 1
set boxwidth 0.5

set format xy "%g"

set ylabel 'Latency (ms)'
set xlabel "Number of VNF Faults"
set yrange [0:0.005]

plot "/home/gvsouza/projects/nfv-consensus/perf/fault/latency.dat" using 1:3:xtic(2) with boxes notitle ls 2