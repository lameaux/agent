rem Generate certificate and private key
openssl genrsa -out agent.key 2048
openssl req -new -out agent.csr -key agent.key
openssl x509 -req -days 365 -in agent.csr -signkey agent.key -out agent.crt

rem Create PKCS12 keystore (use password 123456)
openssl pkcs12 -export -in agent.crt -inkey agent.key -out agent.p12 -name agent

rem Convert PKCS12 into JKS
keytool -importkeystore -deststorepass 123456 -destkeypass 123456 -srckeystore agent.p12 -destkeystore agent.keystore  -srcstoretype PKCS12 -srcstorepass 123456 -alias agent
