package concert.commons.utils;

import concert.commons.common.CustomException;
import concert.commons.common.ErrorCode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import static java.lang.Math.abs;

public class SnowFlakeGenerator {

    public static String createSnowFlake() {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            String randomUUID = String.valueOf(abs(UUID.randomUUID().getMostSignificantBits()));

            return timestamp + '-' + hostAddress + '-' + randomUUID;
        } catch (UnknownHostException e) {
            throw new CustomException(ErrorCode.HOST_NOT_FOUND);
        }
    }
}