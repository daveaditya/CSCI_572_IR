package edu.usc.csci572.unigram;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Inverted Index Job");

        job.setJarByClass(Main.class);
        job.setMapperClass(UnigramIndexJob.UnigramMapper.class);
        job.setReducerClass(UnigramIndexJob.UnigramReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // In and Out Paths
        Path inPath = new Path(args[0]);
        Path outPath = new Path(args[1]);

        // Delete output path if already exists
        FileSystem fs = outPath.getFileSystem(conf);
        if (fs.exists(outPath)) {
            fs.delete(outPath, true);
        }

        FileInputFormat.addInputPath(job, inPath);
        FileOutputFormat.setOutputPath(job, outPath);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}