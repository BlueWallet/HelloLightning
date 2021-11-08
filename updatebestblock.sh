TIP=`curl -s  https://blockstream.info/api/blocks/tip/height`
echo "tip: $TIP"
HASH=`curl -s https://blockstream.info/api/block-height/$TIP`
echo "hash: $HASH"
HEADER=`curl -s https://blockstream.info/api/block/$HASH/header`
echo "header: $HEADER"

URL="http://127.0.0.1:8310/updatebestblock/$HEADER/$TIP"
curl $URL

