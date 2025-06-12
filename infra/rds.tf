# 1. RDS Subnet Group - 프라이빗 서브넷들로 구성
resource "aws_db_subnet_group" "sample_rds_subnet_group" {
  name       = "sample-rds-subnet-group"
  subnet_ids = [
    aws_subnet.sample-subnet-private0110.id,
    aws_subnet.sample-subnet-private0210.id
  ]
  tags = {
    Name = "sample-rds-subnet-group"
  }
}


# 3. RDS 인스턴스 생성 - 프라이빗 (프리티어: db.t4g.micro)
resource "aws_db_instance" "sample_rds" {
  identifier             = "sample-rds"
  engine                 = "mysql"
  engine_version         = "8.0"
  instance_class         = "db.t4g.micro"  # 프리티어 지원
  username               = "root"
  password               = "mysql1234"
  db_name                = "scott"
  allocated_storage      = 20
  skip_final_snapshot    = true
  publicly_accessible    = false

  db_subnet_group_name   = aws_db_subnet_group.sample_rds_subnet_group.name
  vpc_security_group_ids = [aws_security_group.sample-sg-db.id]

  tags = {
    Name = "sample-rds"
  }
}
