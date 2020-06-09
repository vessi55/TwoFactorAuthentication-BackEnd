package twofactorauth.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import org.springframework.stereotype.Service;

@Service
public class SMSService {

    private static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    private static final String AUTO_PHONE_NUMBER = System.getenv("TWILIO_PHONE_NUMBER");

    private static final String TWO_FACTOR_AUTHENTICATION_CODE = "Your Two Factor Authentication Code Is : ";

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendLoginVerificationSMS(String phoneNumber, String verificationCode) {

        Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(AUTO_PHONE_NUMBER),
                TWO_FACTOR_AUTHENTICATION_CODE + verificationCode).create();
    }
}
