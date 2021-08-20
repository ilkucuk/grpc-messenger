# grpc-messenger

Simple GRPC application for load testing

## How to Run

sudo java -jar target/grpc-messenger-service-1.0-SNAPSHOT.jar config/kucuk.yaml

## Update /etc/hosts file

```
kucuk.com is the url I used to enable tls, it is a self signed cert

[ip of the host]	kucuk.com
[ip of the host]	kucuk2.com
ex:
192.168.56.4	kucuk.com
192.168.56.5	kucuk2.com

```

# How to Run Load Test

jmeter -n -t grpc-load.jmx -l run1.log -e -o run1


