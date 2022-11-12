# README

## Build

```shell
mvn -s custom_settings.xml dependency:list
mvn clean install 
```

### EmployeeCardIssuer 
#### Smart contract address
`testnet: 0xbf55b2485272292FF0063142fB244CDd0985B2Ee`\
`mainnet: 0xe961164FA800988DfCBE238f6e937697A620140D`
#### Node url
`testnet: https://node-testnet.corexchain.io`\
`mainnet: https://node.corexchain.io`
#### Chain id
`testnet: 3305`\
`mainnet: 1104`

### Ашиглах жишээ
Тест сүлжээ бол:
`curl --location --request POST 'http://localhost:8081/api/v1/issuer/issue-test' \
--header 'X-API-KEY: abc123' \
--header 'Content-Type: application/json' \
--data '{
    "sourcePath": "test1.pdf",
    "destinationPath": "test2.pdf",
    "desc": "test1"
}'
`

Үндсэн сүлжээ бол:
`curl --location --request POST 'http://localhost:8081/api/v1/issuer/issue' \
--header 'X-API-KEY: abc123' \
--header 'Content-Type: application/json' \
--data '{
    "sourcePath": "test1.pdf",
    "destinationPath": "test2.pdf",
    "desc": "test1"
}'
`

X-API-Key утгыг verify-service.env файлаас харна.

sourcePath, destinationPath-ийн утгыг `/root/corex/file_directory/` фолдер дотор дотоод замыг зааж өгнө. 
Бүтэн зам зааж өгвөл docker дотор мэдэх боломжгүй тул алдаа гарна. 
sourcePath, destinationPath 2 утга ижил байж болно.

#### Унтрааж асаах
`docker-compose.yaml` дотор `restart:always` гэж тохируулсан тул үргэлж өөрөө автоматаар асах ёстой. 
Хэрэв асаагүй бол `/root/corex` фолдерт ороод `docker compose up -d` коммандыг ажиллуулж асаана.  

#### Лог харах
`docker ps` коммандаар асаалттай docker-ийн сервисүүдийг харна. Тэндээс container id-г хуулж аваад 
`docker logs -f {CONTAINER_ID} -n 1000` гэж сүүлийн 1000 мөрийг шалгах боломжтой.