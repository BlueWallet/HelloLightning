ENTROPY="00000000000000000000000000000000000000000000000000000000000000f6"
TIP=`curl -s  https://blockstream.info/api/blocks/tip/height`
echo "tip: $TIP"
HASH=`curl -s https://blockstream.info/api/block-height/$TIP`
echo "hash: $HASH"

URL="http://127.0.0.1:8310/start/$ENTROPY/$TIP/$HASH"
curl $URL

