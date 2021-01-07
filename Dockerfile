FROM golang:1.14-buster

WORKDIR /home/app

COPY go.* ./

RUN go mod download
