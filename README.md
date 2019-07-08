# rundeck-diy-webhook-notification

[Rundeck](https://www.rundeck.com/) [notification plugin](https://rundeck.org/docs/developer/notification-plugin.html) for the DIYer, because it allows you to supply your own custom messages to be sent to a webhook.

## Installation

1. Download the [plugin file](https://github.com/theque5t/rundeck-diy-webhook-notification/raw/master/build/libs/rundeck-diy-webhook-notification-1.0.0.jar)

```sh
wget "https://github.com/theque5t/rundeck-diy-webhook-notification/raw/master/build/libs/rundeck-diy-webhook-notification-1.0.0.jar"
```

2. Put the plugin file, rundeck-diy-webhook-notification-1.0.0.jar, into the [plugin directory](https://rundeck.org/docs/developer/plugin-development.html)

```sh
mv rundeck-diy-webhook-notification-1.0.0.jar /var/lib/rundeck/libext
```

3. The plugin is now ready for use

## Usage
### Provider Name: `DIYWebhookNotificationPlugin`
### Plugin Properties:
 - __Webhook URL:__ The webhook url. Example: `https://hostname/services/TXXXXXXXX/XXXXXXXXX/XXXXXXXXXXXXXXXXXXXXXXXX`  
 configure project: `project.plugin.Notification.DIYWebhookNotificationPlugin.webhookUrl=value`  
 configure framework: `framework.plugin.Notification.DIYWebhookNotificationPlugin.webhookUrl=value`  
  
 - __Content Type:__ The content type header. Example: `application/json`  
 configure project: `project.plugin.Notification.DIYWebhookNotificationPlugin.contentType=value`  
 configure framework: `framework.plugin.Notification.DIYWebhookNotificationPlugin.contentType=value`  
  
 - __Message Body:__ The message body. Example: `{"text":"Hello world!"}`  
 configure project: `project.plugin.Notification.DIYWebhookNotificationPlugin.messageBody=value`  
 configure framework: `framework.plugin.Notification.DIYWebhookNotificationPlugin.messageBody=value`  
 
 >![picture](docs/images/configuration.png)
 ___
### Building the Message
The execution order and summary of what occurs when building the final message body is as follows: 
 1. Any embedded property references will be replaced with the runtime value.
 2. Any execution data references will be replaced with the runtime value.
 3. Any template markup will be rendered.
___
### Embedded Property References
You can add [embedded property references](https://rundeck.org/docs/developer/notification-plugin.html) to your message following this syntax: `${group.key}`

Example for "On Start": 
```
{"text":"Job ${job.name}(#${job.execid}): Started"}
```
###### `${job.name}` and `${job.execid}` from the above example are embedded property references
___
### Execution Data References
You can add [execution data references](https://rundeck.org/docs/developer/notification-plugin.html) to your message following this syntax: `$map.key$`

Examples:
```
{"text":"Job ${job.name}(#${job.execid}): $execution.status$"}
```
###### `$execution.status$` from the above example is an execution data reference
```
{"text":"Job ${job.name}(#$execution.id$): $execution.status$"}
```
###### `$execution.id$` and `$execution.status$` from the above example are execution data references
```
{"text":"Job ${job.name}(#${job.execid}) from Group $execution.context.job.group$: $execution.status$"}
```
###### `$execution.context.job.group$` and `$execution.status$` from the above example are execution data references

[Refer here](https://rundeck.org/docs/developer/notification-plugin.html#execution-data) and [here](https://rundeck.org/docs/manual/creating-job-workflows.html#context-variables) to see what data is available.
You can also supply a reference that equals an entire map. For example:
```
{"text":"Job: ${job.name}
Id: $execution.job.id$
Status: $execution.status$
Job Details: $execution.job$"}
```
###### `$execution.job$` from the above example points to the job map

___
#### Template Markup
You can add template markup following [the template language](https://shopify.github.io/liquid/) syntax.

Example:
```
{"text":"Job ${job.name}(#${job.execid}): {{ "$execution.status$" | capitalize }}"}
```
###### `{{ "$execution.status$" | capitalize }}` from the above example is template markup


Refer to the [documentation for designers](https://github.com/Shopify/liquid/wiki/Liquid-for-Designers) for further assistance with the template language.
___
### Slack Example 1

__Notification:__ `On Start`, `On Success`, `On Failure`

__Webhook URL:__ `https://hooks.slack.com/services/TXXXXXXXX/XXXXXXXXX/XXXXXXXXXXXXXXXXXXXXXXXX` 

__Content Type:__ `application/json` 

__Message Body:__
```
{
        "username": "Rundeck",
        "icon_url": "https://github.com/rundeck/rundeck/blob/49ae04af6e31d5b1bb1c8bba4bb284ff9739327b/rundeckapp/grails-app/assets/images/logos/rundeck-red-chevron.png?raw=true",
        "channel": "#test",
        "attachments": [
          {
            "fallback":"*$execution.job.name$* (<$execution.href$|#$execution.id$>) {% assign status = "$execution.status$" | capitalize | prepend: "*" | append: "*" %}{% if status == '*Running*' %}{{ status | append: " :warning:" }}{% elsif status == '*Succeeded*' %}{{ status | append: " :heavy_check_mark:" }}{% else %}{{ status | append: " :heavy_multiplication_x:" }}{% endif %}",
            "pretext":"*$execution.job.name$* (<$execution.href$|#$execution.id$>) {% assign status = "$execution.status$" | capitalize | prepend: "*" | append: "*" %}{% if status == '*Running*' %}{{ status | append: " :warning:" }}{% elsif status == '*Succeeded*' %}{{ status | append: " :heavy_check_mark:" }}{% else %}{{ status | append: " :heavy_multiplication_x:" }}{% endif %}",
            "color":"{% if status == '*Running*' %}warning{% elsif status == '*Succeeded*' %}good{% else %}danger{% endif %}",
            "fields":[
          {
            "title":"Job Name",
            "value":"<$execution.job.href$|$execution.job.name$>",
            "short":true
          },
          {
            "title":"Project",
            "value":"$job.project$",
            "short":true
          },
          {
            "title":"Execution ID",
            "value":"<$execution.href$|#$execution.id$>",
            "short":true
          },
          {
            "title":"Started By",
            "value":"{% assign user = "$execution.user$" %}{% if user == 'admin' %}<@UC08WJC3Y>{% else %}{{ user }}{% endif %}",
            "short":true
          }{% assign optionMap = "$execution.context.option$" %}{% if optionMap == '{}' %}{% else %},
          {
            "title":"Options",
            "value":"{% assign lengthMinusTwo = optionMap | size | minus: 2 %}{% assign options = optionMap | slice: 1, lengthMinusTwo %}{% assign options = options | split: ", " | reverse %}{% for option in options %} •  {{ option | replace_first: '=', ': `' }}`\n{% endfor %}",
            "short":true
          }
          {% endif %}
        ]
      }
    ]
}
```
__Linking to the Slack user:__ `"value":"{% assign user = "$execution.user$" %}{% if user == 'admin' %}<@XX00XXX0X>{% else %}{{ user }}{% endif %}"` from the above example is template markup that replaces the Rundeck user value with the correlating Slack member ID. The Slack member ID can be found on their Slack profile. This shows how you can link the job notifications to the Rundeck user's Slack account.  
  
![picture](docs/images/slack-example-1.png)

___
### Slack Example 2

This example makes use of Slack's new [Block Kit](https://api.slack.com/block-kit) UI framework

__Notification:__ `On Start`

__Webhook URL:__ `https://hooks.slack.com/services/TXXXXXXXX/XXXXXXXXX/XXXXXXXXXXXXXXXXXXXXXXXX` 

__Content Type:__ `application/json` 

__Message Body:__
```
{
  "username": "Rundeck",
  "icon_url": "https://github.com/rundeck/rundeck/blob/49ae04af6e31d5b1bb1c8bba4bb284ff9739327b/rundeckapp/grails-app/assets/images/logos/rundeck-red-chevron.png?raw=true",
  "channel": "#test",
  "text": "{% assign status = "$execution.status$" %}$execution.job.name$ (#$execution.id$) {% if status == 'running' %}{{ status | append: " :arrow_forward:" }}{% elsif status == 'succeeded' %}{{ status | append: " :white_check_mark:" }}{% else %}{{ status | append: " :no_entry:" }}{% endif %}",
  "blocks":
  [
    {
      "type": "divider"
    },
    {
      "type": "context",
      "elements":
      [
        {
          "type": "mrkdwn",
          "text": ":rundeck-project: $job.project$ :rundeck-group: {{ "$job.group$" | split: '/' | join: ' / ' }}"
        }
      ]
    },
    {
      "type": "section",
      "text":
      {
        "type": "mrkdwn",
        "text": "{% if status == 'running' %}{{ ":rundeck-job-running:" }}{% elsif status == 'succeeded' %}{{ ":rundeck-job-succeeded:" }}{% else %}{{ " :rundeck-job-failed:" }}{% endif %} <$execution.job.href$|$execution.job.name$> (<$execution.href$#output|#$execution.id$>)"
      }
    },
    {
      "type": "section",
      "fields":
      [
        {
          "type": "mrkdwn",
          "text": "*Options:*\n{% assign optionMap = '$execution.context.option$' %}{% if optionMap == '{}' %}{% else %}{% assign lengthMinusTwo = optionMap | size | minus: 2 %}{% assign options = optionMap | slice: 1, lengthMinusTwo %}{% assign options = options | split: ", " | reverse %}{% for option in options %} \u2022  {{ option | replace_first: '=', ': `' }}`\n{% endfor %}{% endif %}"
        },
        {
          "type": "mrkdwn",
          "text": "*Nodes:*\n{% assign nodes = "${job.filter}" %}{% assign nodes = nodes | split: "," %}{% for node in nodes %} \u2022  {{ node }}\n{% endfor %}"
        }
      ]
    },
    {
      "type": "context",
      "elements":
      [
        {
          "type": "mrkdwn",
          "text": "Started by: {% assign user = "$execution.user$" %}{% if user == 'admin' %}<@UC08WJC3Y>{% else %}{{ user }}{% endif %}"
        }
      ]
    },
    {
      "type": "divider"
    }
  ]
}
```

__Notification:__ `On Success`, `On Failure`

__Webhook URL:__ `https://hooks.slack.com/services/TXXXXXXXX/XXXXXXXXX/XXXXXXXXXXXXXXXXXXXXXXXX` 

