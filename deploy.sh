
oc new-project data-backup
oc process --param=APPLICATION_NAME=data-backup -f backup-build.yaml | oc apply -f-
oc process --param=NAMESPACE=$NAMESPACE --param=APPLICATION_NAME=data-backup -f backup-deployment.yaml | oc apply -f-
