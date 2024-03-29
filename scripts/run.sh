#!/bin/bash
mvn package ;
java -cp target/LambdaCalc-1.0-SNAPSHOT.jar com.kyubey.LambdaCalcApp
