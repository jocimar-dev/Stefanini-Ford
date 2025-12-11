output "alb_dns_name" {
  value       = aws_lb.this.dns_name
  description = "Endpoint HTTP do ALB"
}

output "rds_endpoint" {
  value       = aws_db_instance.sqlserver.address
  description = "Endpoint do SQL Server"
}
