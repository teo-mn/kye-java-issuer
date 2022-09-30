# README

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