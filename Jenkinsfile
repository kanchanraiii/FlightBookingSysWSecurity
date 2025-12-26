pipeline {
    agent any
    options {
        timestamps()
        disableConcurrentBuilds()
    }
    parameters {
        booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Skip Maven tests during packaging')
        booleanParam(name: 'AUTO_DEPLOY', defaultValue: true, description: 'Run docker compose up -d after building')
    }
    environment {
        MAVEN_OPTS = '-Dmaven.test.failure.ignore=false'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Detect Compose CLI') {
            steps {
                script {
                    env.COMPOSE_BIN = bat(
                        script: """
                        @echo off
                        docker compose version >NUL 2>&1
                        if %ERRORLEVEL%==0 (
                            echo docker compose
                            exit /b 0
                        )
                        docker-compose version >NUL 2>&1
                        if %ERRORLEVEL%==0 (
                            echo docker-compose
                            exit /b 0
                        )
                        echo Missing docker compose binary
                        exit /b 1
                        """,
                        returnStdout: true
                    ).trim()
                }
            }
        }
        stage('Package Services') {
            steps {
                script {
                    def mvnFlags = params.SKIP_TESTS ? '-DskipTests' : ''
                    bat "mvn -B ${mvnFlags} package"
                }
            }
        }
        stage('Build Containers') {
            steps {
                bat "%COMPOSE_BIN% -f docker-compose.yml build"
            }
        }
        stage('Local Deploy') {
            when {
                expression { return params.AUTO_DEPLOY }
            }
            steps {
                bat "%COMPOSE_BIN% -f docker-compose.yml up -d"
            }
        }
        stage('Snapshot') {
            steps {
                bat "%COMPOSE_BIN% -f docker-compose.yml ps"
                bat returnStatus: true, script: "%COMPOSE_BIN% -f docker-compose.yml logs --tail=50"
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: '*/target/*.jar', allowEmptyArchive: true
        }
        failure {
            bat returnStatus: true, script: "%COMPOSE_BIN% -f docker-compose.yml down"
        }
    }
}
