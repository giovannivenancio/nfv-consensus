set encoding "utf8"
set terminal postscript eps enhanced color font 'Helvetica,20';
set output "/home/gvsouza/projects/nfv-consensus/perf/images/scalability_number.eps"

set style line 1 lt -1 lw 3 linecolor rgb "#CD5C5C" pi -1
set style line 2 lt -1 pt 7 lw 2 linecolor rgb "#696969" pi -1
set style line 3 lt -1 pt 5 lw 2 linecolor rgb "#CD5C5C" pi -1
set style line 4 lt -1 lw 3 linecolor rgb "#006400" pi -1
set style line 5 lt -1 lw 3 linecolor rgb "#D2691E" pi -1

set zeroaxis;
set grid ytics

set key right top
set key font "0.5"
set key spacing 1
set boxwidth 1

set format xy "%g"

set ylabel 'Average throughput (s)'
set yrange [0:600]
set ytics 50

set xlabel "Number of controllers"
set xrange [2:10]
set xtics (2, 3, 4, 5, 6, 7, 8, 9, 10)

plot "/home/gvsouza/projects/nfv-consensus/perf/data/scalability/vnf_number.dat" using 1:2 title 'VNF-Consensus' with linespoints ls 3, \
     "/home/gvsouza/projects/nfv-consensus/perf/data/scalability/controller_number.dat" using 1:2 title 'Consensus on controller' with linespoints ls 2
