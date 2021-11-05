ENTROPY="00000000000000000000000000000000000000000000000000000000000000f6"
TIP=`curl -s  https://blockstream.info/api/blocks/tip/height`
echo "tip: $TIP"
HASH=`curl -s https://blockstream.info/api/block-height/$TIP`
echo "hash: $HASH"

echo "start $ENTROPY $TIP $HASH" | timeout 1 nc localhost  8310 -i 1
echo 'connect 037cc5f9f1da20ac0d60e83989729a204a33cc2d8e80438969fadf35c1c5f1233b 165.227.103.83 9735' | timeout 1 nc localhost  8310 -i 1

