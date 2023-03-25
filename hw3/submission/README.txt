Dear Instructors,

The submitted code was ran on the "ORIGINAL DATASET" provided i.e. data.zip.
The code.zip contains the source code which is a gradle project.
Java version used 19.0.1.

Note:
For the sake of completeness, I have also run the code for the new dataset (shortened dataset),
the output of the same can be found in the new_dataset directory.

The source files can be found as under:

1. Unigram Index Job:

Code File Path: src/main/java/edu/usc/csci572/unigram/UnigramIndexJob.java
Driver File Path: src/main/java/edu/usc/csci572/unigram/Main.java

2. Bigram Index Job:

Code File Path: src/main/java/edu/usc/csci572/bigram/BigramIndexJob.java
Driver File Path: src/main/java/edu/usc/csci572/bigram/Main.java


######


Some Useful Commands:

Build the Project: ./gradlew clean build

Run
	1. Unigram Index Job:
		 java $JVM_OPTIONS -cp "${PWD}/build/libs/*" edu.usc.csci572.unigram.Main in out

	2. Bigram:
		java $JVM_OPTIONS -cp "${PWD}/build/libs/*" edu.usc.csci572.bigram.Main in out

OR using run.sh script

1. Unigram: ./run.sh unigram --in in --out out
1. Bigram: ./run.sh bigram --in in --out out