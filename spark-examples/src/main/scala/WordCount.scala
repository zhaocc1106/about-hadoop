package org.spark_examples

import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]): Unit = {
    println("<<<<<<<<" + args.mkString("Array(", ", ", ")"))
    val input_file = args(0)
    val output_dir = args(1)
    // 创建spark context
    val conf = new SparkConf().setAppName("wordCount").setMaster("spark://zhaocc-Lenovo-Legion-R7000P2021H:7077") // 根据实际设置spark集群的master
    val sc = new SparkContext(conf)

    // 读取文件
    val input = sc.textFile(input_file)

    // 单词切分
    val words = input.flatMap(line => line.split(" "))
    // 转换为键值对并计数
    val counts = words.map(word => (word, 1)).reduceByKey((x, y) => x + y)
    // var counts = words.countByValue() // countByValue直接返回scala map对象，所有数据会出现在内存中，适合小数据统计
    // 统计出来的单词总数保存到文本文件
    counts.saveAsTextFile(output_dir)
  }
}
