#!/bin/bash
set -x

MONGODB_VERSION=${MONGODB_VERSION-2.6.5}
MONGODB_HOME=${MONGODB_HOME-$HOME/.heisenberg/mongodb-$MONGODB_VERSION}

DATABASES_HOME=$HOME/.heisenberg
DATABASE_DIR=$DATABASES_HOME/db

mkdir -p $DATABASE_DIR

# --config $DATABASE_DIR/mongodb.conf 
$MONGODB_HOME/bin/mongod --dbpath $DATABASE_DIR --fork --logpath $DATABASE_DIR/mongodb.log
