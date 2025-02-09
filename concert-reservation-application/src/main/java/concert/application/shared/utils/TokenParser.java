package concert.application.shared.utils;

import org.springframework.stereotype.Component;

@Component
public class TokenParser {
    public String getUuidFromToken(String token){
        return token.split(":")[1];
    }
}
