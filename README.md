# ACINO Orchestrator

The [ACINO](http://www.acino.eu) (Application Centric IP/optical Network Orchestration) project proposes to develop and demonstrate a dynamic, application-centric, multi-layer SDN network orchestrator, where the traffic of each application receives a tailored service at each layer of the transport network, thereby overcoming the gap that the grooming layer introduces between application service requirements and their fulfilment in the lowers layer of the stack.

The ACINO Orchestrator is an open-source, vendor-agnostic modular framework, which exposes to network applications a set of high-level primitives for specifying service requirements, as well as performs multi-layer (IP and optical) planning and optimization processes to translate these requirements into configuration requests for the underlying IP/MPLS and Optical network layers.
The architecture of the Orchestrator (see figure below) can be broken down into four different layers. Starting from the top, the Northbound Interface allows applications to specify their network control desires in form of high-level policies. The Core Layer orchestrates the interaction between the applications and the underlying infrastructure. Among such applications, NetRAP enables the communication between the Orchestrator and external online planning and path computation tools. In the project, we adopted Net2Plan [Net2Plan](https://www.net2plan.com) to implement resource allocation and network optimization algorithms. The Abstraction Layer translates device-dependent objects received from the southbound interfaces into device-agnostic structures for the Core Layer. Finally, the Southbound Interface implements protocols and device-specific drivers for the communication with physical and emulated infrastructures.

![Alt text](/architecture.png?raw=true " ")

The implementation of the ACINO Orchestrator is based on [ONOS](https://onosproject.org/), an open source SDN network operating system whose modules are managed as OSGi bundles inside an Apache Karaf framework. In this regard, the guidelines provided below start with the installation procedure of the ONOS framework, as presented on the [ONOS wiki - Installing and Running ONOS](https://wiki.onosproject.org/display/ONOS/Installing+and+Running+ONOS), to continue with the configuration of the Orchestrator parameters.

## Installation
We define as _development machine_ the PC where the Orchestrator is built and tested, while we the _deployment machine_ is the virtual machine/server where the Orchestrator is deployed and connected to the network infrastructure (real or emulated).
The development machine stores the Orchestrator’s source code and all the tools needed to build and debug it. From the development machine, the Orchestrator’s executable and libraries are eventually deployed locally or to a remote deployment machine.

Additional information can be found on the [ONOS wiki - Installing and Running ONOS](https://wiki.onosproject.org/display/ONOS/Installing+and+Running+ONOS).

### Prerequisites for development machine

The recommended basic requirements of the development machine are:

- Ubuntu 14.04/16.04 LTS 64bit or OS X (Mavericks and later)
- 4GB of RAM and 2 cores

The software prerequisites of the development machine are:

- Java 8 JDK (Oracle Java recommended; OpenJDK is not as thoroughly tested)

```bash
build:~$ sudo apt-get install software-properties-common -y
build:~$ sudo add-apt-repository ppa:webupd8team/java -y
build:~$ sudo apt-get update
build:~$ sudo apt-get install oracle-java8-installer oracle-java8-set-default -y
```


We assume in this guide “~/onos” as the top-level path of the ONOS source tree of the ACINO Orchestrator.
To be able to use the [ONOS tools](https://wiki.onosproject.org/display/ONOS/Adding+ONOS+utility+scripts+and+functions+to+your+environment) and instructions discussed in this guide, the ```$ONOS_ROOT``` environment variable must be exported in the shell profile (e.g., . bash_aliases, .profile ) and it must refer to the root directory of the ONOS source tree. For example, Ubuntu users can add the following lines to the
```~/.bashrc``` file:

```bash
export ONOS_ROOT=~/onos
. $ONOS_ROOT/tools/dev/bash_profile
```

### Prerequisites for the deployment machine

The deployment machine is usually a virtual machine or a server connected to the network infrastructure where the ACINO Orchestrator is deployed and executed. This Section can be skipped in case the Orchestrator is deployed locally on the development machine. The recommended basic requirements of the deployment machine are:

- Ubuntu server 14.04/16.04 LTS 64bit
- 2GB of RAM and 2 cores

The software prerequisites of the deployment machine are:
- Java 8 JDK (Oracle Java recommended; OpenJDK is not as thoroughly tested)

- Password-less sudo privilege.
E.g., `sudo visudo` and add the following line:

```sdn ALL=(ALL) NOPASSWD:ALL```

To make the process easier, password-less login (e.g., key-based SSH login) configured in the deployment machine is recommended. The onos-push-keys utility can be used to transfer public keys to the deployment machine:

```bash
build:~$ onos-push-keys 192.168.56.10
```

## ACINO Orchestrator configuration and start-up

As you may know, ONOS is a distributed control plane, but the first implementation of the ACINO Orchestrator has been tested
and deployed only with a single instance. The next steps describe the ACINO Orchestrator installation for the single instance execution.

The first step is to export the environment variables used by the ONOS tools to deploy and start the control plane.
The easiest way is to use the so-called [Test Cell](https://wiki.onosproject.org/display/ONOS/Environment+setup+with+cells), a set of configuration files placed in folder: `$ONOS_ROOT/tools/test/cells`. The command cells displays all the available configurations, that can be edited using the command `vicell <name of the cell>` and executed using  `cell <name of the cell>`. Remember to lunch the command `cell <name of the cell>` in each terminal tab used to running ONOS tools (e.g., onos-log, onos-service).

The repository contains an examples of the cell ```acino-nms-tid``` used respectively to configure a local and a remote instance of the ACINO Orchestrator. Both cells should be changed in compliant of your environment as the following example states:

    #Subnet of the management interface (delete for local environment)
    export ONOS_NIC="10.95.86.*"

    #IP address of the deployment machine (can be 127.0.0.1 for local environment)
    export OC1="10.95.86.49"

    #Modules to be loaded at start-up. In bold the one used by the ACINO Orchestrator.
    export ONOS_APPS="drivers,drivers.tapi,restsb,netcfghostprovider,netcfgclientportsprovider,orchestrator,linkdiscovery,drivers.juniper"

    #User and group of the deployment machine.
    export ONOS_USER="sdn"
    export ONOS_GROUP="sdn"

The following commands are used to compile the ACINO Orchestrator and create the package containing
the executable binaries:

```bash
build:~$ obf
```

If you want to run the ACINO Orchestrator on your development machine, the following command must be used:

```bash
build:~$ ok clean
```

Instead, if you want to trigger the the remote installation of the ACINO orchestrator, the ONOS stc tool can be used:

```bash
build:~$ stc setup
```

It will install all the modules and will check if they are installed without errors. The latest steps are needed to inject the configuration to reach the network devices and to inject undiscoverable links (e.g., connection between the optical and the IP devices) and hosts (e.g., subnet behind the routers). This example files is available in the repository in the path: `$ONOS_ROOT/tools/test/configs/acino-testbed-configuration.json`

The configuration can be injected into the ACINO orchestrator with the commands:

```bash
build:~$ onos-netcfg $OC1 $ONOS_ROOT/tools/test/configs/acino-testbed-configuration.json
```
