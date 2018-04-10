#!/bin/bash
# Test topology

# cpe1 -> j1 
# cpe2 -> j2 
# cpe3 -> j3



host=${1:-127.0.0.1}

source ~/.kv-bash/kv-bash
ssh-keygen -f "/home/ponsko/.ssh/known_hosts" -R [localhost]:8101 ; sshpass -p karaf  ssh -p 8101 -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no karaf@localhost devices | while read CMD; do 
  [[ $CMD =~ id=([^,]+).+name=(.+) ]]
  ID=${BASH_REMATCH[1]}
  NAME=${BASH_REMATCH[2]}
  kvset $NAME $ID
done


# null:0000000000000010 - j1 
# null:0000000000000011 - j2 
# null:0000000000000012 - j3
# null:0000000000000013 - cpe1
# null:0000000000000014 - cpe2 
# null:0000000000000015 - cpe3

#j1=null:0000000000000010
#j2=null:0000000000000011
#j3=null:0000000000000012
#cpe1=null:0000000000000013
#cpe2=null:0000000000000014
#cpe3=null:0000000000000015
MAXLAT=1000
onos ${host} <<-EOF
# successful! though what is port 13/3 ??
# Between all CPEs
set DEBUG org.onosproject.net.intent

# test between routers, 6 GB, distances from 1-14 km, should work
# fail acino:add-new-aci-intent -t ARP  -b 6000000000 -l $MAXLAT  $(kvget j2)/4 $(kvget j1)/4
# fail acino:add-new-aci-intent -t ARP  -b 6000000000 -l $MAXLAT  $(kvget j1)/4 $(kvget j3)/4
# fail acino:add-new-aci-intent -t ARP  -b 6000000000 -l $MAXLAT  $(kvget j2)/4 $(kvget j3)/4

# test between routers, 0.8 GB, distances from 1-14 km, should work
#acino:add-new-aci-intent -t ARP  -b 800000000 -l $MAXLAT  $(kvget j2)/4 $(kvget j1)/4
#acino:add-new-aci-intent -t ARP  -b 800000000 -l $MAXLAT  $(kvget j1)/4 $(kvget j3)/4
#acino:add-new-aci-intent -t ARP  -b 800000000 -l $MAXLAT  $(kvget j2)/4 $(kvget j3)/4

acino:add-new-aci-intent -t ARP  -b 80000000000 -l $MAXLAT  $(kvget j2)/4 $(kvget j1)/4

acino:add-new-aci-intent -t ARP  -b 80000000000 -l $MAXLAT  $(kvget j1)/4 $(kvget j3)/4
acino:add-new-aci-intent -t ARP  -b 80000000000 -l $MAXLAT  $(kvget j2)/4 $(kvget j3)/4


# 900 meg

#acino:add-new-aci-intent -t ARP  -b 60000000000 -l $MAXLAT  $(kvget j1)/4 $(kvget j3)/4
#acino:add-new-aci-intent -t ARP  -b 6000000000 -l $MAXLAT  $(kvget j1)/4 $(kvget j3)/4
#acino:add-new-aci-intent -t ARP  -b 600000000 -l $MAXLAT  $(kvget j1)/4 $(kvget j3)/4
#acino:add-new-aci-intent -t ARP  -b 60000000 -l $MAXLAT  $(kvget j1)/4 $(kvget j3)/4
#acino:add-new-aci-intent -t ARP  -b 6000000 -l $MAXLAT  $(kvget j1)/4 $(kvget j3)/4
#
#acino:add-new-aci-intent -t ARP  -b 60000000000 -l $MAXLAT  $(kvget j1)/4 $(kvget j2)/4
#acino:add-new-aci-intent -t ARP  -b 6000000000 -l $MAXLAT  $(kvget j1)/4 $(kvget j2)/4
#acino:add-new-aci-intent -t ARP  -b 600000000 -l $MAXLAT  $(kvget j2)/4 $(kvget j3)/4
#acino:add-new-aci-intent -t ARP  -b 60000000 -l $MAXLAT  $(kvget j2)/4 $(kvget j3)/4
#acino:add-new-aci-intent -t ARP  -b 6000000 -l $MAXLAT  $(kvget j2)/4 $(kvget j3)/4



