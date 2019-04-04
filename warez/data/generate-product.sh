#!/bin/bash -e

##########
# Generate a Json Document representing a product.
##########

#set -x

ROOTDIR=$(cd $(dirname $0); pwd)

uuid=$(shuf -n 1 "${ROOTDIR}/values/uuids.txt")
mapfile -t words < <(shuf -n 12 "${ROOTDIR}/values/5-letters-words.txt")
mapfile -t keywords < <(shuf -n 2 "${ROOTDIR}/values/keywords.txt")

cat << EOF
{ 
  "id": "$uuid",
  "name": "${words[0]}-${words[1]}",
  "description": "${words[2]} ${words[3]} ${words[4]}, ${words[5]} ${words[6]}.",
  "keywords": [
    "${keywords[0]}",
    "${keywords[1]}"
  ],
  "skus": [
    {
      "id": "${uuid%..}aa",
      "name": "${words[0]}-${words[1]}-${words[7]}"
    },
    {
      "id": "${uuid%..}bb",
      "name": "${words[0]}-${words[1]}-${words[8]}"
    }
  ]
}
EOF

