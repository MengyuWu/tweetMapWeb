import static tweetBasic.AWSResourceSetup.SNS;
import static tweetBasic.AWSResourceSetup.SNS_TOPIC_ARN;

import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest;
import com.amazonaws.services.sns.model.ConfirmSubscriptionResult;


public class mySNSHelper {
	
	public static void confirmTopicSubmission(String token){
		ConfirmSubscriptionRequest confirmSubscriptionRequest = new ConfirmSubscriptionRequest()
		.withTopicArn(SNS_TOPIC_ARN)
	.withToken(token);
    ConfirmSubscriptionResult resutlt = SNS.confirmSubscription(confirmSubscriptionRequest);
    System.out.println("subscribed to " + resutlt.getSubscriptionArn());
	
	}
}
