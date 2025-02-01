package concert.interfaces.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import concert.application.order.business.OrderApplicationService;
import concert.domain.order.vo.OrderVO;
import concert.interfaces.order.request.OrderRequest;
import concert.interfaces.order.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
public class OrderController {

  private final OrderApplicationService orderApplicationService;

  @PostMapping("/api/v1/order")
  public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) throws ExecutionException, InterruptedException, JsonProcessingException {
    String uuid = orderRequest.getUuid();
    long concertScheduleId = orderRequest.getConcertScheduleId();
    List<Long> concertScheduleSeatIds = orderRequest.getConcertScheduleSeatIds();

    OrderVO orderVO = orderApplicationService.createOrder(uuid, concertScheduleId, concertScheduleSeatIds).get();
    OrderResponse orderResponse = OrderResponse.of(orderVO.getName(), orderVO.getConcertName(), orderVO.getDateTime(), orderVO.getTotalPrice());

    return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
  }
}
