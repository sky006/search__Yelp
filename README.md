#JAVA_VERSION 9.0.4
#Step 0: Crawler

#Step 1: File Preprocess
java -jar fileProcess.jar Preprocess data foodWords.txt
#Step 2: Lucene Index:
java -jar index.jar index luceneIndex data
#Step 3: Hadoop Index: (On Ubuntu)
/usr/local/hadoop/bin/hadoop jar commentIndex.jar WordCount data rawCommentIndex
/usr/local/hadoop/bin/hadoop jar keyIndexer.jar WordCount data rawKeyIndex
#Step 4: association: (On Ubuntu)
/usr/local/hadoop/bin/hadoop jar association.jar data output1 output2 association
#Step 5: seperate hadoop index
Creat keyIndex folder and commentIndex folder first!
java -jar fileProcess.jar seperateIndex rawKeyIndex keyIndex
java -jar fileProcess.jar seperateIndex rawCommentIndex commentIndex
#Step 6: Deploy web application
For convinence, all the folders needed to deploy were provided

On windows move the files (keyIndex,commentIndex,association,data )to D:/sourceData
Copy search.war to Tomcat Webapps
Run startup.bat

On Linux move the files (keyIndex,commentIndex,association,data )to /usr/local/sourceData
Copy search.war to Tomcat Webapps
Run startup.sh