/bin/sh

POD=`oc get pods | grep Running | awk '{print $1}'`
echo `oc cp ~/.go/bin/drive $POD:/home/jboss/`
echo `oc exec $POD mkdirs google_drive/.gd`
echo `oc cp gd_credentials.json $POD:/home/jboss/google_drive/.gd/credentials.json`

echo "Deployed 'drive' to $POD:/home/jboss"
