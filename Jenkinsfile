pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: true,
            description: 'Skip Maven tests'
        )
        booleanParam(
            name: 'AUTO_DEPLOY',
            defaultValue: true,
            description: 'Run docker compose up -d after build'
        )
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
                sh '''
                    docker compose build
                '''
            }
        }

        stage('Deploy (Local)') {
            when {
                expression { return params.AUTO_DEPLOY }
            }
            steps {
                sh '''
                    docker compose up -d
                '''
            }
        }

        stage('Snapshot') {
            steps {
                sh '''
                    docker compose ps
                    docker compose logs --tail=50 || true
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }
        failure {
            sh '''
                docker compose down || true
            '''
        }
        success {
            echo 'Flight Booking system built and deployed successfully'
        }
    }
}
