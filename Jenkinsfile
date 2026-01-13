pipeline {
    agent any

    parameters {
        booleanParam(name: 'FORCE_ALL', defaultValue: false, description: 'Force build and deploy ALL components')
        booleanParam(name: 'FORCE_FRONTEND', defaultValue: false, description: 'Force build and deploy frontend only')
        booleanParam(name: 'FORCE_VEHICLE_SIMULATOR', defaultValue: false, description: 'Force build and deploy vehicle-simulator')
        booleanParam(name: 'FORCE_DATA_PROCESSOR', defaultValue: false, description: 'Force build and deploy data-processor')
        booleanParam(name: 'FORCE_ANALYTICS_SERVICE', defaultValue: false, description: 'Force build and deploy analytics-service')
        booleanParam(name: 'FORCE_NOTIFICATION_SERVICE', defaultValue: false, description: 'Force build and deploy notification-service')
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
        LOCATION             = 'germanywestcentral'
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

                    // Detect changes for each backend service
                    if (forceAll || params.FORCE_VEHICLE_SIMULATOR) {
                        env.VEHICLE_SIMULATOR_CHANGED = 'true'
                    } else {
                        env.VEHICLE_SIMULATOR_CHANGED = sh(
                            script: "git diff --name-only HEAD~1 HEAD | grep -q '^services/vehicle-simulator/' && echo 'true' || echo 'false'",
                            returnStdout: true
                        ).trim()
                    }

                    if (forceAll || params.FORCE_DATA_PROCESSOR) {
                        env.DATA_PROCESSOR_CHANGED = 'true'
                    } else {
                        env.DATA_PROCESSOR_CHANGED = sh(
                            script: "git diff --name-only HEAD~1 HEAD | grep -q '^services/data-processor/' && echo 'true' || echo 'false'",
                            returnStdout: true
                        ).trim()
                    }

                    if (forceAll || params.FORCE_ANALYTICS_SERVICE) {
                        env.ANALYTICS_SERVICE_CHANGED = 'true'
                    } else {
                        env.ANALYTICS_SERVICE_CHANGED = sh(
                            script: "git diff --name-only HEAD~1 HEAD | grep -q '^services/analytics-service/' && echo 'true' || echo 'false'",
                            returnStdout: true
                        ).trim()
                    }

                    if (forceAll || params.FORCE_NOTIFICATION_SERVICE) {
                        env.NOTIFICATION_SERVICE_CHANGED = 'true'
                    } else {
                        env.NOTIFICATION_SERVICE_CHANGED = sh(
                            script: "git diff --name-only HEAD~1 HEAD | grep -q '^services/notification-service/' && echo 'true' || echo 'false'",
                            returnStdout: true
                        ).trim()
                    }

                    // Detect frontend changes
                    if (forceAll || params.FORCE_FRONTEND) {
                        env.FRONTEND_CHANGED = 'true'
                    } else {
                        env.FRONTEND_CHANGED = sh(
                            script: "git diff --name-only HEAD~1 HEAD | grep -q '^frontend/' && echo 'true' || echo 'false'",
                            returnStdout: true
                        ).trim()
                    }

                    echo "========================================"
                    echo "Change Detection Results:"
                    echo "Force All: ${forceAll}"
                    echo "Vehicle Simulator: ${env.VEHICLE_SIMULATOR_CHANGED}"
                    echo "Data Processor: ${env.DATA_PROCESSOR_CHANGED}"
                    echo "Analytics Service: ${env.ANALYTICS_SERVICE_CHANGED}"
                    echo "Notification Service: ${env.NOTIFICATION_SERVICE_CHANGED}"
                    echo "Frontend: ${env.FRONTEND_CHANGED}"
                    echo "========================================"
                }
            }
        }

        // ============================================
        // BACKEND CI - Build & Test (Sequential to save RAM)
        // ============================================
        stage('Backend: Build vehicle-simulator') {
            when {
                expression { env.VEHICLE_SIMULATOR_CHANGED == 'true' }
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
                expression { env.DATA_PROCESSOR_CHANGED == 'true' }
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
                expression { env.ANALYTICS_SERVICE_CHANGED == 'true' }
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
                expression { env.NOTIFICATION_SERVICE_CHANGED == 'true' }
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
                anyOf {
                    expression { env.VEHICLE_SIMULATOR_CHANGED == 'true' }
                    expression { env.DATA_PROCESSOR_CHANGED == 'true' }
                    expression { env.ANALYTICS_SERVICE_CHANGED == 'true' }
                    expression { env.NOTIFICATION_SERVICE_CHANGED == 'true' }
                }
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
                anyOf {
                    expression { env.VEHICLE_SIMULATOR_CHANGED == 'true' }
                    expression { env.DATA_PROCESSOR_CHANGED == 'true' }
                    expression { env.ANALYTICS_SERVICE_CHANGED == 'true' }
                    expression { env.NOTIFICATION_SERVICE_CHANGED == 'true' }
                }
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

        stage('Backend: Build & Push vehicle-simulator') {
            when {
                expression { env.VEHICLE_SIMULATOR_CHANGED == 'true' }
            }
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

        stage('Backend: Build & Push data-processor') {
            when {
                expression { env.DATA_PROCESSOR_CHANGED == 'true' }
            }
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

        stage('Backend: Build & Push analytics-service') {
            when {
                expression { env.ANALYTICS_SERVICE_CHANGED == 'true' }
            }
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

        stage('Backend: Build & Push notification-service') {
            when {
                expression { env.NOTIFICATION_SERVICE_CHANGED == 'true' }
            }
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

        stage('Backend: Deploy vehicle-simulator') {
            when {
                expression { env.VEHICLE_SIMULATOR_CHANGED == 'true' }
            }
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    sh '''
                        # Setup variables for YAML substitution
                        export FULL_IMAGE_NAME="$ACR_LOGIN_SERVER/emergencywatch/vehicle-simulator:$GIT_COMMIT_SHORT"
                        export LOCATION="$LOCATION"
                        export SUB_ID="$AZURE_SUBSCRIPTION_ID"
                        export RESOURCE_GROUP="$RESOURCE_GROUP"
                        export ENVIRONMENT_NAME="$CONTAINER_ENV"
                        export ACR_NAME="$ACR_NAME"

                        echo "================================================"
                        echo "Deploying vehicle-simulator: $FULL_IMAGE_NAME"
                        echo "================================================"

                        # Generate final YAML with substitutions
                        envsubst < infra/containerapps/ew-vehicle-simulator.yml > deploy-simulator.yml

                        # Deploy using YAML
                        az containerapp create \
                            --name vehicle-simulator \
                            --resource-group $RESOURCE_GROUP \
                            --yaml deploy-simulator.yml

                        echo "✅ vehicle-simulator deployed with full configuration"
                    '''
                }
            }
        }

        stage('Backend: Deploy data-processor') {
            when {
                expression { env.DATA_PROCESSOR_CHANGED == 'true' }
            }
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    sh '''
                        # Setup variables for YAML substitution
                        export FULL_IMAGE_NAME="$ACR_LOGIN_SERVER/emergencywatch/data-processor:$GIT_COMMIT_SHORT"
                        export LOCATION="$LOCATION"
                        export SUB_ID="$AZURE_SUBSCRIPTION_ID"
                        export RESOURCE_GROUP="$RESOURCE_GROUP"
                        export ENVIRONMENT_NAME="$CONTAINER_ENV"
                        export ACR_NAME="$ACR_NAME"

                        echo "================================================"
                        echo "Deploying data-processor: $FULL_IMAGE_NAME"
                        echo "================================================"

                        # Generate final YAML with substitutions
                        envsubst < infra/containerapps/ew-data-processor.yml > deploy-processor.yml

                        # Deploy using YAML
                        az containerapp create \
                            --name data-processor \
                            --resource-group $RESOURCE_GROUP \
                            --yaml deploy-processor.yml

                        echo "✅ data-processor deployed with full configuration"
                    '''
                }
            }
        }

        stage('Backend: Deploy analytics-service') {
            when {
                expression { env.ANALYTICS_SERVICE_CHANGED == 'true' }
            }
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    sh '''
                        echo "🔍 Checking required tools on Jenkins agent..."

                        which envsubst || {
                           echo "❌ envsubst not installed (package: gettext)"
                           exit 1
                        }

                        echo "✅ Tool validation complete"

                        # Setup variables for YAML substitution
                        export FULL_IMAGE_NAME="$ACR_LOGIN_SERVER/emergencywatch/analytics-service:$GIT_COMMIT_SHORT"
                        export LOCATION="$LOCATION"
                        export SUB_ID="$AZURE_SUBSCRIPTION_ID"
                        export RESOURCE_GROUP="$RESOURCE_GROUP"
                        export ENVIRONMENT_NAME="$CONTAINER_ENV"
                        export ACR_NAME="$ACR_NAME"

                        echo "================================================"
                        echo "Deploying analytics-service: $FULL_IMAGE_NAME"
                        echo "================================================"

                        # Generate final YAML with substitutions
                        envsubst < infra/containerapps/ew-analytics-service.yml > deploy-analytics.yml

                        # --- DEBUG START ---
                        echo "===== Generated YAML ====="
                        sed -n '1,200p' deploy-analytics.yml
                        echo "=========================="

                        test -s deploy-analytics.yml || {
                          echo "❌ deploy-analytics.yml is empty"
                          exit 1
                        }

                        echo "🔍 CHECKING VARIABLES:"
                        echo "SUB_ID: '${SUB_ID}'"
                        echo "LOCATION: '${LOCATION}'"
                        echo "FULL_IMAGE_NAME: '${FULL_IMAGE_NAME}'"
                        echo "ENVIRONMENT_NAME": '${ENVIRONMENT_NAME}'""

                        echo "📄 CHECKING GENERATED YAML:"
                        # Use cat -e to show hidden characters (like tabs vs spaces)
                        cat -e deploy-analytics.yml
                        # --- DEBUG END ---

                        # Deploy using YAML with system-assigned managed identity
                        az containerapp create \
                            --name analytics-service \
                            --resource-group $RESOURCE_GROUP \
                            --environment $ENVIRONMENT_NAME \
                            --mi-system-assigned \
                            --yaml deploy-analytics.yml

                        echo "✅ analytics-service deployed with full configuration"
                    '''
                }
            }
        }

        stage('Backend: Deploy notification-service') {
            when {
                expression { env.NOTIFICATION_SERVICE_CHANGED == 'true' }
            }
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    sh '''
                        # Setup variables for YAML substitution
                        export FULL_IMAGE_NAME="$ACR_LOGIN_SERVER/emergencywatch/notification-service:$GIT_COMMIT_SHORT"
                        export LOCATION="$LOCATION"
                        export SUB_ID="$AZURE_SUBSCRIPTION_ID"
                        export RESOURCE_GROUP="$RESOURCE_GROUP"
                        export ENVIRONMENT_NAME="$CONTAINER_ENV"
                        export ACR_NAME="$ACR_NAME"

                        echo "================================================"
                        echo "Deploying notification-service: $FULL_IMAGE_NAME"
                        echo "================================================"

                        # Generate final YAML with substitutions
                        envsubst < infra/containerapps/ew-notification-service.yml > deploy-alerts.yml

                        # Deploy using YAML
                        az containerapp create \
                            --name notification-service \
                            --resource-group $RESOURCE_GROUP \
                            --yaml deploy-alerts.yml

                        echo "✅ notification-service deployed with full configuration"
                    '''
                }
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
                    expression {
                        env.VEHICLE_SIMULATOR_CHANGED == 'false' &&
                        env.DATA_PROCESSOR_CHANGED == 'false' &&
                        env.ANALYTICS_SERVICE_CHANGED == 'false' &&
                        env.NOTIFICATION_SERVICE_CHANGED == 'false'
                    }
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
