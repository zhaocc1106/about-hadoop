# about-hadoop
hadoop相关

## 构建高可用hadoop集群(hadoop-ha)
使用三个docker实例模拟三台机器，主机名分别为hadoop1.com、hadoop2.com、hadoop3.com。<br>
hadoop1.com部署的服务包括：“NameNode”、“DataNode”、“NodeManager”、“JournalNode”、“DFSZKFailoverController”、“Zookeeper”。<br>
hadoop2.com部署的服务包括：“NameNode”、“DataNode”、“NodeManager”、“JournalNode”、“DFSZKFailoverController”、“Zookeeper”、“ResourceManager”。<br>
hadoop3.com部署的服务包括：“DataNode”、“NodeManager”、“JournalNode”、“Zookeeper”、“ResourceManager”。
### 构建步骤：
```
拉取docker镜像，镜像中已经包含了jdk，hadoop，zookeeper环境以及ha相关的配置：
docker pull zhaocc1106/hadoop:ha
```
