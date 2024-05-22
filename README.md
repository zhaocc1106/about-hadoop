# big-data
大数据相关

## [构建高可用hadoop集群(hadoop-ha)](https://www.cnblogs.com/ling-yu-amen/articles/11460590.html)
![image](https://github.com/zhaocc1106/big-data/assets/26559935/958d74ec-7ddc-4e66-b0f0-1158e4e41fde)
* HDFS HA 架构中有两台 NameNode 节点，一台是处于活动状态（Active）为客户端提供服务，另外一台处于热备份状态（Standby）。元数据文件有两个文件：fsimage 和 edits，备份元数据就是备份这两个文件。
* JournalNode 用来实时从 Active NameNode 上拷贝 edits 文件，JournalNode 有三台也是为了实现高可用。Standby NameNode 不对外提供元数据的访问，它从 Active NameNode 上拷贝 fsimage 文件，从 JournalNode 上拷贝 edits 文件，然后负责合并 fsimage 和 edits 文件，相当于 SecondaryNameNode 的作用。最终目的是保证 Standby NameNode 上的元数据信息和 Active NameNode 上的元数据信息一致，以实现热备份。
* Zookeeper 来保证在 Active NameNode 失效时及时将 Standby NameNode 修改为 Active 状态。
* ZKFC（失效检测控制）是 Hadoop 里的一个 Zookeeper 客户端，在每一个 NameNode 节点上都启动一个 ZKFC 进程，来监控 NameNode 的状态，并把 NameNode 的状态信息汇报给 Zookeeper 集群，其实就是在 Zookeeper 上创建了一个 Znode 节点，节点里保存了 NameNode 状态信息。当 NameNode 失效后，ZKFC 检测到报告给 Zookeeper，Zookeeper把对应的 Znode 删除掉，Standby ZKFC 发现没有 Active 状态的 NameNode 时，就会用 shell 命令将自己监控的 NameNode 改为 Active 状态，并修改 Znode 上的数据。
* Znode 是个临时的节点，临时节点特征是客户端的连接断了后就会把 znode 删除，所以当 ZKFC 失效时，也会导致切换 NameNode。
* DataNode 会将心跳信息和 Block 汇报信息同时发给两台 NameNode， DataNode 只接受 Active NameNode 发来的文件读写操作指令。

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
  
  hadoop1.com中执行如下命令格式化zookeeper，即创建hadoop在zookeeper中的znode：
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
