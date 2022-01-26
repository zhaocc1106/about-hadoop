package org.spark_examples

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

object LogFilterStream {
  def main(args: Array[String]): Unit = {
    println("<<<<<<<<" + args.mkString("Array(", ", ", ")"))
    val conf = new SparkConf().setAppName("LogFilterStream")
    val ssc = new StreamingContext(conf, batchDuration = Seconds(1)) // 创建一个批处理大小为1s的流上下文
    val lines = ssc.socketTextStream("localhost", 7777) // 创建socket输入源，收到的数据创建dstream
    val errorLines = lines.filter(_.contains("error"))
    errorLines.print()
    ssc.start() // 开启stream
    ssc.awaitTermination()
  }
}
