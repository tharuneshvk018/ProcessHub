
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        DOCKER = 'C:\\Users\\tharu\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe'

        MAVEN_IMAGE = 'maven:3.9.9-eclipse-temurin-21-alpine'
        NODE_IMAGE = 'node:22-alpine'

        BACKEND_IMAGE = 'processhub-backend:ci'
        FRONTEND_IMAGE = 'processhub-frontend:ci'

        SONAR_HOST_URL = 'http://host.docker.internal:9000'
    }

    stages {

        // STAGE 1: GIT CHECKOUT
        stage('1. Git Checkout') {
            steps {
                echo 'Checking out ProcessHub from GitHub'

                checkout scm

                bat '''
                    echo Repository:
                    git remote -v

                    echo Latest commit:
                    git log -1 --oneline
                '''
            }
        }

        // STAGE 2: BACKEND VALIDATION
        stage('2. Backend Validation') {
            steps {
                echo 'Validating backend Maven project'

                bat '''
                    "%DOCKER%" run --rm ^
                    -v "%WORKSPACE%/backend:/workspace" ^
                    -w /workspace ^
                    %MAVEN_IMAGE% ^
                    mvn -B validate
                '''
            }
        }

        // STAGE 3: BACKEND COMPILATION
        stage('3. Backend Compilation') {
            steps {
                echo 'Compiling ProcessHub backend'

                bat '''
                    "%DOCKER%" run --rm ^
                    -v "%WORKSPACE%/backend:/workspace" ^
                    -w /workspace ^
                    %MAVEN_IMAGE% ^
                    mvn -B compile
                '''
            }
        }

        // STAGE 4: BACKEND TESTING
        stage('4. Backend Testing') {
            steps {
                echo 'Running backend tests and generating coverage report'

                bat '''
                    "%DOCKER%" run --rm ^
                    -v "%WORKSPACE%/backend:/workspace" ^
                    -w /workspace ^
                    %MAVEN_IMAGE% ^
                    mvn -B verify
                '''
            }

            post {
                always {
                    junit(
                        testResults: 'backend/target/surefire-reports/*.xml',
                        allowEmptyResults: true
                    )

                    archiveArtifacts(
                        artifacts: 'backend/target/site/jacoco/**',
                        allowEmptyArchive: true
                    )
                }
            }
        }

        // STAGE 5: FRONTEND DEPENDENCY INSTALLATION
        stage('5. Frontend Dependencies') {
            steps {
                echo 'Installing and validating frontend dependencies'

                bat '''
                    "%DOCKER%" run --rm ^
                    -v "%WORKSPACE%:/workspace" ^
                    -w /workspace ^
                    %NODE_IMAGE% ^
                    sh -c "mkdir -p /tmp/frontend && cp -a /workspace/frontend/. /tmp/frontend/ && cd /tmp/frontend && npm ci"
                '''
            }
        }

        // STAGE 6: FRONTEND BUILD AND LINT
        stage('6. Frontend Build and Lint') {
            steps {
                echo 'Running frontend lint and production build'

                bat '''
                    "%DOCKER%" run --rm ^
                    -v "%WORKSPACE%:/workspace" ^
                    -w /workspace ^
                    %NODE_IMAGE% ^
                    sh -c "mkdir -p /tmp/frontend && cp -a /workspace/frontend/. /tmp/frontend/ && cd /tmp/frontend && npm ci && npm run lint && npm run build && mkdir -p /workspace/frontend/dist && cp -a dist/. /workspace/frontend/dist/"
                '''
            }

            post {
                success {
                    archiveArtifacts(
                        artifacts: 'frontend/dist/**',
                        allowEmptyArchive: true
                    )
                }
            }
        }

        // STAGE 7: SONARQUBE ANALYSIS
        stage('7. SonarQube Analysis') {
            steps {
                echo 'Running SonarQube code analysis'

                withCredentials([
                    string(
                        credentialsId: 'sonarqube-token',
                        variable: 'SONAR_TOKEN'
                    )
                ]) {
                    bat '''
                        "%DOCKER%" run --rm ^
                        -e SONAR_TOKEN ^
                        -v "%WORKSPACE%/backend:/workspace" ^
                        -w /workspace ^
                        %MAVEN_IMAGE% ^
                        sh -c "mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar -Dsonar.projectKey=Processhub -Dsonar.host.url=%SONAR_HOST_URL% -Dsonar.token=$SONAR_TOKEN"
                    '''
                }
            }
        }

        // STAGE 8: DOCKER IMAGE BUILD
        stage('8. Docker Image Build') {
            steps {
                echo 'Building ProcessHub Docker images'

                bat '''
                    "%DOCKER%" build ^
                    -t %BACKEND_IMAGE% ^
                    "%WORKSPACE%/backend"

                    if errorlevel 1 exit /b 1

                    "%DOCKER%" build ^
                    -t %FRONTEND_IMAGE% ^
                    "%WORKSPACE%/frontend"

                    if errorlevel 1 exit /b 1

                    echo Docker images:
                    "%DOCKER%" images
                '''
            }
        }

        // STAGE 9: DOCKER COMPOSE VALIDATION
        stage('9. Docker Compose Validation') {
            steps {
                echo 'Validating Docker Compose configuration'

                bat '''
                    "%DOCKER%" compose ^
                    -f "%WORKSPACE%/docker-compose.yml" ^
                    config --quiet

                    if errorlevel 1 exit /b 1

                    echo Docker Compose configuration is valid.
                '''
            }
        }

        // STAGE 10: PIPELINE REPORT
        stage('10. Pipeline Report') {
            steps {
                echo 'Generating ProcessHub pipeline report'

                bat '''
                    echo ==========================================
                    echo       PROCESSHUB CI PIPELINE REPORT
                    echo ==========================================
                    echo.
                    echo 1. Git Checkout
                    echo 2. Backend Validation
                    echo 3. Backend Compilation
                    echo 4. Backend Testing
                    echo 5. Frontend Dependencies
                    echo 6. Frontend Build and Lint
                    echo 7. SonarQube Analysis
                    echo 8. Docker Image Build
                    echo 9. Docker Compose Validation
                    echo 10. Pipeline Report
                    echo.
                    echo ==========================================
                    echo ALL PIPELINE STAGES COMPLETED
                    echo ==========================================
                '''
            }
        }
    }

    post {
        success {
            echo 'SUCCESS: All 10 ProcessHub pipeline stages passed.'
        }

        failure {
            echo 'FAILURE: A pipeline stage failed. Check its console output.'
        }

        always {
            echo 'ProcessHub Jenkins execution finished.'
        }
    }
}
