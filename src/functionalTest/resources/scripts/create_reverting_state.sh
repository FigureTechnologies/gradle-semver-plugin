#!/usr/bin/env bash

# Step 0: Assume the git directory is initialized and switch to the directory
repositoryDir=$1

cd "$repositoryDir" || exit

# Step 1: Create and commit an initial file
echo "This is file.txt" > file.txt
git add file.txt
git commit -m "Initial commit"

# Step 2: Make a change to the file
echo "Change in file.txt" >> file.txt
git add file.txt
git commit -m "Commit to be reverted"

# Step 4: Identify the commit hash to be reverted
commitToRevert=$(git rev-parse HEAD)

# Step 5: Start the revert process
git revert -n "$commitToRevert"

# Step 6: You are now in the middle of the revert. You can resolve conflicts if any and continue the revert process.
# For demonstration purposes, let's simulate a conflict
echo "Conflict content" > file.txt
git add file.txt
