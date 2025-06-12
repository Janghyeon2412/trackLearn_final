resource "aws_subnet" "public_1a" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.10.0/24"  # ✅ 기존과 충돌 안 나는 범위
  availability_zone       = "ca-central-1a"
  map_public_ip_on_launch = true

  tags = {
    Name = "tracklearn-subnet-1a"
  }
}

resource "aws_subnet" "public_1b" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.20.0/24"  # ✅ 서로 다른 대역
  availability_zone       = "ca-central-1b"
  map_public_ip_on_launch = true

  tags = {
    Name = "tracklearn-subnet-1b"
  }
}
