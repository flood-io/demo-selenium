build:
	javac -cp .:*:lib:lib/* -d lib src/io/flood/selenium/FloodSump.java

	javac -cp .:*:lib:lib/* test/*.java
	cp test/*.class .

challenge: build
	WEBDRIVER_HOST=127.0.0.1 WEBDRIVER_PORT=4444 java -Duuid=`date +"%s"` -cp .:lib:lib/*:test/* Challenge

etsy: build
	WEBDRIVER_HOST=127.0.0.1 WEBDRIVER_PORT=4444 java -Duuid=`date +"%s"` -cp .:lib:lib/*:test/* Etsy

stubhub: build
	WEBDRIVER_HOST=127.0.0.1 WEBDRIVER_PORT=4444 java -Duuid=`date +"%s"` -cp .:lib:lib/*:test/* Stubhub

loadtest: build
	WEBDRIVER_HOST=127.0.0.1 WEBDRIVER_PORT=4444 java -Duuid=`date +"%s"` -cp .:lib:lib/*:test/* Loadtest

customer: build
	WEBDRIVER_HOST=127.0.0.1 WEBDRIVER_PORT=4444 java -Duuid=`date +"%s"` -cp .:lib:lib/*:test/* Customer

slowpage: build
	WEBDRIVER_HOST=127.0.0.1 WEBDRIVER_PORT=4444 java -Duuid=`date +"%s"` -cp .:lib:lib/*:test/* Slowpage

debug: build
	WEBDRIVER_HOST=127.0.0.1 WEBDRIVER_PORT=4444 java -Duuid=`date +"%s"` -cp .:lib:lib/*:test/* Debug
