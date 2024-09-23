#!/usr/bin/env bash

# Step 0: Assume the git directory is initialized and switch to the directory
repositoryDir=$1

cd "$repositoryDir" || exit

# Step 1: Create and commit an initial file
echo "This is file.txt" > file.txt
git add file.txt
git commit -m "Initial commit"

# Step 2: Create a bug in the code (for demonstration purposes)
echo "Bug introduced" >> file.txt
git add file.txt
git commit -m "Bug commit"

# Step 3: Start the bisecting process
git bisect start

# Step 4: Mark the current commit as bad
git bisect bad

# Step 5: Mark a known good commit (e.g., the initial commit)
git bisect good HEAD~1
