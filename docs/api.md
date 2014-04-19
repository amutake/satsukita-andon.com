RESTful APIs
===========

設計思想
--------

### 案1

最小の API のみ

### 案2

ページに合わせた API

URL
---

URL              | Description
---------------- | ----------------
https://api.satsukita-andon.com/v1/ | version 1.0
https://api.satsukita-andon.com/dev/ | development version

Open APIs
---------

URL              | Method           | Type             | Description
---------------- | ---------------- | ---------------- | ----------------
gallery/grands   | GET | [(times: Int, theme: String, thumbnail: URL)] | grand prize thumbnails
class/search     | GET | (times: Int, grade: Int, prize: String, tag: String) -> [Class] | search classes
articles/get     | GET | id: Int -> Article | get the article
articles/comments | GET | id: Int -> [Comment] | get comments
comments/

Closed APIs
-----------

URL              | Method           | Type             | Description
---------------- | ---------------- | ---------------- | ----------------
login            | GET | (username : String, password : String) -> User | login
