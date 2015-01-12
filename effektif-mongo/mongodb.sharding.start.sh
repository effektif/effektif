#!/bin/bash

set -x

MONGODB_VERSION=${MONGODB_VERSION-2.6.5}
MONGODB_HOME=${MONGODB_HOME-$HOME/.heisenberg/mongodb-$MONGODB_VERSION}

DATABASES_HOME=$HOME/.heisenberg

DATABASE_DIR=$DATABASES_HOME/shard
CONFIGSVR_DIR=$DATABASES_HOME/cfg
MONGOS_DIR=$DATABASES_HOME/mongos

if [ ! -d ${DATABASE_DIR}1 ]; then
    mkdir -p ${DATABASE_DIR}1
    echo " " > ${DATABASE_DIR}1/mongodb.conf 
fi;

if [ ! -d ${DATABASE_DIR}2 ]; then
    mkdir -p ${DATABASE_DIR}2
    cp -f ${DATABASE_DIR}1/mongodb.conf ${DATABASE_DIR}2 
fi;

if [ ! -d ${DATABASE_DIR}3 ]; then
    mkdir -p ${DATABASE_DIR}3
    cp -f ${DATABASE_DIR}1/mongodb.conf ${DATABASE_DIR}3
fi;

if [ ! -d ${CONFIGSVR_DIR}1 ]; then
    mkdir -p ${CONFIGSVR_DIR}1
fi;

if [ ! -d ${CONFIGSVR_DIR}2 ]; then
    mkdir -p ${CONFIGSVR_DIR}2
fi;

if [ ! -d ${CONFIGSVR_DIR}3 ]; then
    mkdir -p ${CONFIGSVR_DIR}3
fi;

if [ ! -d ${MONGOS_DIR} ]; then
    mkdir -p ${MONGOS_DIR}
fi;

echo Starting the configuration servers

$MONGODB_HOME/bin/mongod --port 28001 --fork --configsvr --dbpath ${CONFIGSVR_DIR}1 --logpath ${CONFIGSVR_DIR}1/cfg.log
$MONGODB_HOME/bin/mongod --port 28002 --fork --configsvr --dbpath ${CONFIGSVR_DIR}2 --logpath ${CONFIGSVR_DIR}2/cfg.log
$MONGODB_HOME/bin/mongod --port 28003 --fork --configsvr --dbpath ${CONFIGSVR_DIR}3 --logpath ${CONFIGSVR_DIR}3/cfg.log

echo Starting the mongos proxy

$MONGODB_HOME/bin/mongos --fork --configdb localhost:28001,localhost:28002,localhost:28003 --logpath $MONGOS_DIR/mongos.log

echo Starting the mongod shards

$MONGODB_HOME/bin/mongod --port 29001 --dbpath ${DATABASE_DIR}1 --config ${DATABASE_DIR}1/mongodb.conf --fork --logpath ${DATABASE_DIR}1/mongodb.log
$MONGODB_HOME/bin/mongod --port 29002 --dbpath ${DATABASE_DIR}2 --config ${DATABASE_DIR}2/mongodb.conf --fork --logpath ${DATABASE_DIR}2/mongodb.log
$MONGODB_HOME/bin/mongod --port 29003 --dbpath ${DATABASE_DIR}3 --config ${DATABASE_DIR}3/mongodb.conf --fork --logpath ${DATABASE_DIR}3/mongodb.log

$MONGODB_HOME/bin/mongo admin --eval "db.runCommand({'addShard':'localhost:29001'})"
$MONGODB_HOME/bin/mongo admin --eval "db.runCommand({'addShard':'localhost:29002'})"
$MONGODB_HOME/bin/mongo admin --eval "db.runCommand({'addShard':'localhost:29003'})"

$MONGODB_HOME/bin/mongo admin --eval "db.runCommand( { enableSharding: 'heisenberg' } )"

$MONGODB_HOME/bin/mongo heisenberg --eval "db.createCollection('processInstances')"
$MONGODB_HOME/bin/mongo heisenberg --eval "db.collection.ensureIndex( { _id: 'hashed' } )"

$MONGODB_HOME/bin/mongo admin --eval "db.runCommand( { shardCollection: 'heisenberg.processInstances', key: { '_id': 'hashed' } } )"
