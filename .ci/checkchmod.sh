#!/bin/bash

set -e

# CI environments may change file permissions; only executable bits are checked.
CHMOD=$(find -type f \
          -not -path '*/.git/*' \
          -not -path './mvnw' \
          -not -path './.ci-temp/*' \
          -a -type f -not -name '*.sh' \
          -a -type f -not -name '*.pl' \
          -a \( -type d -not -perm 775 -o -type f -executable \))
if [[ -n $CHMOD ]]; then
    echo "Expected mode for non '.sh' files is 664.";
    echo "Files that violates this rule:"
    for NAMEFILE in $CHMOD
    do
        echo "$NAMEFILE";
    done
    exit 1;
fi

# Check all '.sh' files for missing executable permission
CHMOD=$(find -type f \
          -not -path '*/.git/*' \
          -not -path './mvnw' \
          -name '*.sh' \
          -not -executable)

if [[ -n $CHMOD ]]; then
    echo "Expected mode for '.sh' files is 755.";
    echo "Files that violates this rule:"
    for NAMEFILE in $CHMOD
    do
        echo "$NAMEFILE";
    done
    exit 1;
fi
