#!/bin/sh

#
# Build the project.
#
# - Script Setup
# - Fetch Submodules
# It initialises all submodules if not yet done.
# It pulls from all submodules.
#
# - Build and Publish Submodules Locally
# It builds the jml parser submodule and publish it to maven local.
# It build the base contract-chameleon submodule and publish it to maven local.
#
# - Build Adapters
# It cleans and builds the main modules.
#
# See README.md for details

# - Script Setup

# identify the root file of the repository
ROOT="$(cd "$(dirname "$0")" && pwd)"
# go to the root directory
cd "$ROOT"

# - Fetch submodules

# fetch and initialize the submodules the first time
git submodule update --init --recursive

# pull changes from submodules
git pull --rebase --recurse-submodules

# - Build and Publish Submodules Locally
# publish custom jml parser
cd "$ROOT/libs/jmlparser"
./mvnw clean install -DskipTests

# publish custom jml parser
cd "$ROOT/libs/contract-chameleon"
./gradlew clean publishToMavenLocal

#check that snapshots exist
ls ~/.m2/repository/io/github/jmltoolkit/jmlparser-core/
ls ~/.m2/repository/org/contract_lib/contract-chameleon/

# - Build Adapters
cd "$ROOT"
# build with gradle
./gradlew clean build
