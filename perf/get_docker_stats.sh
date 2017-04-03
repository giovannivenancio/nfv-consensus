#!/bin/bash

rm log;

while true;
do
    docker stats --no-stream >> log;
done