__Content Type:__ `application/json` 

__Message Body:__
```
{
  "username": "Rundeck",
  "icon_url": "https://github.com/rundeck/rundeck/blob/49ae04af6e31d5b1bb1c8bba4bb284ff9739327b/rundeckapp/grails-app/assets/images/logos/rundeck-red-chevron.png?raw=true",
  "channel": "#test",
  "text": "{% assign status = "$execution.status$" %}$execution.job.name$ (#$execution.id$) {% if status == 'running' %}{{ status | append: " :arrow_forward:" }}{% elsif status == 'succeeded' %}{{ status | append: " :white_check_mark:" }}{% else %}{{ status | append: " :no_entry:" }}{% endif %}",
  "blocks":
  [
    {
      "type": "section",
      "text":
      {
        "type": "mrkdwn",
        "text": "{% if status == 'running' %}{{ ":rundeck-job-running:" }}{% elsif status == 'succeeded' %}{{ ":rundeck-job-succeeded:" }}{% else %}{{ " :rundeck-job-failed:" }}{% endif %} <$execution.job.href$|$execution.job.name$> (<$execution.href$#output|#$execution.id$>)"
      }
    }
  ]
}
```

__Slack Emojis:__ The example above utilizes [custom Slack emojis](https://get.slack.help/hc/en-us/articles/217626538-Customize-your-workspace#emoji) that were [added to the Slack workspace](https://get.slack.help/hc/en-us/articles/206870177). Because they're emoji's, additional context about the data within the notification can be seen by hovering the mouse over the emoji. In the image below, the mouse is hovering over folder icon, which then tells the user the data to the right is actually the "Rundeck group."

![picture](docs/images/slack-example-2.png)

___
## Acknowledgements

rundeck-diy-webhook-notification makes use of the open source projects listed on the [index.md](build/reports/dependency-license/index.md) in the build/reports/dependency-license directory. [Click here](build/reports/dependency-license/index.md) to be automatically redirected to the [index.md](build/reports/dependency-license/index.md).