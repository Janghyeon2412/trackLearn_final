##public rt

resource "aws_route_table" "sample-rt-public" {
    vpc_id = aws_vpc.sample-vpc10.id
    tags = {
      "Name" = "sample-rt-public"
    }
  
}

###private rt
resource "aws_route_table" "sample-rt-private01" {
    vpc_id = aws_vpc.sample-vpc10.id
    tags = {
      "Name" = "sample-rt-private01"
    }
}

resource "aws_route_table" "sample-rt-private02" {
    vpc_id = aws_vpc.sample-vpc10.id
    tags = {
      "Name" = "sample-rt-private02"
    }
  
}
