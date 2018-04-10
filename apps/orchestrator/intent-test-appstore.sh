#!/bin/bash

onos-cell acino-local
BW=1000000
HOST1="00:00:00:00:00:01/None"
HOST2="00:00:00:00:00:02/None"
outputFile="intents.dat"
holdTimeStart=0
holdTimeIncrement=1
holdTimeEnd=500

echo "# generate the intents for testing the app store" > $outputFile

array_index=15501

for holdTime in $(seq $holdTimeStart $holdTimeIncrement $holdTimeEnd)
    do  
        echo "onos:add-host-intent -t IPV4 --ipProto TCP --tcpDst $array_index -b $BW  $HOST1 $HOST2" >> $outputFile
        ((array_index++))
    done

onos < $outputFile


