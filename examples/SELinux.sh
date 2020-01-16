#!/system/bin/sh

#
# Author: sunilpaulmathew <sunil.kde@gmail.com>
# Example script to set permissive SELinux 
#

SELINUX="/sys/fs/selinux/enforce"

if [ ! -f "$SELINUX" ]; then
	echo "Unknown SELinux status..."
else 
	echo "0" > "$SELINUX"
	echo "Setting SELinux into permissive mode..."
fi
