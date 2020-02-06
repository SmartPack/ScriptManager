#!/system/bin/sh

#
# Author: sunilpaulmathew <sunil.kde@gmail.com>
# Example script to tweak Android Doze 
#

DOZE_STATUS="$(dumpsys deviceidle enabled)"

if [ "$DOZE_STATUS" = 1 ]; then
  echo "Disabling Doze Mode..."
  dumpsys deviceidle disable
elif [ "$DOZE_STATUS" = 0  ]; then
  echo "Enabling Doze Mode..."
  dumpsys deviceidle enable
  dumpsys deviceidle force-idle
else
  echo "Unknown Status..."
fi
