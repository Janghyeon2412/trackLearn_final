#!/bin/bash


sudo apt update
sudo apt install -y openjdk-17-jdk

# Jenkins 설치
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null
echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/ | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null
sudo apt update
sudo apt install -y jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins

sudo curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
sudo apt-get install unzip
sudo unzip awscliv2.zip
sudo ./aws/install


# Git 설치
sudo apt install -y git

#도커설치 오래된 명령어 (apt-get)	Ubuntu 16.04 이후 새롭게 도입 (apt) /도커는 공식 설치 가이드에apt-get 사용을 권장
sudo apt-get update
sudo apt-get install -y ca-certificates curl
# Docker GPG 키 추가
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
# Docker 저장소 추가
echo \
"deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
$(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
# Docker 설치
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io
# Jenkins를 Docker 그룹에 추가
sudo usermod -aG docker ubuntu
sudo usermod -aG docker jenkins
# Docker 서비스 재시작
sudo service docker restart
# Jenkins 서비스 재시작
sudo service jenkins restart

# kubectl 설치
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

{
  echo "==== Jenkins 상태 ===="
  sudo systemctl status jenkins
  echo
  echo "==== 버전 확인 ===="
  java -version
  jenkins --version
  aws --version
  docker --version
  git --version
  kubectl version --client
} > /var/log/install_summary.log 2>&1

#cat /var/log/install_summary.log
# userdata 는 꼭 terraform destroy 후 apply 초기화