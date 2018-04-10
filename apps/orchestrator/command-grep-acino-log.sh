#!/bin/bash
cat /tmp/onos-1.11.0-SNAPSHOT/apache-karaf-3.0.8/data/log/acino.log | grep -E 'Future failed|Compilation|ServiceAction|Intents submitted|FlowRule submitted'