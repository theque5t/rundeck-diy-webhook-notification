package com.github.theque5t.DIYWebhookNotificationPlugin;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.notification.NotificationPlugin;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.descriptions.TextArea;
import com.dtolabs.rundeck.plugins.descriptions.RenderingOption;
import static com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants.DISPLAY_TYPE_KEY;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    		description = "The message body. Example: {\"text\":\"Hello world!\"}  \n"
    					+ "You can add execution data to your message following this syntax: `$<map>.<key>$`  \n"
    					+ "Example: `{\"text\":\"Job $job.name$ has $execution.status$\"}`  \n"
    					+ "Valid map values are:\n"
    					+ " - `execution`\n"
    					+ " - `job`\n"
    					+ " - `nodeStatus`\n"
    					+ " - `globalContext`\n"
    					+ " - `jobContext`\n"
    					+ " - `jobOption`\n"
    					+ "  \n"
    					+ "Valid key values are:\n"
    					+ " - execution, job, and nodeStatus keys: see [https://rundeck.org/docs/developer/notification-plugin.html](https://rundeck.org/docs/developer/notification-plugin.html)\n"
    					+ " - globalContext, jobContext, and jobOption keys: see [https://rundeck.org/docs/manual/creating-job-workflows.html#context-variables](https://rundeck.org/docs/manual/creating-job-workflows.html#context-variables)\n",
			required = true)
    @TextArea
    private String messageBody;

    public DIYWebhookNotificationPlugin(){

    }

	private static String formatMessage(String theMessage, Map data) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		Pattern pattern = Pattern.compile("(\\$)(.*?)(\\$)");
        Matcher matcher = pattern.matcher(theMessage);
        StringBuffer buffer = new StringBuffer(theMessage.length());
        while(matcher.find())
        {
        	String mapName = matcher.group(2).substring(0, matcher.group(2).indexOf("."));
        	String keyName = matcher.group(2).substring(matcher.group(2).indexOf(".")+1, matcher.group(2).length());
        	        	
        	Map theMap = null;
        	boolean processMap = true;
        	if(mapName.equals("execution"))
        	{
        		theMap = (Map) data;
        	} 
        	else if(mapName.equals("job")) 
        	{
        		theMap = (Map) data.get("job");
        	} 
        	else if(mapName.equals("nodeStatus")) 
        	{
        		theMap = (Map) data.get("nodestatus");        		
        	} 
        	else if(mapName.equals("globalContext")) 
        	{
        		Map theContext = (Map) data.get("context");
        		theMap = (Map) theContext.get("globals");
        	} 
        	else if (mapName.equals("jobContext")) 
        	{
        		Map theContext = (Map) data.get("context");
        		theMap = (Map) theContext.get("job");        		
        	} 
        	else if (mapName.equals("jobOption")) 
        	{
        		Map theContext = (Map) data.get("context");
        		theMap = (Map) theContext.get("option");
        	} 
        	else 
        	{
        		System.out.println(mapName+" is not a valid map");
        	}
        	
        	
        	String theValue = null;
        	if (processMap) 
        	{
        		if (theMap.containsKey(keyName)) 
        		{
            		theValue = (String) theMap.get(keyName);        			
        		} 
        		else 
        		{
        			System.out.println(keyName+" is not a valid key for map "+mapName);
        		}
        	}
        	
        	matcher.appendReplacement(buffer, theValue);        	
        }
        matcher.appendTail(buffer);
        String theNewMessage = buffer.toString();
        return theNewMessage;
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
			
			out.printf("Execution data type is : %s \n", executionData.getClass().getName());
			out.printf("Config data type is : %s \n", config.getClass().getName());
			
			String formattedMessage = formatMessage(messageBody,executionData);
			//String result = sendMessage(webhookUrl,contentType,messageBody);
			//out.println(result);
		} 
		catch (IOException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) 
		{
			System.out.printf("The exception: %s", e);
		}
        return true;
    }
}