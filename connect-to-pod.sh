oc rsh `oc get pods | grep Running | awk '{print $1}'`
