pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
        nodejs 'NodeJS'
    }

    environment {
        // Azure Service Principal
        AZURE_CLIENT_ID     = credentials('AZURE_CLIENT_ID')
        AZURE_CLIENT_SECRET = credentials('AZURE_CLIENT_SECRET')
        AZURE_TENANT_ID     = credentials('AZURE_TENANT_ID')
        AZURE_SUBSCRIPTION_ID = credentials('AZURE_SUBSCRIPTION_ID')

        // Azure Resources
        RESOURCE_GROUP       = 'emergencywatch-rg'
        ACR_NAME             = 'emergencywatchacr'
        ACR_LOGIN_SERVER     = "${ACR_NAME}.azurecr.io"
        CONTAINER_ENV        = 'emergencywatch-env'
        STORAGE_ACCOUNT      = 'emergencywatchfe'

        // Cloudflare (optional - for cache purge)
        CLOUDFLARE_ZONE_ID   = credentials('CLOUDFLARE_ZONE_ID')
        CLOUDFLARE_API_TOKEN = credentials('CLOUDFLARE_API_TOKEN')

        // Build Info
        BUILD_VERSION        = "${env.BUILD_NUMBER}"
        GIT_COMMIT_SHORT     = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    // Detect what changed
                    env.BACKEND_CHANGED = sh(
                        script: "git diff --name-only HEAD~1 HEAD | grep -q '^services/' && echo 'true' || echo 'false'",
                        returnStdout: true
                    ).trim()
                    env.FRONTEND_CHANGED = sh(
                        script: "git diff --name-only HEAD~1 HEAD | grep -q '^frontend/' && echo 'true' || echo 'false'",
                        returnStdout: true
                    ).trim()

                    echo "Backend changed: ${env.BACKEND_CHANGED}"
                    echo "Frontend changed: ${env.FRONTEND_CHANGED}"
                }
            }
        }

        // ============================================
        // BACKEND CI - Build & Test
        // ============================================
        stage('Backend: Build & Test') {
            when {
                expression { env.BACKEND_CHANGED == 'true' }
            }
            parallel {
                stage('vehicle-simulator') {
                    steps {
                        dir('services/vehicle-simulator') {
                            sh 'mvn clean verify -DskipTests=false'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: 'services/vehicle-simulator/target/surefire-reports/*.xml'
                        }
                    }
                }

                stage('data-processor') {
                    steps {
                        dir('services/data-processor') {
                            sh 'mvn clean verify -DskipTests=false'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: 'services/data-processor/target/surefire-reports/*.xml'
                        }
                    }
                }

                stage('analytics-service') {
                    steps {
                        dir('services/analytics-service') {
                            sh 'mvn clean verify -DskipTests=false'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: 'services/analytics-service/target/surefire-reports/*.xml'
                        }
                    }
                }

                stage('notification-service') {
                    steps {
                        dir('services/notification-service') {
                            sh 'mvn clean verify -DskipTests=false'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: 'services/notification-service/target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }

        stage('Backend: Code Coverage') {
            when {
                expression { env.BACKEND_CHANGED == 'true' }
            }
            steps {
                // Using Coverage Plugin (replacement for deprecated JaCoCo plugin)
                recordCoverage(
                    tools: [[parser: 'JACOCO']],
                    sourceCodeRetention: 'EVERY_BUILD'
                )
            }
        }

        // ============================================
        // BACKEND CD - Docker Build & Push to ACR
        // ============================================
        stage('Backend: Azure Login') {
            when {
                expression { env.BACKEND_CHANGED == 'true' }
            }
            steps {
                sh '''
                    az login --service-principal \
                        -u $AZURE_CLIENT_ID \
                        -p $AZURE_CLIENT_SECRET \
                        --tenant $AZURE_TENANT_ID

                    az account set --subscription $AZURE_SUBSCRIPTION_ID
                    az acr login --name $ACR_NAME
                '''
            }
        }

        stage('Backend: Build & Push Docker Images') {
            when {
                expression { env.BACKEND_CHANGED == 'true' }
            }
            parallel {
                stage('Docker: vehicle-simulator') {
                    steps {
                        dir('services/vehicle-simulator') {
                            sh '''
                                docker build -t $ACR_LOGIN_SERVER/emergencywatch/vehicle-simulator:$GIT_COMMIT_SHORT \
                                             -t $ACR_LOGIN_SERVER/emergencywatch/vehicle-simulator:latest .
                                docker push $ACR_LOGIN_SERVER/emergencywatch/vehicle-simulator:$GIT_COMMIT_SHORT
                                docker push $ACR_LOGIN_SERVER/emergencywatch/vehicle-simulator:latest
                            '''
                        }
                    }
                }

                stage('Docker: data-processor') {
                    steps {
                        dir('services/data-processor') {
                            sh '''
                                docker build -t $ACR_LOGIN_SERVER/emergencywatch/data-processor:$GIT_COMMIT_SHORT \
                                             -t $ACR_LOGIN_SERVER/emergencywatch/data-processor:latest .
                                docker push $ACR_LOGIN_SERVER/emergencywatch/data-processor:$GIT_COMMIT_SHORT
                                docker push $ACR_LOGIN_SERVER/emergencywatch/data-processor:latest
                            '''
                        }
                    }
                }

                stage('Docker: analytics-service') {
                    steps {
                        dir('services/analytics-service') {
                            sh '''
                                docker build -t $ACR_LOGIN_SERVER/emergencywatch/analytics-service:$GIT_COMMIT_SHORT \
                                             -t $ACR_LOGIN_SERVER/emergencywatch/analytics-service:latest .
                                docker push $ACR_LOGIN_SERVER/emergencywatch/analytics-service:$GIT_COMMIT_SHORT
                                docker push $ACR_LOGIN_SERVER/emergencywatch/analytics-service:latest
                            '''
                        }
                    }
                }

                stage('Docker: notification-service') {
                    steps {
                        dir('services/notification-service') {
                            sh '''
                                docker build -t $ACR_LOGIN_SERVER/emergencywatch/notification-service:$GIT_COMMIT_SHORT \
                                             -t $ACR_LOGIN_SERVER/emergencywatch/notification-service:latest .
                                docker push $ACR_LOGIN_SERVER/emergencywatch/notification-service:$GIT_COMMIT_SHORT
                                docker push $ACR_LOGIN_SERVER/emergencywatch/notification-service:latest
                            '''
                        }
                    }
                }
            }
        }

        stage('Backend: Deploy to Container Apps') {
            when {
                expression { env.BACKEND_CHANGED == 'true' }
            }
            steps {
                sh '''
                    # Deploy vehicle-simulator
                    az containerapp update \
                        --name vehicle-simulator \
                        --resource-group $RESOURCE_GROUP \
                        --image $ACR_LOGIN_SERVER/emergencywatch/vehicle-simulator:$GIT_COMMIT_SHORT

                    # Deploy data-processor
                    az containerapp update \
                        --name data-processor \
                        --resource-group $RESOURCE_GROUP \
                        --image $ACR_LOGIN_SERVER/emergencywatch/data-processor:$GIT_COMMIT_SHORT

                    # Deploy analytics-service
                    az containerapp update \
                        --name analytics-service \
                        --resource-group $RESOURCE_GROUP \
                        --image $ACR_LOGIN_SERVER/emergencywatch/analytics-service:$GIT_COMMIT_SHORT

                    # Deploy notification-service
                    az containerapp update \
                        --name notification-service \
                        --resource-group $RESOURCE_GROUP \
                        --image $ACR_LOGIN_SERVER/emergencywatch/notification-service:$GIT_COMMIT_SHORT
                '''
            }
        }

        // ============================================
        // FRONTEND CD - Build & Deploy to Blob Storage
        // ============================================
        stage('Frontend: Build') {
            when {
                expression { env.FRONTEND_CHANGED == 'true' }
            }
            steps {
                dir('frontend') {
                    sh '''
                        npm ci
                        npm run build
                    '''
                }
            }
        }

        stage('Frontend: Azure Login') {
            when {
                allOf {
                    expression { env.FRONTEND_CHANGED == 'true' }
                    expression { env.BACKEND_CHANGED == 'false' }
                }
            }
            steps {
                sh '''
                    az login --service-principal \
                        -u $AZURE_CLIENT_ID \
                        -p $AZURE_CLIENT_SECRET \
                        --tenant $AZURE_TENANT_ID

                    az account set --subscription $AZURE_SUBSCRIPTION_ID
                '''
            }
        }

        stage('Frontend: Deploy to Blob Storage') {
            when {
                expression { env.FRONTEND_CHANGED == 'true' }
            }
            steps {
                sh '''
                    # Delete old files
                    az storage blob delete-batch \
                        --source '$web' \
                        --account-name $STORAGE_ACCOUNT \
                        --auth-mode login

                    # Upload new files with proper cache headers
                    # Long cache for hashed assets (JS, CSS)
                    az storage blob upload-batch \
                        --source frontend/dist \
                        --destination '$web' \
                        --account-name $STORAGE_ACCOUNT \
                        --auth-mode login \
                        --content-cache-control "public, max-age=31536000, immutable" \
                        --pattern "assets/*"

                    # Short cache for HTML and other files
                    az storage blob upload-batch \
                        --source frontend/dist \
                        --destination '$web' \
                        --account-name $STORAGE_ACCOUNT \
                        --auth-mode login \
                        --content-cache-control "public, max-age=3600" \
                        --pattern "*" \
                        --exclude-pattern "assets/*"
                '''
            }
        }

        stage('Frontend: Purge Cloudflare Cache') {
            when {
                expression { env.FRONTEND_CHANGED == 'true' }
            }
            steps {
                sh '''
                    curl -s -X POST "https://api.cloudflare.com/client/v4/zones/$CLOUDFLARE_ZONE_ID/purge_cache" \
                        -H "Authorization: Bearer $CLOUDFLARE_API_TOKEN" \
                        -H "Content-Type: application/json" \
                        --data '{"purge_everything":true}'
                '''
            }
        }
    }

    post {
        always {
            // Logout from Azure
            sh 'az logout || true'

            // Clean workspace
            cleanWs()
        }
        success {
            echo '''
            =========================================
            ✅ Build & Deployment Successful!
            =========================================
            '''
        }
        failure {
            echo '''
            =========================================
            ❌ Build or Deployment Failed!
            =========================================
            '''
            // enable email notifications
            mail to: 'contact@denizaltun.de',
                 subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
                 body: "Something went wrong with ${env.BUILD_URL}"
        }
    }
}
