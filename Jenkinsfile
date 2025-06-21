pipeline {
    agent any
    
    environment {
        DOCKER_HUB_FRONTEND_REPO = 'anhnd2301/ecommerce-frontend'
        DOCKER_HUB_BACKEND_REPO = 'anhnd2301/ecommerce-backend'
        CONFIG_REPO_URL = 'https://github.com/ducanhnguyen07/ecommerce-k8s.git'
        CONFIG_REPO_BRANCH = 'master'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    // Lấy tag name từ git
                    env.TAG_NAME = sh(returnStdout: true, script: 'git describe --tags --exact-match HEAD 2>/dev/null || echo "latest"').trim()
                    if (env.TAG_NAME == "latest") {
                        error("This build must be triggered by a tag push")
                    }
                    echo "Building with tag: ${env.TAG_NAME}"
                }
            }
        }
        
        stage('Build Docker Images') {
            parallel {
                stage('Build Frontend Image') {
                    steps {
                        script {
                            dir('03-frontend_angular-ecommerce') {
                                def frontendImage = docker.build("${DOCKER_HUB_FRONTEND_REPO}:${env.TAG_NAME}")
                            }
                        }
                    }
                }
                stage('Build Backend Image') {
                    steps {
                        script {
                            dir('02-backend_spring-boot-rest-api') {
                                def backendImage = docker.build("${DOCKER_HUB_BACKEND_REPO}:${env.TAG_NAME}")
                            }
                        }
                    }
                }
            }
        }
        
        stage('Push to Docker Hub') {
            parallel {
                stage('Push Frontend Image') {
                    steps {
                        script {
                            docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                                def frontendImage = docker.image("${DOCKER_HUB_FRONTEND_REPO}:${env.TAG_NAME}")
                                frontendImage.push()
                                frontendImage.push("latest")
                            }
                        }
                    }
                }
                stage('Push Backend Image') {
                    steps {
                        script {
                            docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                                def backendImage = docker.image("${DOCKER_HUB_BACKEND_REPO}:${env.TAG_NAME}")
                                backendImage.push()
                                backendImage.push("latest")
                            }
                        }
                    }
                }
            }
        }
        
        stage('Update Config Repo') {
            steps {
                script {
                    // Clone config repo
                    sh '''
                        rm -rf config-repo
                        git clone ${CONFIG_REPO_URL} config-repo
                        cd config-repo
                        git checkout ${CONFIG_REPO_BRANCH}
                    '''
                    
                    // Update values.yaml file
                    sh """
                        cd config-repo/ecommerce-chart
                        
                        # Update both frontend and backend tags in values.yaml
                        sed -i 's/tag: .*/tag: ${env.TAG_NAME}/g' values.yaml
                        
                        # Show changes
                        echo "=== Changes made to values.yaml ==="
                        git diff values.yaml
                        
                        # Check if there are changes
                        if git diff --quiet; then
                            echo "No changes to commit"
                        else
                            # Commit and push changes
                            git config user.email "jenkins@company.com"
                            git config user.name "Jenkins CI"
                            git add values.yaml
                            git commit -m "Update frontend and backend image tags to ${env.TAG_NAME}"
                            git push origin ${CONFIG_REPO_BRANCH}
                        fi
                    """
                }
            }
        }
    }
    
    post {
        always {
            // Clean up workspace only
            cleanWs()
        }
        success {
            echo "Pipeline completed successfully!"
        }
        failure {
            echo "Pipeline failed!"
        }
    }
}