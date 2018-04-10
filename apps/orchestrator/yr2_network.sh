#!/bin/bash
# Test topology
source ~/.kv-bash/kv-bash

host=${1:-127.0.0.1}

if [ $# -eq 1 ]
  then
    onos ${host} <<-EOF
    app activate org.onosproject.orchestrator
EOF

fi

# clean up and create devices 
onos ${host} <<-EOF
wipe-out please
#app uninstall org.onosproject.null
app activate org.onosproject.orchestrator
app activate org.onosproject.null
EOF

sleep 2
onos ${host} <<-EOF
null-simulation start custom
EOF

sleep 5
onos ${host} <<-EOF

cfg set org.onosproject.orchestrator.netrap.impl.NetRapTransactionImpl nullProvider true


# null-create-roadm <name> <line port> <client port> <latitude> <longitude>
# Create three roadm with 2 line ports and 3 client ports, at x/y 0 0 
null-create-roadm madrid 2 3 0 0
null-create-roadm brussels 2 3 1 0
null-create-roadm trento 2 3 0 1

null-create-device router j1 4 40.0 -3.0 
null-create-device router j2 4 50.5 4.0
null-create-device router j3 4 46.01 11.2

null-create-device switch madridsw 4   40.416775 -3.703790
null-create-device switch brusselssw 4 50.878899  4.355607
null-create-device switch trentosw 4 47.01 12.2

EOF

# Give the devices some time to actually get created, otherwise, NullPointerExceptions may occour 
sleep 4
# Create some links
onos ${host} <<-EOF
# Link the three roadms

acino:create-annotated-link -a LengthInKm -a 400.0 optical madrid_bottom brussels_bottom
acino:create-annotated-link -a LengthInKm -a 400.0 -a PropagationSpeed -a 200000 optical madrid_bottom trento_bottom
acino:create-annotated-link -a LengthInKm -a 400.0 -a PropagationSpeed -a 200000 optical brussels_bottom trento_bottom
#acino:create-annotated-link -a LengthInKm -a 400.0 optical madrid_bottom trento_bottom
#acino:create-annotated-link -a LengthInKm -a 400.0 optical brussels_bottom trento_bottom

#null-create-link optical madrid_bottom trento_bottom
#null-create-link optical brussels_bottom trento_bottom

# create links between transponders and routers, J1 - A1 
null-create-link direct j1 madrid_tr_0
null-create-link direct j1 madrid_tr_1
null-create-link direct j1 madrid_tr_2

# create links between transponders and routers, J2 - A2 
null-create-link direct j2 brussels_tr_0
null-create-link direct j2 brussels_tr_1
null-create-link direct j2 brussels_tr_2

# create links between transponders and routers, J1 - A1 
null-create-link direct j3 trento_tr_0
null-create-link direct j3 trento_tr_1
null-create-link direct j3 trento_tr_2

# create links between cpes and routers
null-create-link direct j1 madridsw
null-create-link direct j2 brusselssw
null-create-link direct j3 trentosw

# add some hosts
#null-create-host madridsw 10.0.0.1 41.016775 -3.203790
#null-create-host madridsw 10.0.0.2 41.016775 -4.203790

#null-create-host brusselssw 10.0.0.10 51.58899  3.855607
#null-create-host brusselssw 10.0.0.20 51.58899  4.755607
EOF

# annotate all links with latency 
# annotate-links latency 0.01

## Find all IP links we have created, they are marked with DIRECT
linkit=0
declare -a links
for n in $(onos ${host}  links | grep DIRECT | egrep -o -e '(null:[0-9a-f/]+)') ; do
    links[$linkit]=$n
    linkit=$(( $linkit + 1 ))
done
## Iterate through link pairs and annotate with "do not delete me"
## as well as lengthInKm
count=0
echo "" > tmpcmd
while [ "x${links[count]}" != "x" ]
do
    cmd=$(printf "annotate-link ${links[count]} ${links[$(( $count + 1 ))]} 'do not delete me' true")
    echo $cmd >> tmpcmd
    cmd=$(printf "annotate-link ${links[count]} ${links[$(( $count + 1 ))]} LengthInKm 1.0")
    echo $cmd >> tmpcmd
    count=$(( $count + 2 ))
done
#sleep 10
onos ${host} < tmpcmd
rm tmpcmd 

ssh-keygen -f "/home/aghafoor/.ssh/known_hosts" -R [localhost]:8101 ; sshpass -p karaf  ssh -p 8101 -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no karaf@localhost devices | while read CMD; do 
  [[ $CMD =~ id=([^,]+).+name=(.+) ]]
  ID=${BASH_REMATCH[1]}
  NAME=${BASH_REMATCH[2]}
  kvset $NAME $ID
done

onos ${host} <<-EOF
# latitude = y
#null-create-device switch j2 4 50.5 4.0
## Brussels node
# brussels_tr_0
annotate-device $(kvget brussels_tr_0) latitude 50.0
annotate-device $(kvget brussels_tr_0) longitude 2.5
# brussels_tr_1
annotate-device $(kvget brussels_tr_1) latitude 50.0
annotate-device $(kvget brussels_tr_1) longitude 4.0
# brussels_tr_2
annotate-device $(kvget brussels_tr_2) latitude 50.0
annotate-device $(kvget brussels_tr_2) longitude 5.5
# brussels_top
annotate-device $(kvget brussels_top) latitude 49.5
annotate-device $(kvget brussels_top) longitude 4.0
# brussels_bottom
annotate-device $(kvget brussels_bottom) latitude 48.5
annotate-device $(kvget brussels_bottom) longitude 5.0

# Madrid node
#null-create-device router j1 4 40.0 -3.0 
# madrid_tr_0
annotate-device $(kvget madrid_tr_0) latitude 39.5
annotate-device $(kvget madrid_tr_0) longitude -2.0
# madrid_tr_1
annotate-device $(kvget madrid_tr_1) latitude 40.0
annotate-device $(kvget madrid_tr_1) longitude -2.0
# madrid_tr_2
annotate-device $(kvget madrid_tr_2) latitude 40.5
annotate-device $(kvget madrid_tr_2) longitude -2.0
# madrid_top
annotate-device $(kvget madrid_top) latitude 40.0
annotate-device $(kvget madrid_top) longitude -1.0
# madrid_bottom
annotate-device $(kvget madrid_bottom) latitude 40.5 
annotate-device $(kvget madrid_bottom) longitude -0.1

# Trento node
#null-create-device router j3 4 46.01 11.2
# trento_tr_0
annotate-device $(kvget trento_tr_0) latitude 45.5
annotate-device $(kvget trento_tr_0) longitude 10.0
# trento_tr_1
annotate-device $(kvget trento_tr_1) latitude 46.0
annotate-device $(kvget trento_tr_1) longitude 10.0
# trento_tr_2
annotate-device $(kvget trento_tr_2) latitude 46.5
annotate-device $(kvget trento_tr_2) longitude 10.0
# trento_top
annotate-device $(kvget trento_top) latitude 46.0
annotate-device $(kvget trento_top) longitude 9.5
# trento_bottom
annotate-device $(kvget trento_bottom) latitude 45.0
annotate-device $(kvget trento_bottom) longitude 8.942184

annotate-link null:0000000000000001/3 null:0000000000000002/4 originalNode rest:FSP3000_196
annotate-link null:0000000000000002/1 null:0000000000000005/2 originalNode rest:FSP3000_196
annotate-link null:0000000000000002/2 null:0000000000000004/2 originalNode rest:FSP3000_196
annotate-link null:0000000000000002/3 null:0000000000000003/2 originalNode rest:FSP3000_196
annotate-link null:0000000000000002/4 null:0000000000000001/3 originalNode rest:FSP3000_196
annotate-link null:0000000000000003/2 null:0000000000000002/3 originalNode rest:FSP3000_196
annotate-link null:0000000000000004/2 null:0000000000000002/2 originalNode rest:FSP3000_196
annotate-link null:0000000000000005/2 null:0000000000000002/1 originalNode rest:FSP3000_196
annotate-link null:0000000000000006/3 null:0000000000000007/4 originalNode rest:FSP3000_196
annotate-link null:0000000000000007/1 null:000000000000000a/2 originalNode rest:FSP3000_196
annotate-link null:0000000000000007/2 null:0000000000000009/2 originalNode rest:FSP3000_196
annotate-link null:0000000000000007/3 null:0000000000000008/2 originalNode rest:FSP3000_196
annotate-link null:0000000000000007/4 null:0000000000000006/3 originalNode rest:FSP3000_196
annotate-link null:0000000000000008/2 null:0000000000000007/3 originalNode rest:FSP3000_196
annotate-link null:0000000000000009/2 null:0000000000000007/2 originalNode rest:FSP3000_196
annotate-link null:000000000000000a/2 null:0000000000000007/1 originalNode rest:FSP3000_196
annotate-link null:000000000000000b/3 null:000000000000000c/4 originalNode rest:FSP3000_196
annotate-link null:000000000000000c/1 null:000000000000000f/2 originalNode rest:FSP3000_196
annotate-link null:000000000000000c/2 null:000000000000000e/2 originalNode rest:FSP3000_196
annotate-link null:000000000000000c/3 null:000000000000000d/2 originalNode rest:FSP3000_196
annotate-link null:000000000000000c/4 null:000000000000000b/3 originalNode rest:FSP3000_196
annotate-link null:000000000000000d/2 null:000000000000000c/3 originalNode rest:FSP3000_196
annotate-link null:000000000000000e/2 null:000000000000000c/2 originalNode rest:FSP3000_196
annotate-link null:000000000000000f/2 null:000000000000000c/1 originalNode rest:FSP3000_196




EOF

echo "[
   {
      \"name\":\"ControlBrussels\",
      \"endpoints\":[
         {
            \"end_point\":\".IPEndPoint\",
            \"router_id\":\"$(kvget j2)\",
            \"port_id\":3,
            \"in_addr\":\"\t10.0.0.1/24\",
            \"subnets\":\"255.255.255.0\"
         }
      ]
   },
   {
      \"name\":\"DronesMadrid\",
      \"endpoints\":[
         {
            \"end_point\":\".IPEndPoint\",
            \"router_id\":\"$(kvget j3)\",
            \"port_id\":3,
            \"in_addr\":\"10.0.0.2/24\",
            \"subnets\":\"255.255.255.0\"
         }
      ]
   },
   {
      \"name\":\"RouterMadrid\",
      \"endpoints\":[
         {
            \"end_point\":\".IPEndPoint\",
            \"router_id\":\"$(kvget j1)\",
            \"port_id\":3,
            \"in_addr\":\"10.0.0.3/24\",
            \"subnets\":\"255.255.255.0\"
         }
      ]
   }
]
" > /home/aghafoor/Desktop/Development/acino/others/controlpoints.json
