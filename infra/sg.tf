resource "aws_security_group" "sample-sg-bastion" {
    name = "sample-sg-bastion"
    description = "for bastion Server"
    vpc_id = aws_vpc.sample-vpc10.id
    tags = {
      "Name" = "sample-sg-bastion"
    }
}

resource "aws_security_group_rule" "sample_sg-bastion-ingress" {
    security_group_id = aws_security_group.sample-sg-bastion.id
    type = "ingress"
    description = "allow all for ssh"
    from_port = 22
    to_port = 22
    protocol = "tcp"
    cidr_blocks = [ "0.0.0.0/0" ]
  
}



resource "aws_security_group_rule" "sample-sg-bastion-egress" {
    security_group_id = aws_security_group.sample-sg-bastion.id
    type = "egress"
    description = "allow all for all outbound"
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [ "0.0.0.0/0" ]
  
}


#### elb sg
resource "aws_security_group" "sample-sg-elb" {
    name = "sample-sg-elb"
    description = "for load balancer"
    vpc_id = aws_vpc.sample-vpc10.id
    tags = {
      "Name" = "sample-sg-elb"
    }  
}

resource "aws_security_group_rule" "sample-sg-elb-ingress" {
    security_group_id = aws_security_group.sample-sg-elb.id
    type = "ingress"
    description = "allow all http"
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = [ "0.0.0.0/0" ]
}

resource "aws_security_group_rule" "sample-sg-elb-egress" {
    security_group_id = aws_security_group.sample-sg-elb.id
    type = "egress"
    description = "allow all"
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [ "0.0.0.0/0" ]
  
}


resource "aws_security_group" "sample-sg-jenkins" {
  name        = "sample-sg-jenkins"
  description = "Security Group for Jenkins Server"
  vpc_id      = aws_vpc.sample-vpc10.id

  # Jenkins UI 접근 (8080 포트)
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Jenkins 노드 에이전트 (50000 포트)
  ingress {
    from_port   = 50000
    to_port     = 50000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # EC2 SSH
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTP (80 포트)
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTPS (443 포트)
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # SpringBoot 앱 포트 (8090)
  ingress {
    from_port   = 8090
    to_port     = 8090
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # 모든 아웃바운드 허용
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "sample-sg-jenkins"
  }
}

resource "aws_security_group" "sample-sg-db" {
  name        = "sample-sg-db"
  description = "Security Group for DB Server"
  vpc_id      = aws_vpc.sample-vpc10.id

  # SSH 접근 허용
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # MySQL (3306 포트)
  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # 아웃바운드 전체 허용
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "sample-sg-db"
  }
}


# Bastion -> EKS API 서버(443번 포트)로 나가는 트래픽 허용 (egress)
resource "aws_security_group_rule" "sample-sg-bastion-eks-egress" {
  security_group_id = aws_security_group.sample-sg-bastion.id
  type = "egress"
  description = "Allow Bastion to communicate with EKS API Server"
  from_port = 443
  to_port = 443
  protocol = "tcp"
  cidr_blocks = [ "0.0.0.0/0" ] # EKS API 서버는 AWS에서 관리하는 엔드포인트
}

# EKS API 서버에서 Bastion의 요청을 받을 수 있도록 ingress 규칙 추가
resource "aws_security_group_rule" "sample-sg-eks-ingress" {
  security_group_id = aws_security_group.sample-sg-bastion.id
  type = "ingress"
  description = "Allow EKS API Server to accept traffic from Bastion"
  from_port = 443
  to_port = 443
  protocol = "tcp"
  source_security_group_id = aws_security_group.sample-sg-bastion.id
}