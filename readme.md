
## Install

	mvn clean install

## Document
	
	mvn site

Then visit target/dokka/noze/index.html

## Build

	mvn assembly:assembly -DdescriptorId=jar-with-dependencies

## Run

	java -jar target/noze-0.0-jar-with-dependencies.jar

## Test

	mvn test

