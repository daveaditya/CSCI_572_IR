job=$1

job_class=""

# determine the job class to run
if [ "$job" = "unigram" ]
then
  job_class="edu.usc.csci572.unigram.Main"
elif [ "$job" = "bigram" ]
then
  job_class="edu.usc.csci572.bigram.Main"
else
  printf "Unrecognized Job: %s\n" "$job"
  printf "Supported Jobs: unigram, bigram\n"
  exit 1
fi;

rm -rf build # remove previous builds, if any

./gradlew clean build # build the jar

printf "\n"

java $JVM_OPTIONS -cp "${PWD}/build/libs/*" $job_class "${@:2}" # run the jar file