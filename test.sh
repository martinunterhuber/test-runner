#! /bin/bash
# JAVA=11 DIRECTORY=/home/martin/commons-cli PACKAGE=org.apache.commons.cli NAME=commons-io

export JAVA_HOME=/usr/lib/jvm/java-$JAVA-openjdk-amd64
GRADLE=$(ls $DIRECTORY | grep "build.gradle" -c)
MWN_WRAPPER=$(ls $DIRECTORY | grep "mvnw" -c)
RUNNER=$(pwd)
MAX=100

if [ ! -e $RUNNER/results/$NAME ]
then
  mkdir $RUNNER/results/$NAME
fi

cd $DIRECTORY
git clean -f
git reset HEAD --hard
git checkout master
git checkout main
for (( i=0; i < $MAX; ++i ))
do
  cd $DIRECTORY
  git clean -f
  git reset HEAD --hard

  if [ $(git diff --name-only HEAD^ | grep "\.java" -c)  != "0" ]
  then
    RESULT=$RUNNER/results/$NAME/$i
    mkdir $RESULT

    if [ $GRADLE -eq 1 ]
    then
      ./gradlew clean assemble compileTestJava processResources processTestResources > $RESULT/compile.txt 2>&1
    elif [ $MWN_WRAPPER -gt 1 ]
    then
      ./mvnw clean test-compile package -DskipTests dependency:copy-dependencies > $RESULT/compile.txt 2>&1
    else
      mvn clean test-compile package -DskipTests dependency:copy-dependencies > $RESULT/compile.txt 2>&1
    fi

    cd $RUNNER
    ./gradlew doCustomTest --directory=$DIRECTORY --packageName=$PACKAGE --commit="HEAD^" > $RESULT/reduced.txt 2>&1
    ./gradlew doCustomTest --directory=$DIRECTORY --packageName=$PACKAGE --commit="HEAD^" --mutation > $RESULT/mutation.txt 2>&1
    ./gradlew doCustomTest --directory=$DIRECTORY --packageName=$PACKAGE --commit="HEAD^" --mutation --runAll > $RESULT/mutation_all.txt 2>&1

    cd $DIRECTORY
    if [ $GRADLE -eq 1 ]
    then
      ./gradlew test > $RESULT/all.txt 2>&1
    elif [ $MWN_WRAPPER -gt 1 ]
    then
      ./mvnw test > $RESULT/all.txt 2>&1
    else
      mvn test > $RESULT/all.txt 2>&1
    fi
  fi
  git checkout HEAD^
done
