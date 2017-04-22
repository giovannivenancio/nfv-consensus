set encoding "utf8"
set terminal postscript eps enhanced color font 'Helvetica,20';
set output "/home/gvsouza/projects/nfv-consensus/perf/images/cpu.eps"

set style line 1 lt -1 lw 3 linecolor rgb "#6495ED" pi -1
set style line 4 lt -1 lw 3 linecolor rgb "#006400" pi -1
set style line 2 lt -1 pt 7 lw 1 linecolor rgb "#696969" pi -10
set style line 3 lt -1 pt 5 lw 1 linecolor rgb "#CD5C5C" pi -10
set style line 5 lt -1 pt 13 lw 1 linecolor rgb "#000080" pi -10

set zeroaxis;
set grid ytics

set key right top
set key font "0.5"
set key spacing 1
set boxwidth 1

set format xy "%g"

set ylabel 'CPU Usage (%)'
set yrange [0:100]
set ytics 10

set xlabel "Time (s)"
set xrange [0:60]
set xtics 10

plot "/home/gvsouza/projects/nfv-consensus/perf/data/cpu/cpu_vnf/vnfs/vnf.dat" using 1:2 title 'VNF-Consensus' with linespoints ls 3, \
     "/home/gvsouza/projects/nfv-consensus/perf/data/cpu/cpu_controller/controller.dat" using 1:2 title 'Consensus on controller' with linespoints ls 2, \
     "/home/gvsouza/projects/nfv-consensus/perf/data/cpu/cpu_vnf/controllers/controller.dat" using 1:2 title 'Controller' with linespoints ls 5

