# SlackBot - PDI Plugin

[![Build Status](https://travis-ci.org/graphiq-data/pdi-slackbot-plugin.svg?branch=master)](https://travis-ci.org/graphiq-data/pdi-slackbot-plugin)

This job entry allows you to send messages to Slack channels or groups.

## Use Cases
This is a useful alternative to providing status update alerts via email for several reasons:

+ Slack has smart notification rules, which allows you to respond to urgent messages and get to non-critical issues when you have time
+ You can create a group/channel for each job or group the alerts into one group/channel, either way it's easy for you and others to organize your alerts
+ Slack is an extensible environment that means you can design custom integrations that trigger other actions based on the content of messages posted to a given room
+ You can send messages when frequent jobs complete successfully without worrying about clogging up your inbox

## Development
### Build
To build (requires Apache Ant and Apache Ivy)
```bash
# from the project root
ant resolve  # resolves dependencies
ant -f build/build.xml dist  # compiles project
```
### Install
1. Update the value for `kettle-dir` in `build/build.properties` to point to your `data-integration` folder
2. Run the ant install task (once again from project root)

```bash
ant -f build/build.xml install  # compiles project and installs plugin
```
### Testing
1. Create the file `build/creds.properties`

2. Add a property called `ltoken` with your [slack token](https://api.slack.com/web)

```
ltoken=<your token here>
```

3. Use the following command to run functional tests

```
ant -f build/build.xml test  # run tests
```

## Authors
+ [Andrew Overton](https://team.graphiq.com/l/232/Andrew-Overton) - aoverton at graphiq dot com
+ [Matt Rybak](https://team.graphiq.com/l/270/Matthew-Rybak) - mrybak at graphiq dot com
