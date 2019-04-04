#!/bin/bash -e

##########
# Generate a Json Document representing a stock update.
##########

#set -x

ROOTDIR=$(cd $(dirname $0); pwd)

uuid=$(shuf -n 1 "${ROOTDIR}/values/uuids.txt")
if [ $((RANDOM%2)) -eq 0 ]
then
  sku_suffix="aa"
else
  sku_suffix="bb"
fi

cat << EOF
{ 
  "productId": "$uuid",
  "skuId": "${uuid%..}$sku_suffix",
  "diff": $(((RANDOM%21)-10))
}
EOF

