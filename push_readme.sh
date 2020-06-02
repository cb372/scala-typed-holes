#!/usr/bin/env bash
#
# Travis uses this script to commit the updated README.md and push it to GitHub.

git fetch origin master

if [ "$(git describe --exact-match --tags FETCH_HEAD)" == "$TRAVIS_TAG" ]; then
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "Travis CI"

  # Re-clone the repo because Travis clones it with the --branch option,
  # which means we are on a detached head
  git clone --depth=50 https://github.com/cb372/scala-typed-holes.git fresh-clone

  cd fresh-clone
  cp ../README.md .

  git add README.md
  git commit -m '[skip ci] Update version number in README'

  git remote set-url origin https://${GITHUB_TOKEN}@github.com/cb372/scala-typed-holes.git > /dev/null 2>&1

  git push -u origin master
else
  echo "A tag was pushed but it was not pointing at the HEAD of master, so I won't push the updated readme to GitHub."
fi
