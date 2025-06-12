
resource "aws_eip" "bastion_ip" {  #Elastic IP (EIP) 할당
  domain = "vpc"  # 변경된 부분
  tags = {
    "Name" = "bastion_eip"
  }
  instance = aws_instance.sample-ec2-bastion10.id
}
#이렇게 하면 매번 ami 값을 직접 입력하지 않아도 최신 AMI를 자동으로 사용함. 22->24 로 올라감
# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/instance
# module "ubuntu_22_04_latest" {
#   source = "github.com/andreswebs/terraform-aws-ami-ubuntu"
#   release = "22.04"
# }
# 22버전 사용
#https://cloud-images.ubuntu.com/locator/ec2/ 는 우분투 공식 사이트 ap-northeast-2으로 검색하여사용,  t2,t3 이므로 amd64 CPU 아키텍처선택

locals {
  ami_id = "ami-05a7f3469a7653972" # 수정됨
}
 
resource "aws_instance" "sample-ec2-bastion10" {
  ami             = "ami-048d2b60a58148709"  # local.ami_id 사용  #  ami
  instance_type   = "t2.micro"
  vpc_security_group_ids = [aws_security_group.sample-sg-bastion.id, aws_default_security_group.default.id]
  subnet_id       = aws_subnet.sample-subnet-public0110.id
  key_name        = "web"  # 본인 키페어 이름 입력

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
    tags = {
      "Name" = "sample-ec2-bastion-root"
    }
  }

  tags = {
    "Name" = "ec2-bastion"
  }
}