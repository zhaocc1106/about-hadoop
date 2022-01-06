# big-data
大数据相关

## [构建高可用hadoop集群(hadoop-ha)](https://www.cnblogs.com/ling-yu-amen/articles/11460590.html)
使用三个docker实例模拟三台机器，主机名分别为hadoop1.com、hadoop2.com、hadoop3.com。<br>
* hadoop1.com部署的服务包括：<br>
“NameNode”、“DataNode”、“NodeManager”、“JournalNode”、“DFSZKFailoverController”、“Zookeeper”。<br>
* hadoop2.com部署的服务包括：<br>
“NameNode”、“DataNode”、“NodeManager”、“JournalNode”、“DFSZKFailoverController”、“Zookeeper”、“ResourceManager”。<br>
* hadoop3.com部署的服务包括：<br>
“DataNode”、“NodeManager”、“JournalNode”、“Zookeeper”、“ResourceManager”。
### 构建步骤：
```
1.拉取docker镜像，镜像中已经包含了jdk，hadoop，zookeeper环境以及ha相关的配置：
  docker pull zhaocc1106/hadoop:ha

2.开启三个docker实例模拟三台机器
  docker run -itd --name hadoop1 zhaocc1106/hadoop:ha bash
  docker run -itd --name hadoop2 zhaocc1106/hadoop:ha bash
  docker run -itd --name hadoop3 zhaocc1106/hadoop:ha bash

3.更改每个实例的/etc/hosts，以及修改主机的/etc/hosts方便web访问
  添加每台实例新指定的域名：
   172.17.0.2  hadoop1.com                                                                                                                                           
   172.17.0.3  hadoop2.com
   172.17.0.4  hadoop3.com
  互相添加其他实例的自动生成的域名：
   172.17.0.2  dd6b9aae142e
   172.17.0.3  1594073577c8
   172.17.0.4  04aef1ab8377

4.开启每台实例的ssh服务，每个实例执行
  service ssh start

5.开启zookeeper集群，每个实例执行
  rm -rf /opt/data/zookeeper/data/
  mkdir -p /opt/data/zookeeper/data/zData/
  echo 1 > /opt/data/zookeeper/data/zData/myid // hadoop1.com, haoop2.com, hadoop3.com实例分别写1, 2, 3到myid中
  cd /root/env
  ./apache-zookeeper-3.5.9-bin/bin/zkServer.sh start

6. 格式化
  每个实例执行如下命令启动journalnode:
  hdfs --daemon start journalnode
  
  hadoop1.com中执行如下命令格式化namenode并启动：
  hdfs namenode -format
  hdfs --daemon start namenode
  
  hadoop2.com中执行如下命令初始化namenode为standby并启动：
  hdfs namenode -bootstrapStandby
  
  hadoop1.com中执行如下命令格式化hadoop在zookeeper中的node：
  hdfs zkfc -formatZK
  
7. 开启hadoop-ha其它服务
  进入任意实例hadoop环境执行：
  /root/env/hadoop-3.3.1
  ./sbin/start-all.sh

8. 通过jps确认服务是否正常
  hadoop1:
    2163 NodeManager
    885 NameNode
    1365 JournalNode
    2534 Jps
    1609 DFSZKFailoverController
    269 QuorumPeerMain
    1054 DataNode
  hadoop2:
    995 ResourceManager
    659 JournalNode
    1363 Jps
    409 NameNode
    1114 NodeManager
    251 QuorumPeerMain
    796 DFSZKFailoverController
    524 DataNode
  hadoop3:
    528 JournalNode
    819 NodeManager
    676 ResourceManager
    1188 Jps
    269 QuorumPeerMain
    397 DataNode
```

## 基于hadoop集群构建spark集群
### 构建步骤
```
1.下载与hadoop兼容的版本：wget https://www.apache.org/dyn/closer.lua/spark/spark-3.1.2/spark-3.1.2-bin-hadoop3.2.tgz

2.解压到hadoop1容器/root/env目录并进入spark目录

3.添加spark环境变量到.bashrc
  export SPARK_HOME=/root/env/spark-3.1.2-bin-hadoop3.2
  export PATH=$SPARK_HOME/bin:$PATH

4.添加spark-env的环境变量
  cp conf/spark-env.sh.template conf/spark-env.sh
    export JAVA_HOME=/root/env/jdk1.8.0_301
    export JRE_HOME=$JAVA_HOME/jre
    export SPARK_MASTER_IP=172.17.0.2
    export SPARK_WORKER_MEMORY=8g
    export SPARK_WORKER_CORES=4
    export SPARK_EXECUTOR_MEMORY=4g
    export HADOOP_HOME=/root/env/hadoop-3.3.1
    export HADOOP_CONF_DIR=/root/env/hadoop-3.3.1/etc/hadoop
    export LD_LIBRARY_PATH=$HADOOP_HOME/lib/native:$JRE_HOME/lib/native/:$JRE_HOME/lib/amd64/server/:$LD_LIBRARY_PATH

5.设置workers域名
  cp conf/workers.template conf/workers
    hadoop1.com
    hadoop2.com
    hadoop3.com

6.修改spark集群管理web port
  vim sbin/start-master.sh
    SPARK_MASTER_WEBUI_PORT=8081

7.启动spark集群
  ./sbin/start-all.sh
  访问hadoop1.com:8081集群管理web确认spark集群启动成功，通过spark-shell访问hdfs中的文件确认和hadoop关联成功。
```
