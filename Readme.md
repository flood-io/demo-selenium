# Flood Selenium

This is a simple test repository for the Flood reporting engine used by Selenium in Flood IO. The FloodSump itself is stubbed and will print results out to screen. The results are similar in value to what is recorded on Flood IO. You can use this library to test your Flood IO purposed Selenium scripts locally.

Included is a simple makefile to compile and execute the example test. This assumes `selenium-server -port 4444` is already running on localhost and you have the chrome driver installed. Modify variables to suit.

# OSX / 'nix
```
make challenge
javac -cp .:*:lib:lib/* -d lib src/io/flood/selenium/FloodSump.java
javac -cp .:*:lib:lib/* test/*.java
cp test/*.class .
WEBDRIVER_HOST=127.0.0.1 WEBDRIVER_PORT=4444 java -Duuid=`date +"%s"` -cp .:lib:lib/*:test/* Challenge
...
test started
{response_data=, response_code=200, source_host=null, active_threads_in_group=null, latency=3433, end_time=1463393920232, label=Flood IO Script Challenge, error_count=0, uuid=1463393912, url=https://challengers.flood.io/, start_time=1463393914489, thread_id=null, response_headers=, bytes=2656, connect_time=1417, sample_count=1, request_headers=, response_time=5743, active_threads=null, request_data=, timestamp=1463393920232, successful=true}
...
```

# Windows
```
"%ProgramFiles%\Java\jdk1.8.0_144\bin\javac" -cp .;*;lib;lib\* -d lib src\io\flood\selenium\FloodSump.java
"%ProgramFiles%\Java\jdk1.8.0_144\bin\javac" -cp .;*;lib;lib\* test\*.java
copy test\*.class .

"%ProgramFiles%\Java\jdk1.8.0_144\bin\java" -jar lib\selenium-server-standalone-2.53.0.jar -port 4444 -debug

set WEBDRIVER_HOST=localhost
set WEBDRIVER_PORT=4444
"%ProgramFiles%\Java\jdk1.8.0_144\bin\java" -Duuid=abcd123 -cp .;lib;lib/*;test/* Challenge
```
