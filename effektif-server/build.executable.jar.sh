#!/bin/bash

mvn clean compile assembly:single
mv target/effektif-server-jar-with-dependencies.jar target/effektif-server.jar 