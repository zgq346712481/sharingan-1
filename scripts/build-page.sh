#!/usr/bin/env bash

# 编译页面

DIR=$(cd `dirname $0`;pwd)
cd $DIR/../sharingan-web/src/main/page/
npm run build