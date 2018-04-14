build:
	javac -cp .:*:lib:lib/* -d lib src/io/flood/selenium/FloodSump.java

	javac -cp .:*:lib:lib/* test/*.java
	cp test/*.class .

firefox: build
	docker run --rm -e JAVA_OPTS="-Dselenium.LOGGER.level=SEVERE" -d -p 4444:4444 --name=firefox selenium/standalone-firefox:3.11.0-californium || true
	WEBDRIVER_HOST=0.0.0.0 WEBDRIVER_PORT=4444 java -Duuid=`date +"%s"` -cp .:lib:lib/*:test/* ChallengeFirefox
	docker kill firefox

chrome: build
	docker run --rm -e JAVA_OPTS="-Dselenium.LOGGER.level=SEVERE" -d -p 4444:4444 --name=chrome selenium/standalone-chrome:3.11.0-californium || true
	WEBDRIVER_HOST=0.0.0.0 WEBDRIVER_PORT=4444 java -Duuid=`date +"%s"` -cp .:lib:lib/*:test/* ChallengeChrome
	docker kill chrome
