package charge.presentation;

import charge.application.ChargeFacade;
import charge.application.dto.request.ChargeRequest;
import charge.application.dto.response.ChargeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChargeController {

    private final ChargeFacade chargeFacade;

    public ChargeController(ChargeFacade chargeFacade){
        this.chargeFacade = chargeFacade;
    }

    @PostMapping("/api/v1/charge")
    public ResponseEntity<ChargeResponse> chargeBalance(@RequestBody ChargeRequest chargeRequest) {
        String uuid = chargeRequest.getUuid();
        long amount = chargeRequest.getAmount();

        long updatedBalance = chargeFacade.chargeBalance(uuid, amount);
        ChargeResponse chargeResponse = ChargeResponse.of(updatedBalance);

        return ResponseEntity.status(HttpStatus.OK).body(chargeResponse);
    }
}
