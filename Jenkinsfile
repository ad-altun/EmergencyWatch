pipeline {
    agent any

    parameters {
        booleanParam(name: 'FORCE_ALL', defaultValue: false, description: 'Force build and deploy ALL components (backend + frontend)')
        booleanParam(name: 'FORCE_BACKEND', defaultValue: false, description: 'Force build and deploy backend services only')
        booleanParam(name: 'FORCE_FRONTEND', defaultValue: false, description: 'Force build and deploy frontend only')
    }

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

        // Memory optimization for low-RAM servers
        MAVEN_OPTS           = '-Xmx512m -XX:+UseG1GC'
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
                    // Check if force parameters are set
                    def forceAll = params.FORCE_ALL ?: false
                    def forceBackend = params.FORCE_BACKEND ?: false
                    def forceFrontend = params.FORCE_FRONTEND ?: false

                    // Detect what changed (or use force flags)
                    if (forceAll || forceBackend) {
                        env.BACKEND_CHANGED = 'true'
                    } else {
                        env.BACKEND_CHANGED = sh(
                            script: "git diff --name-only HEAD~1 HEAD | grep -q '^services/' && echo 'true' || echo 'false'",
                            returnStdout: true
                        ).trim()
                    }

                    if (forceAll || forceFrontend) {
                        env.FRONTEND_CHANGED = 'true'
                    } else {
                        env.FRONTEND_CHANGED = sh(
                            script: "git diff --name-only HEAD~1 HEAD | grep -q '^frontend/' && echo 'true' || echo 'false'",
                            returnStdout: true
                        ).trim()
                    }

                    echo "========================================"
                    echo "Force All: ${forceAll}"
                    echo "Force Backend: ${forceBackend}"
                    echo "Force Frontend: ${forceFrontend}"
                    echo "Backend will build: ${env.BACKEND_CHANGED}"
                    echo "Frontend will build: ${env.FRONTEND_CHANGED}"
                    echo "========================================"
                }
            }
        }

        // ============================================
        // BACKEND CI - Build & Test (Sequential to save RAM)
        // ============================================
        stage('Backend: Build vehicle-simulator') {
            when {
                expression { env.BACKEND_CHANGED == 'true' }
            }
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

        stage('Backend: Build data-processor') {
            when {
                expression { env.BACKEND_CHANGED == 'true' }
            }
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

        stage('Backend: Build analytics-service') {
            when {
                expression { env.BACKEND_CHANGED == 'true' }
            }
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

        stage('Backend: Build notification-service') {
            when {
                expression { env.BACKEND_CHANGED == 'true' }
            }
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
            steps {
                // Sequential Docker builds to save RAM
                dir('services/vehicle-simulator') {
                    sh '''
                        docker build -t $ACR_LOGIN_SERVER/emergencywatch/vehicle-simulator:$GIT_COMMIT_SHORT \
                                     -t $ACR_LOGIN_SERVER/emergencywatch/vehicle-simulator:latest .
                        docker push $ACR_LOGIN_SERVER/emergencywatch/vehicle-simulator:$GIT_COMMIT_SHORT
                        docker push $ACR_LOGIN_SERVER/emergencywatch/vehicle-simulator:latest
                    '''
                }
                dir('services/data-processor') {
                    sh '''
                        docker build -t $ACR_LOGIN_SERVER/emergencywatch/data-processor:$GIT_COMMIT_SHORT \
                                     -t $ACR_LOGIN_SERVER/emergencywatch/data-processor:latest .
                        docker push $ACR_LOGIN_SERVER/emergencywatch/data-processor:$GIT_COMMIT_SHORT
                        docker push $ACR_LOGIN_SERVER/emergencywatch/data-processor:latest
                    '''
                }
                dir('services/analytics-service') {
                    sh '''
                        docker build -t $ACR_LOGIN_SERVER/emergencywatch/analytics-service:$GIT_COMMIT_SHORT \
                                     -t $ACR_LOGIN_SERVER/emergencywatch/analytics-service:latest .
                        docker push $ACR_LOGIN_SERVER/emergencywatch/analytics-service:$GIT_COMMIT_SHORT
                        docker push $ACR_LOGIN_SERVER/emergencywatch/analytics-service:latest
                    '''
                }
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

        stage('Backend: Deploy to Container Apps') {
            when {
                expression { env.BACKEND_CHANGED == 'true' }
            }
            steps {
                sh '''
                    # Function to deploy a container app (create if not exists, update if exists)
                    deploy_containerapp() {
                        local APP_NAME=$1
                        local IMAGE=$2

                        echo "Deploying $APP_NAME..."

                        if az containerapp show --name $APP_NAME --resource-group $RESOURCE_GROUP &>/dev/null; then
                            echo "$APP_NAME exists, updating..."
                            az containerapp update \
                                --name $APP_NAME \
                                --resource-group $RESOURCE_GROUP \
                                --image $IMAGE
                        else
                            echo "$APP_NAME does not exist, creating..."
                            az containerapp create \
                                --name $APP_NAME \
                                --resource-group $RESOURCE_GROUP \
                                --environment $CONTAINER_ENV \
                                --image $IMAGE \
                                --registry-server $ACR_LOGIN_SERVER \
                                --registry-identity system \
                                --cpu 0.5 \
                                --memory 1.0Gi \
                                --min-replicas 0 \
                                --max-replicas 1
                        fi
                    }

                    # Deploy all services
                    deploy_containerapp "vehicle-simulator" "$ACR_LOGIN_SERVER/emergencywatch/vehicle-simulator:$GIT_COMMIT_SHORT"
                    deploy_containerapp "data-processor" "$ACR_LOGIN_SERVER/emergencywatch/data-processor:$GIT_COMMIT_SHORT"
                    deploy_containerapp "analytics-service" "$ACR_LOGIN_SERVER/emergencywatch/analytics-service:$GIT_COMMIT_SHORT"
                    deploy_containerapp "notification-service" "$ACR_LOGIN_SERVER/emergencywatch/notification-service:$GIT_COMMIT_SHORT"
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

                    # Upload all files with short cache (for HTML and root files)
                    az storage blob upload-batch \
                        --source frontend/dist \
                        --destination '$web' \
                        --account-name $STORAGE_ACCOUNT \
                        --auth-mode login \
                        --content-cache-control "public, max-age=3600"

                    # Overwrite assets/ with long cache (hashed JS, CSS files)
                    az storage blob upload-batch \
                        --source frontend/dist \
                        --destination '$web' \
                        --account-name $STORAGE_ACCOUNT \
                        --auth-mode login \
                        --content-cache-control "public, max-age=31536000, immutable" \
                        --pattern "assets/*"
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
            // Email notifications - requires SMTP configuration in Jenkins
            // Manage Jenkins → System → E-mail Notification
            // Uncomment when SMTP is configured:
            // mail to: 'email@example.com',
            //      subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
            //      body: "Something went wrong with ${env.BUILD_URL}"
        }
    }
}
