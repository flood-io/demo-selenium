# Flood Selenium

This is a simple test repository for the Flood reporting engine used by Selenium in Tricentis Flood. The FloodSump itself is stubbed and will print results out to screen. The results are similar in value to what is recorded on Tricentis Flood. You can use this library to test your Tricentis Flood purposed Selenium scripts locally.

You can use the Makefile on OSX / 'nix with `make chrome` or `make firefox`. Alternatively you can do this by hand below.

# OSX / 'nix
```
# Run a UDP stub for reporting endpoint of FloodSUmp
nc -l -u 35663

# Compile FloodSump
javac -cp .:*:lib:lib/* -d lib src/io/flood/selenium/FloodSump.java

# Compile your example code
javac -cp .:*:lib:lib/* test/*.java
cp test/*.class .

# Run headless doocker container
docker run --rm -e JAVA_OPTS="-Dselenium.LOGGER.level=SEVERE" -d -p 4444:4444 --name=firefox selenium/standalone-firefox:3.11.0-californium || true

# Run your example code
WEBDRIVER_HOST=0.0.0.0 WEBDRIVER_PORT=4444 java -Duuid=`date +"%s"` -cp .:lib:lib/*:test/* ChallengeFirefox

...
# View results in your stub
Jun 01, 2018 11:38:11 AM org.openqa.selenium.remote.DesiredCapabilities chrome
INFO: Using `new ChromeOptions()` is preferred to `DesiredCapabilities.chrome()`
Jun 01, 2018 11:38:14 AM org.openqa.selenium.remote.ProtocolHandshake createSession
INFO: Detected dialect: OSS
Jun 01, 2018 11:38:14 AM io.flood.selenium.FloodSump openClient
INFO: connecting to flood sump results at localhost:35663
Starting iteration 0
Starting iteration 1
...
response_time,account=1,flood=1,region=local,grid=1,node=1,project=1,label=Challenge+Step+2
...

docker kill firefox
```

# Windows
```
"%ProgramFiles%\Java\jdk1.8.0_144\bin\javac" -cp .;*;lib;lib\* -d lib src\io\flood\selenium\FloodSump.java
"%ProgramFiles%\Java\jdk1.8.0_144\bin\javac" -cp .;*;lib;lib\* test\*.java
copy test\*.class .
```

Note for windows, make sure you [download a copy of Chrome driver](https://chromedriver.storage.googleapis.com/index.html) and copy it somewhere like `C:/Webdriver/chromedriver.exe`

```
"%ProgramFiles%\Java\jdk1.8.0_144\bin\java" -jar lib\selenium-server-standalone-2.53.0.jar -port 4444 -debug -Dwebdriver.chrome.driver="C:/Webdriver/chromedriver.exe"
```

```
set WEBDRIVER_HOST=localhost
set WEBDRIVER_PORT=4444
"%ProgramFiles%\Java\jdk1.8.0_144\bin\java" -Duuid=abcd123 -cp .;lib;lib/*;test/* Challenge
```