#acino:add-new-aci-intent -t ARP  -b 10000000000 -l $MAXLAT $(kvget controlsw)/4 $(kvget dronesw)/4
#acino:add-new-aci-intent -t ARP  -b 1000000000 -l $MAXLAT $(kvget controlsw)/4 $(kvget dronesw)/4
#acino:add-new-aci-intent -t ARP  -b 100000000 -l $MAXLAT $(kvget controlsw)/4 $(kvget dronesw)/4
#acino:add-new-aci-intent -t ARP  -b 10000000 -l $MAXLAT $(kvget controlsw)/4 $(kvget dronesw)/4
#acino:add-new-aci-intent -t ARP  -b 1000000 -l $MAXLAT $(kvget controlsw)/4 $(kvget dronesw)/4
#acino:add-new-aci-intent -t ARP  -b 10000000 -l $MAXLAT $(kvget controlsw)/4 $(kvget dronesw)/4

#acino:add-new-aci-intent -t ARP  -b 60000000000 -l $MAXLAT  $(kvget dronesw)/4 $(kvget controlsw)/4
#acino:add-new-aci-intent -t ARP  -b 6000000000 -l $MAXLAT  $(kvget dronesw)/4 $(kvget controlsw)/4
#acino:add-new-aci-intent -t ARP  -b 600000000 -l $MAXLAT  $(kvget dronesw)/4 $(kvget controlsw)/4
#acino:add-new-aci-intent -t ARP  -b 60000000 -l $MAXLAT  $(kvget dronesw)/4 $(kvget controlsw)/4
#acino:add-new-aci-intent -t ARP  -b 6000000 -l $MAXLAT  $(kvget dronesw)/4 $(kvget controlsw)/4

#acino:add-new-aci-intent -t ARP  -b 60000000000 -l $MAXLAT  $(kvget j1)/4 $(kvget j3)/4
#acino:add-new-aci-intent -t ARP  -b 6000000000 -l $MAXLAT  $(kvget j1)/4 $(kvget j3)/4
#acino:add-new-aci-intent -t ARP  -b 600000000 -l $MAXLAT  $(kvget j1)/4 $(kvget j3)/4
#acino:add-new-aci-intent -t ARP  -b 60000000 -l $MAXLAT  $(kvget j1)/4 $(kvget j3)/4
#acino:add-new-aci-intent -t ARP  -b 6000000 -l $MAXLAT  $(kvget j1)/4 $(kvget j3)/4



#acino:add-new-aci-intent -t ARP  -b 900000000 -l 200 $(kvget controlsw)/4 $(kvget dronesw)/4

#acino:add-new-aci-intent -t ARP -b 200000 -l 200 $(kvget dronesw)/4 $(kvget controlsw)/4
#acino:add-new-aci-intent -t ARP -b 200000 -l 200 $(kvget j3)/1 $(kvget controlsw)/4

#acino:add-new-aci-intent -t ARP -b 200000000 -l 200 $(kvget j3)/2 $(kvget j2)/1


# Set of intents to test IP_OPT_7 reordering
#acino:add-new-aci-intent -t ARP -b 200000000 -l 200 $(kvget cpe1)/2 $(kvget cpe3)/1
#acino:add-new-aci-intent -t ARP -b 600000000 -l 200 $(kvget cpe1)/2 $(kvget cpe2)/2
#acino:add-new-aci-intent -t ARP -b 600000000 -l 200 $(kvget cpe2)/2 $(kvget cpe3)/1
#acino:add-new-aci-intent -t ARP -b 9900000000 -l 200 $(kvget cpe1)/2 $(kvget cpe3)/1


# 100 Mbit, 200, 300 .. 
#acino:add-new-aci-intent -t ARP -b 100000000 -l 200 $(kvget cpe1)/2 $(kvget cpe3)/1
#acino:add-new-aci-intent -t ARP -b 200000000 -l 200 $(kvget cpe1)/2 $(kvget cpe2)/2
#acino:add-new-aci-intent -t ARP -b 300000000 -l 200 $(kvget cpe2)/2 $(kvget cpe3)/1


#acino:add-new-aci-intent -t ARP -b 0.1 -l 200 $(kvget cpe2)/2 $(kvget cpe1)/2
#acino:add-new-aci-intent -t ARP -b 0.1 -l 200 $(kvget cpe2)/2 $(kvget cpe3)/2
#acino:add-new-aci-intent -t ARP -b 0.1 -l 200 $(kvget cpe3)/2 $(kvget cpe1)/2
#acino:add-new-aci-intent -t ARP -b 0.1 -l 200 $(kvget cpe3)/2 $(kvget cpe1)/2

# higher bandwidth, between routers
#acino:add-new-aci-intent -t ARP -b 3.0 -l 200 $(kvget j1)/2 $(kvget j2)/2
#acino:add-new-aci-intent -t ARP -b 3.0 -l 200 $(kvget j1)/2 $(kvget j3)/2
#acino:add-new-aci-intent -t ARP -b 3.0 -l 200 $(kvget j2)/2 $(kvget j3)/2



EOF


