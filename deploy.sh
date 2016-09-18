#!/bin/bash
set -e

echo -e "Host github.com\n\tStrictHostKeyChecking no\nIdentityFile ~/.ssh/deploy.key\n" >> ~/.ssh/config &&
openssl aes-256-cbc -k "$SERVER_KEY" -in travis_deploy_key.enc -d -a -out deploy.key &&
cp deploy.key ~/.ssh/ &&
chmod 600 ~/.ssh/deploy.key &&
git config --global user.email "6b656e6a69@gmail.com" &&
git config --global user.name "xuwei-k" &&
mv gitbook/_book ../ &&
mv gitbook/scala_text.epub ../_book/scala_text.epub &&
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

git checkout -qf $TRAVIS_COMMIT
openssl aes-256-cbc -k "$PREVIEW_KEY" -in preview_key.enc -d -a -out preview.key
cp preview.key ~/.ssh/
chmod 600 ~/.ssh/preview.key
echo -e "Host github.com\n\tStrictHostKeyChecking no\nIdentityFile ~/.ssh/preview.key\n" > ~/.ssh/config
git clone git@github.com:dwango/scala_text_previews.git
cd scala_text_previews
rm -rf ./${TRAVIS_BRANCH}
mkdir ${TRAVIS_BRANCH}
cp -r ../../_book/* ./${TRAVIS_BRANCH}/
git add .
git commit -a -m "auto commit on travis $TRAVIS_JOB_NUMBER $TRAVIS_COMMIT $TRAVIS_BRANCH"
git push origin gh-pages:gh-pages
cd ..

if [[ "${TRAVIS_BRANCH}" == "master" && "${TRAVIS_PULL_REQUEST}" == "false" ]]; then
  git checkout -qf $TRAVIS_COMMIT
  openssl aes-256-cbc -k "$PDF_KEY" -in travis_deploy_pdf_key.enc -d -a -out pdf.key
  cp pdf.key ~/.ssh/
  chmod 600 ~/.ssh/pdf.key
  echo -e "Host github.com\n\tStrictHostKeyChecking no\nIdentityFile ~/.ssh/pdf.key\n" > ~/.ssh/config
  git clone git@github.com:dwango/scala_text_pdf.git
  cd scala_text_pdf
  git submodule update --init
  cd scala_text
  git pull origin master
  cd ..
  git add scala_text
  git commit -a -m "auto commit on travis $TRAVIS_JOB_NUMBER $TRAVIS_COMMIT"
  git push origin master
fi
