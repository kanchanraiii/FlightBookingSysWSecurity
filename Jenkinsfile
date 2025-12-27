pipeline {
    agent {
        docker {
            image 'maven:3.9.6-eclipse-temurin-17'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        booleanParam(name: 'SKIP_TESTS', defaultValue: true)
        booleanParam(name: 'AUTO_DEPLOY', defaultValue: true)
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Verify Docker') {
            steps {
                sh '''
                    docker --version
                    docker compose version
                '''
            }
        }

        stage('Package Services') {
            steps {
                script {
                    def mvnFlags = params.SKIP_TESTS ? '-DskipTests' : ''
                    def services = [
                        'ApiGateway',
                        'BookingService',
                        'ConfigServerFlightBooking',
                        'FlightBookingEurekaServer',
                        'FlightService'
                    ]
                    for (s in services) {
                        dir(s) {
                            sh "mvn -B ${mvnFlags} package"
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                sh 'docker compose build'
            }
        }

        stage('Deploy (Local)') {
            when {
                expression { params.AUTO_DEPLOY }
            }
            steps {
                sh 'docker compose up -d'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }
        failure {
            sh 'docker compose down || true'
        }
    }
}
