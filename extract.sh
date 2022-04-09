#! /bin/bash
DIR=$(pwd)
RES=$DIR/results
cd $RES
for dirname in $RES/*
do
  if [[ -f $dirname ]]; then
    continue
  fi
  cd $dirname
  echo "test_count;test_count_reduced;line_coverage_reduced;mutations_killed_reduced;test_strength_reduced;line_coverage;mutations_killed;test_strength" > $dirname/total.csv
  for subdirname in $dirname/*
  do
    if [[ -f $subdirname ]]; then
      continue
    fi
    cd $subdirname
    cat reduced.txt | grep -P -o "[0-9]+(?= tests started)" > results2.txt
    cat mutation.txt | grep -B 1 -A 5 "Statistics" | grep -P -o "[0-9]+(?=%)" | tr '\n' ' ' | xargs -n3 > results3.txt
    cat mutation_all.txt | grep -B 1 -A 5 "Statistics" | grep -P -o "[0-9]+(?=%)" | tr '\n' ' ' | xargs -n3 > results4.txt
    count=$(wc -l < results2.txt)
    tac ../all.txt | grep -m 1 "Tests run" | sed -r "s/\x1B\[([0-9]{1,3}(;[0-9]{1,2})?)?[mGK]//g" | grep -P -o "(?<=Tests run: )[0-9]+" > results1.txt
    tests=$(cat results1.txt)
    for (( i=1; i < $count; ++i ));do echo $tests >> results1.txt; done
    paste -d " " results1.txt results2.txt results3.txt results4.txt > results.txt
    cat results.txt | tr ' ' ';' | grep -v ";;" >> $dirname/total.csv
    cd $RES
  done
done
cd $DIR
