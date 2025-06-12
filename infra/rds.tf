resource "aws_db_subnet_group" "tracklearn_subnet_group" {
  name       = "tracklearn-subnet-group"
  subnet_ids = [
    aws_subnet.public_1a.id,
    aws_subnet.public_1b.id
  ]

  tags = {
    Name = "tracklearn-subnet-group"
  }
}

resource "aws_db_instance" "tracklearn_rds" {
  identifier             = "tracklearn-rds"
  engine                 = "mysql"
  engine_version         = "8.0"
  instance_class         = "db.t3.micro"
  username               = "root"
  password               = "tiger1234"
  allocated_storage      = 20
  skip_final_snapshot    = true
  publicly_accessible    = true
  db_subnet_group_name   = aws_db_subnet_group.tracklearn_subnet_group.name
  vpc_security_group_ids = [aws_security_group.ec2_sg.id]

  tags = {
    Name = "tracklearn-rds"
  }
}