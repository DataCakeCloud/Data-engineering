#!/usr/bin/env bash

#获取相应目录
#PATH=$(cd $(dirname $0); pwd)
FILE_PATH=/data/flink
#FILE_PATH="/Users/wuyan/shareit/flink-1.11.0/flink-1.11.0"
#echo "执行文件目录为: ${FILE_PATH}"
cd ${FILE_PATH}
COMMAND=$1

#-----------------------test all----------------------------------------
#echo "before decode: ${COMMAND}"
#解密
command=$(printf "%s" ${COMMAND}| base64 -d)
#echo "after decode: $command"
eval $command

exit 0