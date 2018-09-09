package com.github.theque5t.DIYWebhookNotificationPlugin;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.notification.NotificationPlugin;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.descriptions.TextArea;
import com.dtolabs.rundeck.plugins.descriptions.RenderingOption;
import static com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants.DISPLAY_TYPE_KEY;
import java.util.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Plugin(service="Notification",name="DIYWebhookNotificationPlugin")
@PluginDescription(title="DIY Webhook Notification Plugin", description="The DIY(do it yourself) webhook notification plugin that lets you supply your own custom messages.")
public class DIYWebhookNotificationPlugin implements NotificationPlugin{
	
	@PluginProperty(
			name = "webhookUrl",
			title = "Webhook URL",
			description = "The webhook url. Example: https://hostname/services/TXXXXXXXX/XXXXXXXXX/XXXXXXXXXXXXXXXXXXXXXXXX",
			required = true)
	@RenderingOption(key = DISPLAY_TYPE_KEY, value = "PASSWORD")
	private String webhookUrl;
	
    @PluginProperty(
    		name = "contentType",
    		title = "Content Type",
    		description = "The content type header. Example: application/json",
    		required = true)
    private String contentType;
    
    @PluginProperty(
    		name = "messageBody",
    		title = "Message Body",
    		description = "The message body. Example: {\"text\":\"Hello world!\"}",
    		required = true)
    @TextArea
    private String messageBody;

    public DIYWebhookNotificationPlugin(){

    }
    
	private String sendMessage(String endpoint, String contentTypeHeader, String content) throws IOException {
		URL url = new URL(endpoint);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-type", contentTypeHeader);
		connection.setDoOutput(true);
		DataOutputStream body = new DataOutputStream(connection.getOutputStream());
		body.write(content.getBytes("UTF-8"));
		body.flush();
		body.close();
		int responseCode = connection.getResponseCode();
		connection.disconnect();
		String result = "The response code is: "+responseCode;
		return result;
	}
	    
    public boolean postNotification(String trigger, Map executionData, Map config){
    	try(FileWriter fw = new FileWriter("/tmp/DIYWebhookNotificationPlugin.txt", true); 
    		BufferedWriter bw = new BufferedWriter(fw);
    		PrintWriter out = new PrintWriter(bw))
		{
			out.printf("Trigger: %s \n",trigger);
			out.printf("Execution data: %s \n",executionData);
			out.printf("Config: %s \n",config);
			out.printf("Webhook URL string: %s \n",webhookUrl);
			out.printf("Content Type string: %s \n",contentType);
			out.printf("Message Body string: %s \n",messageBody);
			// TODO: analyze the messageBody and look for references to $executionData in message body. Foreach reference, store in a variable that is named after the execution data they're trying to get. Then format a new message with the execution data replacements. So like $executionData.name would become executionDataName with the value of executionData.name. And then replaced in that position in the message body using formatting.
			
			out.printf("Execution data type is : %s \n", executionData.getClass().getName());
			out.printf("Config data type is : %s \n", config.getClass().getName());
			
			out.printf("Execution data job is: %s \n", executionData.get("job"));
			Map job = (Map) executionData.get("job");
			out.printf("Execution data job name is %s \n", job.get("name"));
			out.printf("Config $something. message body is: %s \n", config.get("messageBody"));
			//String result = sendMessage(webhookUrl,contentType,messageBody);
			//out.println(result);
		} 
		catch (IOException e) 
		{
			System.out.printf("The exception: %s", e);
		}
        return true;
    }
}