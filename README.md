# about-hadoop
hadoop相关

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
  docker run -itd --name hadoop zhaocc1106/hadoop:device1 bash
  docker run -itd --name hadoop zhaocc1106/hadoop:device2 bash
  docker run -itd --name hadoop zhaocc1106/hadoop:device3 bash

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
  cd /root/env
  ./apache-zookeeper-3.5.9-bin/bin/zkServer.sh start

6. 开启hadoop-ha
  进入任意实例hadoop环境执行：
  /root/env/hadoop-3.3.1
  ./sbin/start-all.sh

7. 通过jps确认服务是否正常
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
