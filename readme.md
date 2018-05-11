LightningScorer - Blazing fast PMML scoring web service
========

LightningScorer is a lightning fast web service for scoring PMML files built with an emphasis on speed, reliability and simplicity.

# Features

* Fast. LightningScorer boasts of [Rapidoid](https://github.com/rapidoid/rapidoid) - under the hood which allows you to break speed records. See our benchmarks below.
* Reliable. Support for PMML specification 3.0 to 4.3 thanks to [JPMML-Evaluator](https://github.com/jpmml/jpmml-evaluator) as PMML engine.
* Easy to use.
	* Simple API for Model deploy, undeploy and score operations
	* Stand-alone jar for easy deployment
* Lightweight. Minimalistic approach in terms of dependency.

# Benchmark

### How
After a cold start for servers, 6K models are deployed, scored and undeployed for 10 cycles. 
Time is measured for each request and added up.
We use Openscoring as baseline because this is one of the most successful PMML scoring rest api out there.
As logging can change the performance of each call, we set log level to minimum so that we can be fair while evaluating.

### Results
Results are in terms of seconds and the lower the better.

| Undeploy | Deploy | Score |
| ![Undeploy benchmark](https://raw.githubusercontent.com/sezinkarli/lightningscorer/master/undeploy-performance.png) | ![Deploy benchmark](https://raw.githubusercontent.com/sezinkarli/lightningscorer/master/deploy-performance.png) | ![Score benchmark](https://raw.githubusercontent.com/sezinkarli/lightningscorer/master/score-performance.png) |


# Installation and Usage

LightningScorer is maven-based and requires Java 1.8 or newer.

### Build
Enter the project directory and build using [Apache Maven](http://maven.apache.org/):
```
mvn clean install
```

### Use

Build process will produce a jar with all the necessary dependencies in it called  `lightningscorer-uberjar-1.0.jar` in the target directory. 
That is all you need to run LightningScorer server.

For launching your server, go to target folder and execute this command:
```
java -jar lightningscorer-uberjar-1.0.jar
```

By default, the REST web service is started at [http://localhost:8080/](http://localhost:8080/).
When you check you will see the following message if the server is up and running.

```
{
data: "I have come here to chew bubblegum and kick ass...",
success: true
}
```

There are several configurations you can tweak. 
You can change the logging settings to your liking by adding a file named `tinylog.properties`.
If you'd like a helping hand check `tinylog.properties.example` in the resource folder for an example property file with a console and file logger. You can either remove ".example" suffix and build to have a logging config or you can build your tiny log property file and put it next to your jar file so that the server can use it.

You can also change ip and port of LightningScorer by adding a config.yml to your classpath.
Again, you can use the example file from resource folder named `config.yml.example`.
It is quite self-explanatory as you can see:

```
on:
  port: 8081
  address: 0.0.0.0
```

# REST API

### Overview

REST API endpoints:

| HTTP method | Endpoint | Description |
| ----------- | --------  | ----------- |
| POST | /model/{modelId} | Deploy model with model id {modelId} |
| DELETE | /model/{modelId} | Undeploy model with model id {modelId}|
| POST | /model/{modelId}/score | Score model with model id {modelId} |
| GET | /model/ids | Get all model ids |
| GET | /model/additionals | Get additional parameters of models |
| GET | /model/{modelId}/additional | Get additional parameters of the given model id {modelId} |

If your request is successful the response of the server will be in this format:
```
{
data: "Your data in json format",
success: true
}
```

If your request got an exception you will get the following response in json format:

```
{
  "data": null,
  "success": false,
  "exceptionType": "Simple name of the application class",
  "exceptionMessage": "Exception message"
}
```
Notice that success response and exception response structures are similar with the difference that success does not have exception related fields.


### Model Deployment

##### POST /model/{modelId}

Deploys the model with model id {modelId}.
If there is already a model with the same model id, the server will warn you in the logs and replace the old one with the new one.
Keep in mind that PMML file should be POSTed in multipart mode and the form parameter name of the file must be "model".

Additional parameters you will send as request parameters will also be persisted. So, for instance, you have a certain variance value associated with your model you can persist that next to your model.

Sample curl:
```
curl -F model=@123456.xml http://localhost:8080/model/123456
```

with additional parameters:
```
curl -F model=@123456.xml http://localhost:8080/model/123456?param1=value1&param2=value2
```

Sample success response:
```
{
	"data":true,
	"success":true
}
```

### Model Undeployment

##### DELETE /model/{modelId}

Deploys the model with model id {modelId}.
If there are no model found with the given model id, it will throw an exception.

Sample curl:
```
curl -X DELETE localhost:8080/model/123456
```

Sample success response:
```
{
	"data":true,
	"success":true
}
```

### Model Scoring

##### POST /model/{modelId}/score

Scores the model with model id {modelId}.
If model id cannot be found or model input parameters are empty, it wil throw an exception.

Input parameters for the scoring must be in this format:

```
{
	"fields": {
		"inputKey1" : "inputValue1",
		"inputKey2" : "inputValue2"
	}
}
```

Sample curl:
```
curl -X POST "http://localhost:8080/model/123456/score" -H "Content-Type: application/json" -d '{"fields": {"field1":1, "field2":2}}'
```

Sample success response:
```
{
	"data":{
		"result":
		{
			"value1": 2
		}
	},
	"success":true
}
```

### Retrieval of All Model IDs

#####  GET /model/ids

Fetches all model ids. 
Quite useful for knowing what you can score through LightningScorer.

Sample curl:
```
curl -X GET localhost:8080/model/ids
```

Sample response:
```
{
	"data":["123456", "234567"],
	"success":true
}
```
###  Retrieval of All Additional Parameters of each Model ID

##### GET /model/additionals

Fetches all model ids with additional parameters.
Additional parameters can be persisted while deploying models.
This method will not return all model ids but only the ones with additional parameter values.

Sample curl:
```
curl -X GET localhost:8080/model/additionals
```

Sample response:
```
{
	"data":{
		"123456":{"key1":"value1"}
	},
	"success":true
}
```
`123456` is model id here. Key-value pair is the additional parameter associated with it. It is possible to have more than one additional parameter.

### Retrieval of Additional Parameters for given Model ID

##### GET /model/{modelId}/additional

Fetches additional parameters of given model id.
Will throw exception if given model id could not be found, or the model does not have persisted additional parameters.

Sample curl:
```
curl -X GET localhost:8080/model/123456/additional
```

Sample response:
```
{
	"data":
	{
		"key1":"value1",
		"key2":"value2"
	}
	,
	"success":true
}
```

# License

LightningScorer is released under Apache Public License v2, so it is free to use for both commercial and non-commercial projects.