pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: 'Skip Maven tests during packaging'
        )
        booleanParam(
            name: 'AUTO_DEPLOY',
            defaultValue: true,
            description: 'Run docker compose up -d after building'
        )
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
                    env.COMPOSE_BIN = sh(
                        script: '''
                            if docker compose version >/dev/null 2>&1; then
                                echo docker compose
                            elif docker-compose version >/dev/null 2>&1; then
                                echo docker-compose
                            else
                                echo "Missing docker compose binary"
                                exit 1
                            fi
                        ''',
                        returnStdout: true
                    ).trim()
                }
            }
        }

        stage('Package Services') {
            steps {
                script {
                    def mvnFlags = params.SKIP_TESTS ? '-DskipTests' : ''
                    sh """
                        mvn -B ${mvnFlags} package
                    """
                }
            }
        }

        stage('Build Containers') {
            steps {
                sh """
                    ${env.COMPOSE_BIN} -f docker-compose.yml build
                """
            }
        }

        stage('Local Deploy') {
            when {
                expression { return params.AUTO_DEPLOY }
            }
            steps {
                sh """
                    ${env.COMPOSE_BIN} -f docker-compose.yml up -d
                """
            }
        }

        stage('Snapshot') {
            steps {
                sh """
                    ${env.COMPOSE_BIN} -f docker-compose.yml ps
                """
                sh(
                    script: "${env.COMPOSE_BIN} -f docker-compose.yml logs --tail=50",
                    returnStatus: true
                )
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '*/target/*.jar', allowEmptyArchive: true
        }
        failure {
            sh(
                script: "${env.COMPOSE_BIN} -f docker-compose.yml down",
                returnStatus: true
            )
        }
    }
}
