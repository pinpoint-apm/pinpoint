#!/bin/bash

set -e # exit with nonzero exit code if anything fails

echo "Starting to update gh-pages"

#copy data we're interested in to other place
mkdir temp

cp -R doc temp/doc

#go to home and setup git
git config --global user.email "sungwook0115.kim@gmail.com"
git config --global user.name "RoySRose"

#using token clone gh-pages branch
git clone --quiet --branch=gh-pages https://.:${GITHUB_TOKEN}@github.com/pinpoint-apm/pinpoint.git gh-pages > /dev/null
#go into directory and copy data we're interested in to that directory
cd gh-pages
cp -Rf ../temp/doc/*.md ./pages
cp -Rf ../temp/doc/images/* ./images

#add, commit and push files
git add -f .
git diff-index --quiet HEAD || git commit -m "Auto commit with Github Action"
echo "add and commited gh-pages"

git push -fq origin gh-pages > /dev/null

echo "Done updating gh-pages"
