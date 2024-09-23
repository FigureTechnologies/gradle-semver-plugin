#!/usr/bin/env bash

# Step 0: Assume the git directory is initialized and switch to the directory
repositoryDir=$1

cd "$repositoryDir" || exit

# Step 1: Create and commit an initial file
echo "This is file.txt" > file.txt
git add file.txt
git commit -m "Initial commit"

# Step 2: Make some changes in the main branch
echo "Changes in main branch" >> file.txt
git add file.txt
git commit -m "Commit in main branch"

# Step 3: Switch back to the initial commit (detached head state)
git checkout HEAD~1
