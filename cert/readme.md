#Self Signed Server Certificate Instructions

## Generate CA private key

openssl genrsa -aes256 -out ca.key 4096

## Generate the root CA

openssl req -new -x509 -sha256 -days 730 -key ca.key -out ca.crt

## Verify

openssl x509 -noout -text -in ca.crt

## Generate Server Key

openssl genrsa -out kucuk.com.key 2048

## Generate Server Cert

openssl x509 -req -days 365 -sha256 -in kucuk.csr -CA ca.crt -CAkey ca.key -set_serial 1 -out kucuk.crt

## Convert server keys to pem format

openssl pkcs8 -topk8 -inform pem -in kucuk.com.key -outform pem -nocrypt -out kucuk.com.pem