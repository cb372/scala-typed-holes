#!/usr/bin/env bash
#
# Travis uses this script to commit the updated README.md and push it to GitHub.

if [ "$(git describe --exact-match --tags master)" == "$TRAVIS_TAG" ]; then
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "Travis CI"

  git checkout master

  git add README.md
  git commit -m '[skip ci] Update version number in README'

  git remote add origin-with-token https://${GITHUB_TOKEN}@github.com/cb372/scala-typed-holes.git > /dev/null 2>&1

  git push --quiet --set-upstream origin-with-token master
else
  echo "A tag was pushed but it was not pointing at the HEAD of master, so I won't push the updated readme to GitHub."
fi
