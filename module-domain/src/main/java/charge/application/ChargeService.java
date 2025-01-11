package charge.application;

import charge.domain.Charge;
import charge.domain.ChargeRepository;
import org.springframework.stereotype.Service;

@Service
public class ChargeService {

    private final ChargeRepository chargeRepository;

    public ChargeService(ChargeRepository chargeRepository){
        this.chargeRepository = chargeRepository;
    }

    public void createCharge(String uuid, long amount){
        Charge charge = Charge.of(uuid, amount);
        chargeRepository.save(charge);
    }

    public void getChargeByUuid(String uuid){
        chargeRepository.findChargeByUuid(uuid);
    }
}
