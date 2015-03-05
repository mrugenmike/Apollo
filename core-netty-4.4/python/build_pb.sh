#!/bin/bash
#
# creates the python classes for our .proto
#

project_base="/Users/mrugen/gitrepos/core-netty-4.4/python"

rm ${project_base}/src/comm_pb2.py

protoc -I=${project_base}/resources --python_out=./src ../resources/comm.proto 
