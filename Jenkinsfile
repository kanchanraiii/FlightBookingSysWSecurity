pipeline {
    agent any

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

                    for (service in services) {
                        echo "Packaging ${service}"
                        dir(service) {
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

        stage('Deploy') {
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
