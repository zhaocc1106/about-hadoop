package org.spark_examples

import org.apache.spark.{HashPartitioner, SparkConf, SparkContext}

object PageRank {
  def main(args: Array[String]): Unit = {
    println("<<<<<<<<" + args.mkString("Array(", ", ", ")"))
    val input_file = args(0)
    val output_dir = args(1)
    // 创建spark context
    val conf = new SparkConf().setAppName("pageRank") // 根据实际设置spark集群的master
    val sc = new SparkContext(conf)

    var links = sc.textFile(input_file)
      .map(line => {
        val arr = line.split(" ")
        (arr(0), arr.drop(0))
      }).partitionBy(new HashPartitioner(2)) // 提前设置分区，后面操作会使用该分区方式
      .persist() // RDD缓存到memory

    var ranks = links.mapValues(v => 1.0) // 每个链接初始rank值

    // 执行10次page rank迭代
    for (i <- 0 until 10) {
      // 每个链接对附近的链接的贡献值为rank(p) / numNeighbors(p)
      // 计算每个链接收到的贡献值
      val contributions = links.join(ranks).flatMap {
        case (pageId, (links, rank)) => links.map(dest => (dest, rank / links.size))
      }
      // 计算每个链接的rank值
      ranks = contributions.reduceByKey((x, y) => x + y).mapValues(v => 0.15 + 0.85 * v)
    }
    ranks.saveAsTextFile(output_dir + "pages")
  }
}
