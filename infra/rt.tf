resource "aws_route_table" "sample-rt-public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }

  tags = {
    Name = "sample-rt-public"
  }
}

resource "aws_route_table" "sample-rt-private01" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "sample-rt-private01"
  }
}

resource "aws_route_table" "sample-rt-private02" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "sample-rt-private02"
  }
}
