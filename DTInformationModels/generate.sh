# Export all the meta needed from the blend file
$1 "./3d mock glb/3d mock glb export.blend" -b  --python-text "Export Data" ;
mv -f ./reference.json ./roads/model ;
cd ./roads ;
ant ;
cd .. ;
cp -rf ./roads/model/Interchange1.model ./server/models;

# Export all the glb files needed from the blend file
mkdir ./model ;
$1 "./3d mock glb/3d mock glb export.blend" -b  --python-text "Export Model" ;
for mesh in ./model/*; 
do 
    gltf-transform etc1s $mesh $mesh & 
    if [ $(jobs -r -p | wc -l) -ge 8 ];
    then
        wait -n
    fi
done ;
wait ;
rm -r ./server/meshes/Interchange1 ;
mkdir ./server/meshes ;
mkdir ./server/meshes/Interchange1 ;
mv ./model/* ./server/meshes/Interchange1/ ;
rm -r ./model 

