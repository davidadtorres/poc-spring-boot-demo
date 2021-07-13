#!/bin/bash
#
# commit.sh
#
# Syntax:
#   commit.sh 'commit_message'
#
if [ $# -gt 2 ] || [ $# -lt 1 ]
then
  echo -e "SYNTAX ERROR: commit message expected!\n\ncommit.sh 'commit_message'\n"
  exit -1
fi

APP_CONF_FILE="src/main/resources/application.properties" # Add the "BUILD=1" line before run this script first time!!!
BUILD_TAG=build

build=$(grep -i "BUILD=" $APP_CONF_FILE | cut -d= -f2)
((build += 1))
sed -i "s/BUILD=[0-9]\+/$BUILD_TAG=$build/gi" $APP_CONF_FILE

git add .
git commit -m "$1"

if [ $? != '0' ]
then
  ((build -= 1))
  sed -i "s/BUILD=[0-9]\+/$BUILD_TAG=$build/gi" $APP_CONF_FILE
fi

git push -u origin-git master

git log -n 4 --oneline