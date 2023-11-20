# Why has this fork?
I try to implement a workflow like this:
```
user request --> wolf --> decrypt the request body ---> upstream xxx --> encrypt the response ---> send it out
```
the apisix has some issues which can:<br>
1、rewrite the body and send to upstream in ext-plugin-post-req.<br>
2、we can't correlate the request and response between ext-plugin-post-req and ext-plugin-post-resp.<br> 

So in this project, I changed some code to make sure it can work as expected.<br>
I implemented [DecryptRequestFilter](runner-starter/src/main/java/org/apache/apisix/plugin/runner/filter/DecryptRequestFilter.java)
to decrypt the request filter and sent it to upstream with decrypted body. <br>
and [EncryptResponseFilter](runner-starter/src/main/java/org/apache/apisix/plugin/runner/filter/EncryptResponseFilter.java) to encrypt the response and sent it out.

This is not a pretty work. but for now (2023/11/20), I have to change the source code to make it work.

# build
first install the localfix jar:
```shell
cd runner-code
mvn install -DskipTests -Dgpg.skip=true

cd runner-plugin-sdk
mvn install -DskipTests -Dgpg.skip=true

mvn package task in the apisix java runner starter module.

```
also need the required 0.6.1-RELEASE for https://github.com/api7/ext-plugin-proto. (the jar is not released at 2023/11/20).
```shell
mvn install -DskipTests -Dgpg.skip=true
```

# How to use the decrypt and encrypt the response filter
An example route conf in apisix:
```
  "plugins": {
    "ext-plugin-post-req": {
      "_meta": {
        "disable": false
      },
      "conf": [
        {
          "name": "DecryptRequestFilter",
          "value": "{}"
        }
      ]
    },
    "ext-plugin-post-resp": {
      "_meta": {
        "disable": false
      },
      "conf": [
        {
          "name": "EncryptResponseFilter",
          "value": "{}"
        }
      ]
    },
    "hmac-auth": {
      "_meta": {
        "disable": true
      }
    },
    "wolf-rbac": {
      "_meta": {
        "disable": false
      }
    }
  }
```

an example wolf conf:
```
{
  "username": "wolftokengenerator",
  "desc": "wolftoken生成",
  "plugins": {
    "wolf-rbac": {
      "_meta": {
        "disable": false
      },
      "appid": "apigateway",
      "header_prefix": "internal-",
      "server": "http://wolf-server.wolf:80"
    }
  }
}
```

# This project requires 3.6.0
https://github.com/apache/apisix/pull/9990


# require db information
```sql
CREATE DATABASE apigateway;
CREATE TABLE apigateway.user
(
    id          int auto_increment primary key                     not null,
    userid      int unique                          not null COMMENT 'wolf中的用户id',
    publickey   varchar(1024)                       not null COMMENT '公钥',
    privatekey  varchar(1024)                       not null COMMENT '私钥',
    status      tinyint(1) default 1 not null comment '状态：1.正常 0.删除',
    gmtcreate   timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    gmtmodified timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '修改时间'
)
```

# generate the encryption key pair
[MySmUtilTest.java](./runner-starer/src/test/java/cn/sichuancredit/apigateway/encryption/MySmUtilTest.java)

# how it works
https://github.com/apache/apisix-java-plugin-runner/blob/main/docs/en/latest/how-it-works.md
