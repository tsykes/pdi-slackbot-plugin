# SlackBot - Help

This job entry allows you to send messages to Slack channels or groups.

## Message Settings


| Option                                 | Definition                                                                          |
|----------------------------------------|-------------------------------------------------------------------------------------|
| Step name                              | Name of this step as it appears in the job workspace                                |
| Token           | Slack Web API Token. Can be accessed at https://api.slack.com/web                                          |
| Update Channels                   | Button used to fetch a list of channels/groups that the provided token can access        |
| Post Type                     | Optional field that controls whether the Update Channels button retrieves channels or groups |
| Channel/Group                  | The name of the channel or group that you want to post to.                                  |
| Bot Name | The name that the bot will use to post                                                                            |
| Bot Icon                      | The emoticon that will show next to the bot's name when sending messages                     |


## Message


| Option                        | Definition                                                                                |
|-------------------------------|-------------------------------------------------------------------------------------------|
| Send Standard Success Message | Sends a standard message indicating the job was successful                                |
| Send Standard Failure Message | Sends a standard message indicating the job failed                                        |
| Send Custom Message           | Allows you to enter a custom message to be sent in the text box at the bottom of the step |