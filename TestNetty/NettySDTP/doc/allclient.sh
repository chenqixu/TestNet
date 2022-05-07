if [ $# -eq 1 ]; then
  num=$1
  echo "You input num is ${num}"
  for i in $(seq 1 $num);
  do
    sh client.sh >/dev/null 2>&1 &
  done
  wait
else
  echo "please input num."
fi
