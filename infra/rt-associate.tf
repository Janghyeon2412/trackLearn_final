resource "aws_route_table_association" "rt_associate-public01" {
    subnet_id = aws_subnet.sample-subnet-public0110.id
    route_table_id = aws_route_table.sample-rt-public.id 
}

resource "aws_route_table_association" "rt_associate-public02" {
    subnet_id = aws_subnet.sample-subnet-public0210.id
    route_table_id = aws_route_table.sample-rt-public.id
}

resource "aws_route_table_association" "rt_associate-private01" {
    subnet_id = aws_subnet.sample-subnet-private0110.id
    route_table_id = aws_route_table.sample-rt-private01.id
}

resource "aws_route_table_association" "rt_associate-private02" {
    subnet_id = aws_subnet.sample-subnet-private0210.id
    route_table_id = aws_route_table.sample-rt-private02.id
}