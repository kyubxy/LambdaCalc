#!/bin/bash
mvn package ;
java -cp target/LambdaCalc-1.0.jar com.kyubey.LambdaCalcApp
