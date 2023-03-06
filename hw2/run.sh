rm -rf build
./gradlew clean build # build the jar
java $JVM_OPTIONS -cp "${PWD}/build/libs/*" edu.usc.csci572.Main "$@" # run the jar file
