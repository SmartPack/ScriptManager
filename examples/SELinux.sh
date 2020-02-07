#!/system/bin/sh

#
# Author: sunilpaulmathew <sunil.kde@gmail.com>
# Example script to tweak SELinux state 
#

SELINUX_STATUS="$(getenforce)"
SELINUX="setenforce"

if [ "$SELINUX_STATUS" = "Enforcing" ]; then
	echo "Setting SELinux into Permissive mode..."
	"$SELINUX" 0
elif [ "$SELINUX_STATUS" = "Permissive" ]; then
	echo "Setting SELinux into Enforcing mode..."
	"$SELINUX" 1
else
	echo "0" > "$SELINUX"
	echo "Unknown SELinux state..."
fi
