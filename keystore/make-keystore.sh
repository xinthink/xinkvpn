#!/bin/sh
CUR_DIR=`pwd`
ANDROID_SRC=/macdroid/a
cp keystore.c $ANDROID_SRC/frameworks/base/cmds/keystore

cd $ANDROID_SRC
make -j8

cd $CUR_DIR
cp $ANDROID_SRC/out/target/product/generic/system/bin/keystore .
 
