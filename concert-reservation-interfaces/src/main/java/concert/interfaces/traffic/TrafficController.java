package concert.interfaces.traffic;

import concert.infrastructure.monitoring.TrafficMonitoringFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TrafficController {

    private final TrafficMonitoringFilter trafficMonitoringFilter;

    @GetMapping("/api/v1/traffic")
    public void getTraffic(@RequestParam(value="apiName") String apiName){
        long totalTraffic = trafficMonitoringFilter.getTrafficForLastMinute(apiName);
        log.info("api calls?" + totalTraffic);
    }
}
