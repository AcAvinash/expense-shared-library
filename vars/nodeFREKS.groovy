def call(Map configMap){
    // mapName.get("key-name")
    def component = configMap.get("component")
    echo "component is : $component"
    pipeline {
        agent { node { label 'AGENT-1' } }
        environment{
            //here if you create any variable you will have global access, since it is environment no need of def
            packageVersion = '1.0.0'
        }
        
        stages {
            stage('Unit test') {
                steps {
                    echo "unit testing is done here"
                }
            }
            //sonar-scanner command expect sonar-project.properties should be available
            // stage('Sonar Scan') {
            //     steps {
            //         echo "Sonar scan done"
            //     }
            // }
            stage('Build') {
                steps {
                    sh 'ls -ltr'
                    sh "zip -r ${component}.zip ./* --exclude=.git --exclude=.zip"
                }
            }
            stage('SAST') {
                steps {
                    echo "SAST Done"
                    echo "package version: $packageVersion"
                }
            }
            //install pipeline utility steps plugin, if not installed
            stage('Publish Artifact') {
                steps {
                    nexusArtifactUploader(
                        nexusVersion: 'nexus3',
                        protocol: 'http',
                        nexusUrl: 'jenkins.joindevops.fun:8081',
                        groupId: 'com.expense',
                        version: "$packageVersion",
                        repository: "${component}",
                        credentialsId: 'nexus-auth',
                        artifacts: [
                            [artifactId: "${component}",
                            classifier: '',
                            file: "${component}.zip",
                            type: 'zip']
                        ]
                    )
                }
            }
            stage('Prepare frontend code') {
                steps {
                    sh '''

                    echo "Preparing frontend build context..."
                    rm -rf code
                    mkdir -p code
                     cp -r /usr/share/nginx/html/ code 
                    '''
                }
            }


            stage('Docker Build') {
                steps {
                    script{
                        sh """
                            docker build -t acavinash/${component}:${packageVersion} .
                        """
                    }
                }
            }
        //just make sure you login inside agent
            stage('Docker Push') {
                steps {
                    script{
                        sh """
                            docker push acavinash/${component}:${packageVersion}
                        """
                    }
                }
            }

            stage('EKS Deploy') {
                steps {
                    script{
                        sh """
                            cd helm
                            sed -i 's/IMAGE_VERSION/$packageVersion/g' values.yaml
                            helm install ${component} -n expense .
                        """
                    }
                }
            }


        }

        post{
            always{
                echo 'cleaning up workspace'
                //deleteDir()
            }
        }
    }
}