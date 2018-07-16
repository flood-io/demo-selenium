build:
	javac -cp .:*:lib:lib/* -d lib src/io/flood/selenium/FloodSump.java

	javac -cp .:*:lib:lib/*:test/* test/*.java
	cp test/*.class .

stub:
	nc -l -u 35663

firefox: build
	docker run --rm -e JAVA_OPTS="-Dselenium.LOGGER.level=SEVERE" -d -p 4444:4444 --name=firefox selenium/standalone-firefox:3.11.0-californium || true
	WEBDRIVER_HOST=0.0.0.0 WEBDRIVER_PORT=4444 java -Duuid=`date +"%s"` -cp .:lib:lib/*:test/* ChallengeFirefox
	docker kill firefox

chrome: build
	docker run --rm -e JAVA_OPTS="-Dselenium.LOGGER.level=SEVERE" -d -p 4444:4444 --name=chrome selenium/standalone-chrome:3.11.0-californium || true
	WEBDRIVER_HOST=0.0.0.0 WEBDRIVER_PORT=4444 java -Duuid=`date +"%s"` -cp .:lib:lib/*:test/* ChallengeChrome
	docker kill chrome

helper:
	# First compile the package helper;
	mkdir /tmp/lib
	javac -cp .:*:lib:lib/* -d /tmp/lib src/helper/Test.java

	# Then JAR it, using the root of the packaged class e.g. /tmp/lib NOT /tmp/lib/helper/test/etc
	jar cvf /tmp/lib/test.jar -C /tmp/lib .
	rm -rf /tmp/lib/helper

	# Inspect the JAR
	jar tf /tmp/lib/test.jar

	# Make a zip of the 'lib' directory so flood knows to extract the JAR file(s) from this lib
	cd /tmp && zip -r helper.zip lib
	ls -alt /tmp/helper.zip
	cp /tmp/helper.zip test/
	rm -rf /tmp/lib

