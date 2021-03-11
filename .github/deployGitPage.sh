#!/bin/bash

set -e # exit with nonzero exit code if anything fails

echo "Starting to update gh-pages\n"

#copy data we're interested in to other place
mkdir gh
mkdir gh/temp

cp -R doc gh/temp/doc
cd gh

#go to home and setup git
git config --global user.email "sungwook0115.kim@gmail.com"
git config --global user.name "RoySRose"

#using token clone gh-pages branch
#git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/pinpoint-apm/pinpoint.git gh-pages > /dev/null
it clone --quiet --branch=gh-pages https://.:${{ secrets.GITHUB_TOKEN }}@github.com/pinpoint-apm/pinpoint.git gh-pages > /dev/null
#go into directory and copy data we're interested in to that directory
cd gh-pages
cp -Rf temp/doc/*.md ./pages
cp -Rf temp/doc/images/* ./images

#add, commit and push files
git add -f .
git commit -m "Auto commit with Github Action"

git push -fq origin gh-pages > /dev/null

echo "Done updating gh-pages\n"
