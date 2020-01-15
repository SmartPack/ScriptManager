#!/system/bin/sh

#
# Author: sunilpaulmathew <sunil.kde@gmail.com>
#
# An example script, which will enable USB fast charge
# upon execution.
#

echo "1" > /sys/kernel/fast_charge/force_fast_charge
echo "USB fast charge is enabled"
