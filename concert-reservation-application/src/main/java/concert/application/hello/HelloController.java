package concert.application.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class HelloController {


  @GetMapping("/hello")
  public String hello() {
    return "Hello " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
  }

}
