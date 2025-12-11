variable "aws_region" {
  description = "AWS region (ex: us-east-1)"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID onde o ECS/RDS vão rodar"
  type        = string
}

variable "public_subnet_ids" {
  description = "Subnets públicas para o ALB"
  type        = list(string)
}

variable "private_subnet_ids" {
  description = "Subnets privadas para ECS e RDS"
  type        = list(string)
}

variable "db_username" {
  description = "Usuário do banco"
  type        = string
  default     = "sa"
}

variable "db_password" {
  description = "Senha do banco (forte)"
  type        = string
  sensitive   = true
}

variable "image" {
  description = "Imagem Docker (ex: ghcr.io/<owner>/todo-api:latest)"
  type        = string
}
