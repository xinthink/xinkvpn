#!/bin/sh

dist_dir=../dist
apk=XinkVpn.apk
key_dir=../../keystore
key_file=xinthink_keystore
key_alias=xinthink

cp bin/VpnSettings-unsigned.apk $dist_dir/
mv $dist_dir/VpnSettings-unsigned.apk $dist_dir/$apk.unsigned

echo "cleaning"
rm $dist_dir/*.apk
rm $dist_dir/*.unaligned

echo "sign the apk"
cp $dist_dir/$apk.unsigned $dist_dir/$apk.unaligned
jarsigner -verbose -keystore $key_dir/$key_file $dist_dir/$apk.unaligned $key_alias
jarsigner -verify -verbose $dist_dir/$apk.unaligned

echo "align the apk"
zipalign -fv 4 $dist_dir/$apk.unaligned $dist_dir/$apk

