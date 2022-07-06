# Skupper-Edge

<img width="1136" alt="Screen Shot 2022-07-06 at 3 41 13 PM" src="https://user-images.githubusercontent.com/7470319/177629957-46721c96-fcdc-49a9-828f-765c9c4f3f79.png">

### Follow below steps : 

### 1 - In your Edge location, Create Kind cluster with Kafka cluster using bridge

https://github.com/svalluru/strimzi-kafka-bridge-kind-ingress
Go to kafka namespace and init Skupper as mentioned here https://github.com/skupperproject/skupper-example-hello-world#step-4-install-skupper-in-your-namespaces.

Expose HTTP Bridge and Kafka Broker in Skupper Network : 
 ```
 skupper expose service my-bridge-bridge-service --address kafka --port 8080 -n kafka
 
 skupper expose statefulset/my-cluster-kafka --headless --port 9092 -n kafka
```

### 2 - Setup OCP cluster on public cloud.

### 3 - In a private Data Center, Create DB using docker and expose it using Skupper using kubeconfig of OCP on Public cloud from step (2)
```
docker run -p 3306:3306 --name=sri-mysql --env="MYSQL_ROOT_PASSWORD=mypassword" mysql 
docker exec -i -t <containerid> bin/bash
mysql -u root -p
create privatedcdb database
create devicedetails table

export KUBECONFIG = OCP kubeconfig
skupper expose mysql service	:  skupper gateway expose mysqldb localhost 3306 --type docker
```

### 4 - Create Skupper network by connecting Kind Cluster, OCP Cluster on Public cloud and Database. Steps to link are here https://github.com/skupperproject/skupper-example-hello-world#step-6-link-your-namespaces.

 - Run "oc get svc" and see if you can see the exposed services of kafka, database in the Public cloud cluster.
 ```
svalluru@svalluru-mac ~ % oc get svc
NAME                       TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)               AGE
kafka                      ClusterIP   172.30.86.121    <none>        8080/TCP              23h
my-cluster-kafka-brokers   ClusterIP   None             <none>        9092/TCP              23h
mysqldb                    ClusterIP   172.30.42.40     <none>        3306/TCP              15d
skupper                    ClusterIP   172.30.235.121   <none>        8080/TCP,8081/TCP     21d
skupper-router             ClusterIP   172.30.231.3     <none>        55671/TCP,45671/TCP   21d
skupper-router-local       ClusterIP   172.30.70.227    <none>        5671/TCP              21d
```
### 5 - Create Quarkus Camel project (tower-app) and generate a native build images as mentioned here https://github.com/svalluru/camel-quarkus-native-kafka-bridge-post
	
	- Run HTTP bridge for Kafka cluster locally on http://localhost/topics/devices or you can change in code. This Kafka bridge will be at your Edge location.
	- Run multiple native builds with different values for the 'tower' param :  ./tower-app-1.0.0-SNAPSHOT-runner -Dtower=tower6 &
	
	This will populate messages as if edge device is sending some messages to data store (here kafka topic).

### 6 - Inside Kind Cluster, run CamelK [ai-ml/KafkaProcessor.java](ai-ml/KafkaProcessor.java) process that will read the data from "devices" kafka topic, do some processing as if it's an AI ML code and then send the processed data to "na-east-com" topic used by public cloud cluster.
	 
	 Install camelk, kamel on your machine and run the KafkaProcessor in Kind cluster created above.
   ```
   - kubectl -n kafka create secret docker-registry external-registry-secret --docker-server=quay.io --docker-username abc --docker-password "pwd"
   
   - ./kamel install --olm=false -n kafka --registry quay.io --organization svalluru1 --registry-secret external-registry-secret --wait
   
   - ./kamel run KafkaProcessor.java --config secret:external-registry-secret -n kafka
```

### 7 - Inside OCP cluster project on public cloud, run [ai-ml/KafkaToDatabase.java](ai-ml/KafkaToDatabase.java) that will read the data from "na-east-com" kafka topic, do some processing as if it's an AI ML code and then send the processed data to database in private datacenter.

	- Install Camel K operator and create Integration Platform in OCP project.
```
oc -n kafka create secret docker-registry external-registry-secret --docker-server=quay.io --docker-username abc --docker-password "pwd"

./kamel install --olm=false -n kafka --registry quay.io --organization svalluru1 --registry-secret external-registry-secret --wait

./kamel run KafkaToDatabase.java --config secret:external-registry-secret -n kafka

```

### Verify the flow of demo : 
```
1) Open 1 CLI for reading messages from devices topic

kubectl -n kafka run kafka-consumer33 -ti --image=quay.io/strimzi/kafka:0.29.0-kafka-3.2.0 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic devices --from-beginning

2) Open 1 CLI for reading messages from na-east-com topic 

kubectl -n kafka run kafka-consumer44 -ti --image=quay.io/strimzi/kafka:0.29.0-kafka-3.2.0 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic na-east-com --from-beginning

3) Open 1 CLI for running tower-app-1.0.0-SNAPSHOT-runner

./tower-app-1.0.0-SNAPSHOT-runner -Dtower=tower7 &

4) Open 1 CLI to show DB table output

docker exec -i -t ed6efdc945ea bin/bash
mysql -u root -p
use privatedcdb
privatedcdb
select * from devicedetails;

5) Open Browser to show Pod kafka-to-database Logs.
```
