set encoding "utf8"
set terminal postscript eps enhanced color font 'Helvetica,20';
set output "/home/gvsouza/projects/nfv-consensus/perf/images/rtt.eps"

set style line 1 lt -1 lw 3 linecolor rgb "#CD5C5C" pi -1
set style line 2 lt -1 pt 7 lw 1 linecolor rgb "#696969" pi -1
set style line 3 lt -1 pt 5 lw 1 linecolor rgb "#CD5C5C" pi -1
set style line 4 lt -1 lw 3 linecolor rgb "#006400" pi -1
set style line 5 lt -1 lw 3 linecolor rgb "#D2691E" pi -1

set zeroaxis;
set grid ytics

set key right top
set key font "0.5"
set key spacing 1
set boxwidth 1

set format xy "%g"

set ylabel 'Flows/s per switch'
set yrange [0:2000]
set ytics 200

set xlabel "Number of switches"
set xrange [1:32]
set xtics (1, 2, 4, 8, 16, 32)
set logscale x

plot "/home/gvsouza/projects/nfv-consensus/perf/data/rtt/vnf.dat" using 1:2 title 'VNF-Consensus' with linespoints ls 3, \
     "/home/gvsouza/projects/nfv-consensus/perf/data/rtt/controller.dat" using 1:2 title 'Consensus on controller' with linespoints ls 2

