variable "aws_region" {
  description = "AWS region (ex: us-east-1)"
  type        = string
}

variable "vpc_cidr" {
  description = "Bloco CIDR para a VPC (ex: 10.0.0.0/16)"
  type        = string
  default     = "10.0.0.0/16"
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
  description = "Imagem Docker (ex: 892387274401.dkr.ecr.us-east-1.amazonaws.com/todo-api:latest)"
  type        = string
}
