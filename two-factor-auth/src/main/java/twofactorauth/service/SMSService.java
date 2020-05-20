package twofactorauth.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import org.springframework.stereotype.Service;

@Service
public class SMSService {

    private static final String ACCOUNT_SID = "ACc291ba97aabcdb032770360da0ab93a5";
    private static final String AUTH_TOKEN = "98248b0382c39fdcaf0559b006ebc699";
    private static final String AUTO_PHONE_NUMBER = "+12513069400";
    private static final String TWO_FACTOR_AUTHENTICATION_CODE = "Your Two Factor Authentication Code Is : ";

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendLoginVerificationSMS(String phoneNumber, String verificationCode) {

        Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(AUTO_PHONE_NUMBER),
                TWO_FACTOR_AUTHENTICATION_CODE + verificationCode).create();
    }
}
