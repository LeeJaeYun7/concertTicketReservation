package concert.reservation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import concert.application.reservation.application.event.PaymentConfirmedEvent;
import concert.domain.concert.domain.Concert;
import concert.domain.concerthall.domain.ConcertHall;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.member.domain.Member;
import concert.domain.reservation.application.ReservationService;
import concert.domain.reservation.domain.Reservation;
import concert.domain.reservation.domain.ReservationRepository;
import concert.domain.seat.domain.Seat;
import concert.domain.seatgrade.domain.SeatGrade;
import concert.domain.seatinfo.domain.SeatInfo;
import concert.factory.TestDataFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@Slf4j
@Disabled
public class KafkaPaymentConfirmedAndReservationCreationTest {

  @Container
  static final MySQLContainer mysqlContainer = new MySQLContainer<>("mysql:8.0")
          .withDatabaseName("concertticket")
          .withUsername("root")
          .withPassword("1234");

  @Container
  static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"))
          .waitingFor(Wait.forListeningPort());

  static {
    mysqlContainer.start();
    kafkaContainer.start();
  }

  @Autowired
  ReservationService sut;
  @Autowired
  private TestDataFactory testDataFactory;

  @Autowired
  private ReservationRepository reservationRepository;
  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;
  @Autowired
  private ObjectMapper objectMapper;

  private String memberUuid;

  private Member member;
  private Concert concert;
  private ConcertHall concertHall;
  private ConcertSchedule concertSchedule;
  private Seat seat;
  private SeatGrade seatGrade;
  private SeatInfo seatInfo;

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    String kafkaBootstrapServer = "localhost:" + kafkaContainer.getMappedPort(9093);
    registry.add("spring.kafka.bootstrap-servers", () -> kafkaBootstrapServer);
    registry.add("spring.kafka.consumer.group-id", () -> "test-group");
    registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
    registry.add("spring.kafka.consumer.key-deserializer", () -> StringDeserializer.class.getName());
    registry.add("spring.kafka.consumer.value-deserializer", () -> StringDeserializer.class.getName());
  }

  @BeforeEach
  void setUp() {
    member = testDataFactory.createMember();
    memberUuid = member.getUuid();

    concertHall = testDataFactory.createConcertHall();
    concert = testDataFactory.createConcert(concertHall);
    concertSchedule = testDataFactory.createConcertSchedule(concert);

    seat = testDataFactory.createSeat(concertHall);
    seatGrade = testDataFactory.createSeatGrade(concert);
    seatInfo = testDataFactory.createSeatInfo(seat, concertSchedule, seatGrade);
  }

  @Test
  @DisplayName("PaymentConfirmedEvent를_전달받은_후_예약을_생성한다")
  void PaymentConfirmedEvent를_전달받은_후_예약을_생성한다() throws JsonProcessingException {
    PaymentConfirmedEvent event = new PaymentConfirmedEvent(concert.getId(), concertSchedule.getId(), memberUuid, seatInfo.getSeat().getNumber(), seatInfo.getSeatGrade().getPrice());

    String eventJson = objectMapper.writeValueAsString(event);
    kafkaTemplate.send("payment-confirmed-event", eventJson);
    String consumedMessage = pollMessageFromKafka("payment-confirmed-event");

    PaymentConfirmedEvent consumedEvent = objectMapper.readValue(consumedMessage, PaymentConfirmedEvent.class);

    sut.handlePaymentConfirmed(consumedEvent);

    Reservation reservation = reservationRepository.findAll().get(0);
    assertNotNull(reservation);
    assertEquals(memberUuid, reservation.getUuid());
    assertEquals(50000L, reservation.getPrice());
  }

  private String pollMessageFromKafka(String topic) {
    Map<String, Object> consumerProps = new HashMap<>();
    consumerProps.put("bootstrap.servers", kafkaContainer.getBootstrapServers());
    consumerProps.put("group.id", "test-group");
    consumerProps.put("key.deserializer", StringDeserializer.class);
    consumerProps.put("value.deserializer", StringDeserializer.class);
    consumerProps.put("auto.offset.reset", "earliest");

    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
    consumer.subscribe(java.util.Collections.singletonList(topic));

    var records = consumer.poll(Duration.ofSeconds(5));

    if (!records.isEmpty()) {
      return records.iterator().next().value();
    }

    return "";
  }
}
