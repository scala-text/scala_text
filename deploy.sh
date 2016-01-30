#!/bin/bash

echo -e "Host github.com\n\tStrictHostKeyChecking no\nIdentityFile ~/.ssh/deploy.key\n" >> ~/.ssh/config &&
openssl aes-256-cbc -k "$SERVER_KEY" -in travis_deploy_key.enc -d -a -out deploy.key &&
cp deploy.key ~/.ssh/ &&
chmod 600 ~/.ssh/deploy.key &&
git config --global user.email "6b656e6a69@gmail.com" &&
git config --global user.name "xuwei-k" &&
mv gitbook/_book ../ &&
mv gitbook/book.epub ../_book/scala_text.epub &&
git fetch origin gh-pages:gh-pages &&
git clean -fdx &&
git checkout gh-pages &&
rm -rf ./* &&
echo -e "*class\ntarget" > .gitignore &&
cp -r ../_book/* ./ &&
git add . &&
git commit -a -m "auto commit on travis $TRAVIS_JOB_NUMBER $TRAVIS_COMMIT" &&
if [[ "${TRAVIS_BRANCH}" == "master" && "${TRAVIS_PULL_REQUEST}" == "false" ]];
then git push git@github.com:dwango/scala_text.git gh-pages:gh-pages ; fi
