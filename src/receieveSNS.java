

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static tweetBasic.AWSResourceSetup.*;


import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest;
import com.amazonaws.services.sns.model.ConfirmSubscriptionResult;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet implementation class receieveSNS
 */
public class receieveSNS extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public receieveSNS() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException, SecurityException {
		String messagetype = req.getHeader("x-amz-sns-message-type");
		if (messagetype == null)
			return;

		Scanner scan = new Scanner(req.getInputStream());
		StringBuilder builder = new StringBuilder();
		while (scan.hasNextLine()) {
			builder.append(scan.nextLine());
		}
		scan.close();
		System.out.println("Received SNS: " + builder.toString());
		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> userData = mapper.readValue(builder.toString(), Map.class);
		String Token="";
		if(userData.get("Token")!=null){
			Token=(String) userData.get("Token");
		}
		
		ConfirmSubscriptionRequest confirmSubscriptionRequest = new ConfirmSubscriptionRequest()
			.withTopicArn(SNS_TOPIC_ARN)
		.withToken(Token);
        ConfirmSubscriptionResult resutlt = SNS.confirmSubscription(confirmSubscriptionRequest);
        System.out.println("subscribed to " + resutlt.getSubscriptionArn());
		
		
//		SNSMessage msg = readMessageFromJson(builder.toString());
//
//		/*if (msg.getSignatureVersion().equals("1")) {
//			if (isMessageSignatureValid(msg))
//				System.out.println("Signature verification succeeded");
//			else {
//				System.out.println("Signature verification failed");
//				throw new SecurityException("Signature verification failed.");
//			}
//		}
//		else {
//			System.out.println("Unexpected signature version. Unable to verify signature.");
//			throw new SecurityException("Unexpected signature version. Unable to verify signature.");
//		}*/
//		
//		if (messagetype.equals("Notification")) {
//			//
//		} else if (messagetype.equals("SubscriptionConfirmation")) {
//			Scanner sc = new Scanner(
//					new URL(msg.getSubscribeURL()).openStream());
//			StringBuilder sb = new StringBuilder();
//			while (sc.hasNextLine()) {
//				sb.append(sc.nextLine());
//			}
//			sc.close();
//			SNSHelper.INSTANCE.confirmTopicSubmission(msg);
//		}
//		System.out.println("Done processing message: " + msg.getMessageId());
	}
		 
	

	private SNSMessage readMessageFromJson(String string) {
		ObjectMapper mapper = new ObjectMapper();
		SNSMessage message = null;
		try {
			message = mapper.readValue(string, SNSMessage.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return message;
	}

}
