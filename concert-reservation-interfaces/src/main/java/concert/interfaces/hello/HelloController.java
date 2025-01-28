package concert.interfaces.hello;

import concert.interfaces.shared.dto.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public CommonResponse hello() {
        return CommonResponse.success("Hello " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
    }
}