#!/usr/bin/env bash

# Step 0: Assume the git directory is initialized and switch to the directory
repositoryDir=$1
main_branch=$2
feature_branch=$3

cd "$repositoryDir" || exit

# Step 1: Create and commit an initial file
echo "This is file.txt" > file.txt
git add file.txt
git commit -m "Initial commit"

# Step 3: Create a feature branch
git checkout -b "$feature_branch"

# Step 4: Modify the file in the feature branch
echo "Feature branch change" >> file.txt
git add file.txt
git commit -m "Feature branch commit"

# Step 5: Switch back to the main branch
git checkout "$main_branch"

# Step 6: Modify the same line in the file in the main branch
echo "Main branch change" > file.txt
git add file.txt
git commit -m "Main branch commit"

# Step 7: Attempt to rebase the feature branch onto the main branch
git merge "$feature_branch"
