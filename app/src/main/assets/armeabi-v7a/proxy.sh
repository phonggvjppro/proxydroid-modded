#!/system/bin/sh

DIR=$1
action=$2
type=$3
host=$4
port=$5
auth=$6
user=$7
pass=$8

PATH=$DIR:$PATH
proxy_port=8123

write_base_config() {
  echo "
base {
  log_debug = off;
  log_info = off;
  log = stderr;
  daemon = on;
  redirector = iptables;
}
" >$DIR/redsocks.conf
}

write_non_http_config() {
  if [ "$auth" = "true" ]; then
    echo "
redsocks {
  bind = \"0.0.0.0:8123\";
  relay = \"$host:$port\";
  type = $type;
  login = \"$user\";
  password = \"$pass\";
}
" >>$DIR/redsocks.conf
  else
    echo "
redsocks {
  bind = \"0.0.0.0:8123\";
  relay = \"$host:$port\";
  type = $type;
}
" >>$DIR/redsocks.conf
  fi
}

write_http_config() {
  if [ "$auth" = "true" ]; then
    echo "
redsocks {
  bind = \"0.0.0.0:8123\";
  relay = \"$host:$port\";
  type = http-relay;
  login = \"$user\";
  password = \"$pass\";
}
redsocks {
  bind = \"0.0.0.0:8124\";
  relay = \"$host:$port\";
  type = http-connect;
  login = \"$user\";
  password = \"$pass\";
}
" >>$DIR/redsocks.conf
  else
    echo "
redsocks {
  bind = \"0.0.0.0:8123\";
  relay = \"$host:$port\";
  type = http-relay;
}
redsocks {
  bind = \"0.0.0.0:8124\";
  relay = \"$host:$port\";
  type = http-connect;
}
" >>$DIR/redsocks.conf
  fi
}

start_proxy() {
  write_base_config
  if [ "$type" = "http" ]; then
    proxy_port=8124
    write_http_config
  else
    write_non_http_config
  fi

  $DIR/redsocks2 -p $DIR/redsocks.pid -c $DIR/redsocks.conf
  iptables -A INPUT -i ap+ -p tcp --dport 8123 -j ACCEPT
  iptables -A INPUT -i ap+ -p tcp --dport 8124 -j ACCEPT
  iptables -A INPUT -i lo -p tcp --dport 8123 -j ACCEPT
  iptables -A INPUT -i lo -p tcp --dport 8124 -j ACCEPT
  iptables -A INPUT -p tcp --dport 8123 -j DROP
  iptables -A INPUT -p tcp --dport 8124 -j DROP
  iptables -t nat -A PREROUTING -i ap+ -p tcp -d 192.168.43.1/24 -j RETURN
  iptables -t nat -A PREROUTING -i ap+ -p tcp -j REDIRECT --to $proxy_port
}

stop_proxy() {
  iptables -t nat -D PREROUTING -i ap+ -p tcp -d 192.168.43.1/24 -j RETURN
  iptables -t nat -D PREROUTING -i ap+ -p tcp -j REDIRECT --to 8123
  iptables -t nat -D PREROUTING -i ap+ -p tcp -j REDIRECT --to 8124
  iptables -D INPUT -i ap+ -p tcp --dport 8123 -j ACCEPT
  iptables -D INPUT -i ap+ -p tcp --dport 8124 -j ACCEPT
  iptables -D INPUT -i lo -p tcp --dport 8123 -j ACCEPT
  iptables -D INPUT -i lo -p tcp --dport 8124 -j ACCEPT
  iptables -D INPUT -p tcp --dport 8123 -j DROP
  iptables -D INPUT -p tcp --dport 8124 -j DROP

  killall -9 redsocks2
  killall -9 cntlm

   kill -9 `cat $DIR/redsocks.pid`

   rm $DIR/redsocks.pid

   rm $DIR/redsocks.conf
}

case $action in
start)
  start_proxy
  ;;
stop)
  stop_proxy
  ;;
esac