cd ./server ;
mvn clean compile assembly:single ;
cp ./target/digital-road-3d-0.0.0-jar-with-dependencies.jar ./ ;
zip -r export.zip ./generations ./helpers ./meshes ./metas ./models ./operations ./digital-road-3d-0.0.0-jar-with-dependencies.jar ;
rm ./digital-road-3d-0.0.0-jar-with-dependencies.jar ;
mv ./export.zip ../ ;
