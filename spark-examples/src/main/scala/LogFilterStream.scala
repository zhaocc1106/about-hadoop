package org.spark_examples

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object LogFilterStream {
  def main(args: Array[String]): Unit = {
    println("<<<<<<<<" + args.mkString("Array(", ", ", ")"))
    val conf = new SparkConf().setAppName("LogFilterStream")
    val ssc = new StreamingContext(conf, batchDuration = Seconds(1)) // 创建一个批处理大小为1s的流上下文
    //    val lines = ssc.socketTextStream("localhost", 7777) // 创建socket输入源，收到的数据创建dstream

    // 创建kafka数据源
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "kafka-1:9092,kafka-2:9092,kafka-3:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "use_a_separate_group_id_for_each_stream",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val topics = Array("sparkStreamTopic")
    val lines = KafkaUtils.createDirectStream[String, String](ssc, PreferConsistent, Subscribe[String, String](topics, kafkaParams)).map(record => record.value())

    val errorLines = lines.filter(_.contains("error"))
    errorLines.print()
    ssc.checkpoint("/tmp/spark_stream_ckpt")
    val errorLinesWindow = errorLines.window(Seconds(3), Seconds(2)) // 通过滑动窗口统计最近3s的error log个数
    errorLinesWindow.count().print()
    ssc.start() // 开启stream
    ssc.awaitTermination()
  }
}
