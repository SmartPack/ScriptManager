#!/system/bin/sh

#
# Author: sunilpaulmathew <sunil.kde@gmail.com>
# Example script to flash Magisk module
# Huge thanks to osm0sis @ xda-developers
#


FLASH_FOLDER="/sdcard/flash"
ZIP_PATH="/sdcard/flash.zip"

if [ -f "$FLASH_FOLDER" ]; then
	echo "Working folder available... cleaning..."
	rm -r "$FLASH_FOLDER"/*
else
        echo "Creating working folder..."
	mkdir "$FLASH_FOLDER"
fi

if [ -f "$ZIP_PATH" ]; then
	unzip "$ZIP_PATH" -d "$FLASH_FOLDER"
	mount -o remount,rw /
	mkdir /tmp
	mke2fs -F "$FLASH_FOLDER"/tmp.ext4 500000
	mount -o loop "$FLASH_FOLDER"/tmp.ext4 /tmp/

	sh "$FLASH_FOLDER"/META-INF/com/google/android/update-binary 3 1 "$ZIP_PATH"
else
        echo "Please place your zip file as '$ZIP_PATH' & try again..."
fi

rm -r "$FLASH_FOLDER"
echo "Cleaning..."
