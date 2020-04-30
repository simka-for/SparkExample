import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Main {

    private static final Pattern SPACE = Pattern.compile("\\s+");

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Usage: JavaWordCount <file> <out_file>");
            System.exit(1);
        }

        SparkSession spark = SparkSession
                .builder()
                .config("spark.master", "local")
                .appName("JavaWordCount")
                .getOrCreate();

        JavaRDD<String> lines = spark.read().textFile(args[0]).javaRDD();
        JavaRDD<String> words = lines.flatMap(s -> Arrays.asList(SPACE.split(s)).iterator());
        JavaPairRDD<String, Integer> ones = words.mapToPair(s -> new Tuple2<>(s, 1));
        JavaPairRDD<String, Integer> counts = ones.reduceByKey(Integer::sum);

        List<Tuple2<String, Integer>> output = counts.collect();
        for (Tuple2<?,?> tuple : output) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }

        counts.saveAsTextFile(args[1]);

        spark.stop();
    }
}
