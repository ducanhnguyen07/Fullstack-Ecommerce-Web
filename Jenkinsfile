pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: docker
    image: docker:dind
    securityContext:
      privileged: true
    env:
    - name: DOCKER_TLS_CERTDIR
      value: ""
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run
  - name: git
    image: alpine/git
    command:
    - cat
    tty: true
  - name: maven
    image: maven:3.8.3-openjdk-17
    command:
    - cat
    tty: true
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run
  - name: node
    image: node:18-alpine
    command:
    - cat
    tty: true
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run
  volumes:
  - name: docker-sock
    emptyDir: {}
"""
        }
    }
    
    environment {
        DOCKER_HUB_FRONTEND_REPO = 'anhnd2301/ecommerce-frontend'
        DOCKER_HUB_BACKEND_REPO = 'anhnd2301/ecommerce-backend'
        CONFIG_REPO_URL = 'https://github.com/ducanhnguyen07/ecommerce-k8s.git'
        CONFIG_REPO_BRANCH = 'main'
    }
    
    stages {
        stage('Checkout') {
            steps {
                container('git') {
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
        }
        
        stage('Build Docker Images') {
            parallel {
                stage('Build Frontend Image') {
                    steps {
                        container('docker') {
                            script {
                                dir('03-frontend_angular-ecommerce') {
                                    sh """
                                        docker build -t ${DOCKER_HUB_FRONTEND_REPO}:${env.TAG_NAME} .
                                        docker tag ${DOCKER_HUB_FRONTEND_REPO}:${env.TAG_NAME} ${DOCKER_HUB_FRONTEND_REPO}:latest
                                    """
                                }
                            }
                        }
                    }
                }
                stage('Build Backend Image') {
                    steps {
                        container('docker') {
                            script {
                                dir('02-backend_spring-boot-rest-api') {
                                    sh """
                                        docker build -t ${DOCKER_HUB_BACKEND_REPO}:${env.TAG_NAME} .
                                        docker tag ${DOCKER_HUB_BACKEND_REPO}:${env.TAG_NAME} ${DOCKER_HUB_BACKEND_REPO}:latest
                                    """
                                }
                            }
                        }
                    }
                }
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                container('docker') {
                    script {
                        withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                            sh """
                                echo \$DOCKER_PASS | docker login --username \$DOCKER_USER --password-stdin
                                
                                # Push frontend images
                                docker push ${DOCKER_HUB_FRONTEND_REPO}:${env.TAG_NAME}
                                docker push ${DOCKER_HUB_FRONTEND_REPO}:latest
                                
                                # Push backend images
                                docker push ${DOCKER_HUB_BACKEND_REPO}:${env.TAG_NAME}
                                docker push ${DOCKER_HUB_BACKEND_REPO}:latest
                            """
                        }
                    }
                }
            }
        }
        
        stage('Update Config Repo') {
            steps {
                container('git') {
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
    }
    
    post {
        always {
            echo "Build completed!"
        }
        success {
            echo "Pipeline completed successfully!"
        }
        failure {
            echo "Pipeline failed!"
        }
    }
}