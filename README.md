# Java Agent for Aino.io
Java implementation of Aino.io logging agent.

## What is [Aino.io](http://aino.io) and what does this Agent have to do with it?

[Aino.io](http://aino.io) is an analytics and monitoring tool for integrated enterprise applications and digital
business processes. Aino.io can help organizations manage, develop, and run the digital parts of their day-to-day
business. Read more from our [web pages](http://aino.io).

Aino.io works by analyzing transactions between enterprise applications and other pieces of software.
This Agent helps to store data about the transactions to Aino.io platform using Aino.io Data API (version 2.0).
See [API documentation](http://www.aino.io/api) for detailed information about the API.


## Introduction

This project contains Java implementation for [aino.io](http://aino.io) logging.

## Usage
Before using the agent, it needs to be configured. Agent can be configured manually or via configuration file.

### Configuring the agent
Agent is configured with XML configuration file. Below is example.

```java
File configFile = new File("/path/to/conf_file.xml");
Logger ainoLogger = Logger.getFactory().setConfigurationBuilder(new FileConfigBuilder(configFile)).build();
```


##### Configuration file example
```xml
<ainoConfig>
    <ainoLoggerService enabled="true">
        <address uri="http://localhost:8808/api/1.0/saveLogArray" apiKey="YOUR API KEY GOES HERE"/>
        <send interval="5000" sizeThreshold="30" gzipEnabled="true" />
    </ainoLoggerService>
    <operations>
        <operation key="create" name="Create" />
        <operation key="update" name="Update" />
        <operation key="delete" name="Delete" />
    </operations>
    <applications>
        <application key="app01" name="TestApp 1"/>
        <application key="app02" name="TestApp 2" />
    </applications>
    <idTypes>
        <idType key="dataType01" name="Data Type 1" />
        <idType key="dataType02" name="Data Type 5" />
    </idTypes>
    <payloadTypes>
        <payloadType key="subInterface01" name="Interface 1" />
        <payloadType key="subInterface02" name="Interface 2" />
    </payloadTypes>
</ainoConfig>
```


### Logging to aino.io
Logging is done by creating a `Transaction` object and passing it to the agent:
```java
// Create transaction object
Transaction transaction = ainoLogger.newTransaction();

// .. add data to transaction object
transaction.setFlowId("1249F41E55A1123FB");
transaction.setToKey("application1");
transaction.setFromKey("application2");
transaction.setStatus("success");
transaction.setMessage("Data transfer successful.");
transaction.addMetadata("Extra data", "Oh sleep! it is a gentle thing, Beloved from pole to pole!");

// Send entry to aino.io
ainoLogger.addTransaction(entry);                // Do the actual logging to aino.io
```


## Developing
The agent is developed with Java 6 and Maven 3.

If you made a nifty feature to this agent, why not open a pull request so that others can enjoy it also?!
We hope it has unit tests.

## Contributors

- [Jarkko Kallio](https://github.com/kallja)
- [Pekka Heino](https://github.com/heinop)
- [Aleksi Mustonen](https://github.com/aleksimustonen)
- [Jussi Mikkonen](https://github.com/jussi-mikkonen)
- [Esa Heikkinen](https://github.com/esaheikkinen)
- [Ville Harvala](https://github.com/vharvala)

## [License](LICENSE)

Copyright &copy; 2016 [Aino.io](http://aino.io). Licensed under the [Apache 2.0 License](LICENSE).
