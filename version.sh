#!/usr/bin/env bash

DATE=`env TZ=Asia/Tokyo date "+%Y%m%d-%H%M%S"`  # 日付、タイムゾーン付き
GVER=`git show -s --format=%H`                  # currentのgitのコミットのハッシュ値

echo ${DATE}-${GVER:0:7}                        # ハッシュ値は先頭7桁%
