package org.spark_examples

import org.apache.hadoop.mapred.lib.BinaryPartitioner
import org.apache.spark.{SparkConf, SparkContext}

object CalcMean {
  def main(args: Array[String]): Unit = {
    println("<<<<<<<<" + args.mkString("Array(", ", ", ")"))
    val input_file = args(0)
    val output_dir = args(1)
    // 创建spark context
    val conf = new SparkConf().setAppName("calcMean").setMaster("spark://zhaocc-Lenovo-Legion-R7000P2021H:7077") // 根据实际设置spark集群的master
    val sc = new SparkContext(conf)

    // 读取文件
    val input = sc.textFile(input_file)

    // 单词切分
    val words = input.map(line => {
      val arr = line.split(" ")
      (arr(0), arr(1).toInt)
    })
    // words.collectAsMap().map(println(_))

    val result = words.combineByKey(
      (v) => (v, 1), // create combiner，创建key的累加器的初始值，每个分区key首次出现时调用
      (acc: (Int, Int), v) => (acc._1 + v, acc._2 + 1), // merge value，分区中key已经存在时，将新的值累加到累加器中
      (acc1: (Int, Int), acc2: (Int, Int)) => (acc1._1 + acc2._1, acc1._2 + acc2._2), // merge combiners，将两个分区的同一个key的累加器进行累加
    ).map { case (key, value) => (key, value._1 / value._2.toFloat) }
    result.collectAsMap().map(println(_))

    // 统计出来的单词总数保存到文本文件
    // result.saveAsTextFile(output_dir)
  }
}
