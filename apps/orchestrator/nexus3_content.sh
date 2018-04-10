#!/bin/bash

echo "Checking what content is uploaded at the kurgan.testbed.se nexus3 repo, in the maven-snapshots repository"

TOKEN=""

if [ -z "$TOKEN" ]; then
  URL="http://kurgan.testbed.se:8080/service/siesta/rest/beta/assets?repositoryId=maven-snapshots"
else
  URL="http://kurgan.testbed.se:8080/service/siesta/rest/beta/assets?repositoryId=maven-snapshots&continuationToken=$TOKEN"
fi

#echo "URL is: $URL" 

DATA=$(curl $URL 2> /dev/null)
echo $DATA | json_pp


TOKEN=$(echo $DATA | jq '.continuationToken' | sed 's/"//g')

while [ "$TOKEN" != "null" ] ; do 

#echo "Found token: $TOKEN"

if [ -z "$TOKEN" ]; then
#  echo "Token is empty"
  URL="http://kurgan.testbed.se:8080/service/siesta/rest/beta/assets?repositoryId=maven-snapshots"
else
#  echo "Token is not empty"
  URL="http://kurgan.testbed.se:8080/service/siesta/rest/beta/assets?repositoryId=maven-snapshots&continuationToken=$TOKEN"
fi

#echo "URL is: $URL" 

DATA=$(curl $URL 2>/dev/null)
echo $DATA | json_pp
TOKEN=$(echo $DATA | jq '.continuationToken' | sed 's/"//g')

done 