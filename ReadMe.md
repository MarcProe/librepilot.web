# Usage instructions for NanoPI NEO

  1. Tested with Armbian: http://www.armbian.com/nanopi-neo/
  2. Install Java: (https://launchpad.net/~webupd8team/+archive/ubuntu/java/+index?field.series_filter=xenial) 
    * `sudo add-apt-repository ppa:webupd8team/java && sudo apt-get update && sudo apt-get install oracle-java9-installer`
  3. Install git
    * `apt-get install git`
  4. Get the code
    * `git clone https://github.com/marcproe/librepilot.web.git`
  5. Connect a flight controller with LibrePilot flashed (connect only *1*, the server will claim the first it finds)
  6. Start the Server 
    * `java -jar librepilot.web/out/artifacts/web_jar/web.jar`
  7. Open the website on http://yourip:8080