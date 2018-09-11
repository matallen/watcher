oc cp ~/.go/bin/drive `oc get pods | grep Running | awk '{print $1}'`:/home/jboss/
