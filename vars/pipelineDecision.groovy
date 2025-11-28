#!groovy

def decidePipleine(Map configMap){
    application = configMap.get("application")
    //# here we are getting nodeJSVM
    switch(application) {
        case 'nodeJSVM':
            echo "application is Node JS and VM based"
            nodeJSVMCI(configMap)
            break
        case 'nodeFREKS':
            echo "application is Node JS and VM based"
            nodeFREKS(configMap)
            break
        case 'JavaVM':
            javaVMCI(configMap)
            break
        case 'javaEKS':
            javaEKS(configMap)
            break
        default:
            error "Un recognised application"
            break
    }
}