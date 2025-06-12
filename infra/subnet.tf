
### public subnet ####
resource "aws_subnet" "sample-subnet-public0110" {
    vpc_id = aws_vpc.sample-vpc10.id
    cidr_block = "10.0.0.0/20"
    availability_zone = "ca-central-1a"
    map_public_ip_on_launch = "true"
    tags = {
      "Name" = "sample-subnet-public0110"
    } 
}

resource "aws_subnet" "sample-subnet-public0210" {
    vpc_id = aws_vpc.sample-vpc10.id
    cidr_block = "10.0.16.0/20"
    availability_zone = "ca-central-1a"
    map_public_ip_on_launch = "true"
    tags = {
      "Name" = "sample-subnet-public0210"
    } 
}

## pirvate subnet
resource "aws_subnet" "sample-subnet-private0110" {
    vpc_id = aws_vpc.sample-vpc10.id
    cidr_block = "10.0.64.0/20"
    availability_zone = "ca-central-1a"
    tags = {
      "Name" = "sample-subnet-private0110"
    } 
}

resource "aws_subnet" "sample-subnet-private0210" {
    vpc_id = aws_vpc.sample-vpc10.id
    cidr_block = "10.0.80.0/20"
    availability_zone = "ca-central-1b"
    tags = {
      "Name" = "sample-subnet-private0210"
    } 
}
