resource "aws_internet_gateway" "sample-igw" {
    vpc_id = aws_vpc.sample-vpc10.id
    tags = {
      "Name" = "sample-igw"
    }
  
}