resource "aws_eip" "jenkins_eip" {  #Elastic IP (EIP) 할당
  domain = "vpc"  # 변경된 부분
  tags = {
    "Name" = "jenkins_eip"
  }
  instance = aws_instance.jenkins_server.id
}

resource "aws_instance" "jenkins_server" {
  ami = "ami-048d2b60a58148709" # 22 Ubuntu AMI 사용
  instance_type = "t3.medium"  #"t3.medium"   # Jenkins 실행에 적절한 크기
  subnet_id = aws_subnet.sample-subnet-public0110.id  # 퍼블릭 서브넷 사용
  key_name = "web"  # SSH 키페어 입력
  vpc_security_group_ids = [aws_security_group.sample-sg-jenkins.id]  # Jenkins 보안 그룹 적용

  root_block_device {
    volume_size = 30   # Docker 빌드 및 Jenkins 데이터 저장을 고려하여 30GB 설정
    volume_type = "gp3" # 성능 및 비용 최적화
    tags = {
      "Name" = "jenkins-server-root"
    }
  }

  tags = {
    "Name" = "Jenkins-Server"
  }
  user_data = file("jenkins-install.sh")
}
  # 사용자 데이터 스크립트 들여쓰기를 허용하려면 반드시 <<-EOF 를 사용
#   user_data = <<-EOF
#   #!/bin/bash
#
#   set -ex  # 디버깅 모드 활성화 (오류 발생 시 즉시 중지) 로그는남김
#   sudo apt update
#   sudo apt install -y openjdk-17-jdk
#
#   # Jenkins 설치
#   curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null
#   echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/ | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null
#   sudo apt install -y jenkins
#   sudo systemctl start jenkins
#   sudo systemctl enable jenkins
#
#   sudo curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
#   sudo apt-get install unzip
#   sudo unzip awscliv2.zip
#   sudo ./aws/install
#
#
#   # Git 설치
#   sudo apt install -y git
#
#   #도커설치
#   sudo apt-get update -y
#   sudo apt-get install -y ca-certificates curl
#   # Docker GPG 키 추가
#   sudo install -m 0755 -d /etc/apt/keyrings
#   sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
#   sudo chmod a+r /etc/apt/keyrings/docker.asc
#   # Docker 저장소 추가
#   echo \
#   "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
#   $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
#   sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
#   # Docker 설치
#   sudo apt-get update
#   sudo apt-get install -y docker-ce docker-ce-cli containerd.io
#   # Jenkins를 Docker 그룹에 추가
#   sudo usermod -aG docker $USER
#   sudo usermod -aG docker jenkins
#   # Docker 서비스 재시작
#   sudo service docker restart
#   # Jenkins 서비스 재시작
#   sudo service jenkins restart
#
#   #깃설치(-y 제거 하면 설치과정에 질문에 y 해주면됨)
#   sudo apt install git –y
#
#
#   # kubectl 설치
#   curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
#   sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
#
#   EOF
#
# }