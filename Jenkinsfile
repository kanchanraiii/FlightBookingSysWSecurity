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
                    sh "mvn -B ${mvnFlags} package"
                }
            }
        }

        stage('Build Containers') {
            steps {
                sh '''
                    docker compose -f docker-compose.yml build
                '''
            }
        }

        stage('Local Deploy') {
            when {
                expression { return params.AUTO_DEPLOY }
            }
            steps {
                sh '''
                    docker compose -f docker-compose.yml up -d
                '''
            }
        }

        stage('Snapshot') {
            steps {
                sh '''
                    docker compose -f docker-compose.yml ps
                    docker compose -f docker-compose.yml logs --tail=50 || true
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '*/target/*.jar', allowEmptyArchive: true
        }
        failure {
            sh '''
                docker compose -f docker-compose.yml down || true
            '''
        }
    }
}
