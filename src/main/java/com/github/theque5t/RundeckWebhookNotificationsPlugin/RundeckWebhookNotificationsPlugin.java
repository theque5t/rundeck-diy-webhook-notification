package com.github.theque5t.RundeckWebhookNotificationsPlugin;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.notification.NotificationPlugin;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import java.util.*;
import java.io.*;

@Plugin(service="Notification",name="RundeckWebhookNotificationsPlugin")
@PluginDescription(title="Rundeck Webhook Notifications Plugin", description="A plugin for sending Rundeck job status notifications via Webhooks.")
public class RundeckWebhookNotificationsPlugin implements NotificationPlugin{

    @PluginProperty(name = "text",title = "text",description = "Type some text to be printed")
    private String test;

    public RundeckWebhookNotificationsPlugin(){

    }

    public boolean postNotification(String trigger, Map executionData, Map config) {	
    	
    	try(	FileWriter fw = new FileWriter("/tmp/RundeckWebhookNotificationsPlugin.txt", true); 
    			BufferedWriter bw = new BufferedWriter(fw);
    			PrintWriter out = new PrintWriter(bw))
			{
	        	out.printf("Trigger: %s \n",trigger);
	            out.printf("Execution data: %s \n",executionData);
	            out.printf("Config: %s \n",config);
	            out.printf("Text string: %s \n",test);
    		} catch (IOException e) {}
    	
        return true;
    }
}