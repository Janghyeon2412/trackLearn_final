resource "aws_ecr_repository" "docker_repo" {
  name                 = "docker_repo"  # ECR 리포지토리 이름
  force_delete = true # 리포지토리 삭제 시 모든 이미지를 함께 삭제

  tags = {
    Name        = "docker_repo"
    Environment = "Production"
  }
}


# terraform apply -target="aws_ecr_repository.docker_repo" -auto-approve

#  -target="aws_ecr_repository.docker_repo" → 특정 리소스(ECR)만 추가
#  기존 리소스(EC2, VPC 등) 변경 없이 docker-repo만 생성됨

#  ECR 생성 확인

# aws ecr describe-repositories --query "repositories[*].repositoryUri"


