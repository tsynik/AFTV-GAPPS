#!/bin/bash
export PROJ=`pwd`
export AJ=/android-sdk/platforms/android-19/android.jar

aapt package -f -m -J $PROJ/src -M $PROJ/AndroidManifest.xml -S $PROJ/res -I $AJ
aapt package -f -m -F $PROJ/bin/AFTV-GAPPS-XM.unaligned.apk -M $PROJ/AndroidManifest.xml -S $PROJ/res -A $PROJ/assets -I $AJ
mkdir -p obj
javac -d obj -classpath "src:provided/XposedBridgeApi-54.jar" -bootclasspath $AJ src/tsynik/xposed/mod/gapps/*.java
mkdir -p bin
dx --dex --output=$PROJ/bin/classes.dex $PROJ/obj
cp $PROJ/bin/classes.dex .
aapt add $PROJ/bin/AFTV-GAPPS-XM.unaligned.apk classes.dex
zipalign -f 4 $PROJ/bin/AFTV-GAPPS-XM.unaligned.apk $PROJ/bin/AFTV-GAPPS-XM.133t.apk

# SIGN HERE


