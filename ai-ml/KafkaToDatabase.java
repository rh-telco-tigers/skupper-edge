// camel-k: language=java dependency=camel:sql
// camel-k: language=java dependency=camel:jdbc
// camel-k: dependency=mvn:org.apache.commons:commons-dbcp2:2.7.0.redhat-00001
// camel-k: dependency=mvn:mysql:mysql-connector-java:8.0.28.redhat-00001

import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.dbcp2.BasicDataSource;
import javax.sql.DataSource;

public class KafkaToDatabase extends RouteBuilder {
  @Override
  public void configure() throws Exception {
    registerDatasource();
	log.info("About to start route: Kafka Server -> DB ");
	from("kafka:na-east-com?brokers=my-cluster-kafka-brokers:9092"
             + "&seekTo=beginning")
             .routeId("FromKafka")
             .log("${body}")
             //.setBody(simple("select * from devicedetails;"))
             .setBody(simple("insert into devicedetails values(${body}, 'Yes', 'IND')"))
             .to("jdbc:mysqldb")
             .log("${body}");;
         
             from("timer:calldb?delay=30s").noAutoStartup()
             .log("${body}")
             .setBody(simple("select * from devicedetails;"))
             .to("jdbc:mysqldb")
             .log("${body}");
         
  }


            

  private void registerDatasource() throws Exception {
		BasicDataSource ds = new BasicDataSource();
		ds.setUsername("root");
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setPassword("mypassword");
		ds.setUrl("jdbc:mysql://mysqldb:3306/privatedcdb");
	 
		this.getContext().getRegistry().bind("mysqldb", ds);
	  }

}
