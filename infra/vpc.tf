resource "aws_vpc" "sample-vpc10" {
    cidr_block = "10.0.0.0/16"
    enable_dns_hostnames = true
    enable_dns_support = true
    tags = {
      "Name" = "sample-vpc10"
    }
}