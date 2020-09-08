# Usage instructions for NanoPI NEO

  * Tested with Armbian: http://www.armbian.com/nanopi-neo/
  * Install Java: (https://launchpad.net/~webupd8team/+archive/ubuntu/java/+index?field.series_filter=xenial)  
    * `sudo add-apt-repository ppa:webupd8team/java && sudo apt-get update && sudo apt-get install oracle-java9-installer`
  * Install git  
    * `apt-get install git`
  * Get the code  
    * `git clone https://github.com/marcproe/librepilot.web.git`
  * Connect a flight controller with LibrePilot 16.09 RC1 flashed (connect only *1*, the server will claim the first it finds)
  * Start the Server  
    * `java -jar librepilot.web/out/artifacts/web_jar/web.jar`
  * Open the website on http://yourip:8080
