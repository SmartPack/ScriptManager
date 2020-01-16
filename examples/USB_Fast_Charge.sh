#!/system/bin/sh

#
# Author: sunilpaulmathew <sunil.kde@gmail.com>
# Example script to enable USB fast charging
#

FAST_CHARGE="/sys/kernel/fast_charge/force_fast_charge"

if [ ! -f "$FAST_CHARGE" ]; then
	echo "USB fast charge not supported..."
else 
	echo "1" > "$FAST_CHARGE"
	echo "USB fast charge is enabled..."
fi
