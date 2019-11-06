#!/bin/bash

set -e # exit with nonzero exit code if anything fails

if [[ $TRAVIS_BRANCH == "master" && $TRAVIS_PULL_REQUEST == "false" ]]; then

echo "Starting to update gh-pages\n"

#copy data we're interested in to other place
cp -R doc $HOME/doc

#go to home and setup git
cd $HOME
git config --global user.email "sungwook0115.kim@gmail.com"
git config --global user.name "RoySRose"

#using token clone gh-pages branch
git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/naver/pinpoint.git gh-pages > /dev/null

#go into directory and copy data we're interested in to that directory
cd gh-pages
cp -Rf $HOME/doc/*.md ./pages
cp -Rf $HOME/doc/images/* ./images

#add, commit and push files
git add -f .
git commit -m "Auto commit by TRAVIS $TRAVIS_BUILD_NUMBER"

git push -fq origin gh-pages > /dev/null

echo "Done updating gh-pages\n"

else
 echo "Skipped updating gh-pages, because build is not triggered from the master branch."
fi;