import org.apache.camel.builder.RouteBuilder;

public class KafkaProcessor extends RouteBuilder {
  @Override
  public void configure() throws Exception {
	log.info("About to start route: Kafka Server -> Log ");
	from("kafka:devices?brokers=my-cluster-kafka-bootstrap:9092"
             + "&seekTo=beginning")
             .routeId("FromKafka")
             .log("${body}")
             .to("kafka:na-east-com?brokers=my-cluster-kafka-bootstrap:9092");
  }
}
