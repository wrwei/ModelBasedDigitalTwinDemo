# Requirements
- Make sure Eclipse Epsilon 2.5 IDE is installed
- Make sure ant is installed (i.e. sudo apt install ant)
- Make sure Blender is installed (i.e. via Steam)
- Make sure gltf-transform is installed (i.e. via npm)

# Setup
- run ```chmod +x generate.sh```
- run ```chmod +x build.sh```
- run ```./generate.sh {location of blender installed}```
  - example if installed via steam maybe: ```/home/$USER/.steam/debian-installation/steamapps/common/Blender/blender```
  
# Build
- run ```./build.sh```
- output with all the files needed to run is in ```export.zip```
