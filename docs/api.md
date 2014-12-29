RESTful APIs
===========

設計思想
--------

Web API The Good Parts に則る


URL
---

URL              | Description
---------------- | ----------------
https://api.satsukita-andon.com/v1/ | version 1.0
https://api.satsukita-andon.com/dev/ | development version


Open APIs
---------

### Articles

URL              | Method | Description
---------------- | ------ | --------------------------------
articles         | GET    | article オブジェクトのリスト。クエリパラメータでフィルターできる
articles/:id     | GET    | 指定した ID の article オブジェクト
articles/:id/comments | GET | 指定した ID の記事が持つコメントのリスト


### Classes

URL              | Method | Description
---------------- | ------ | --------------------------------
classes          | GET    |
classes/:times/:grade/:class | GET | 指定したクラスの情報を得る
classes/:times/:grade/:class/reviews | GET | 講評
classes/:times/:grade/:class/images | 画像リスト


### Times

URL              | Method | Description
---------------- | ------ | --------------------------------
times            | GET    | times オブジェクトのリスト
times/:times     | GET    | 指定した回の times オブジェクト


### Data

URL              | Method | Description
---------------- | ------ | --------------------------------
data             | GET    | ジャンルでフィルターとか
data/:id         | GET    | タイトルとURLなど


Auth APIs
---------

URL              | Method | Description
---------------- | ------ | --------------------------------
oauth2/token     | GET    | OAuth2 のトークン


Auth Required APIs
------------------

URL              | Method | Description
---------------- | ------ | --------------------------------
classes/:times/:grade/:class | POST | クラスを作成
classes/:times/:grade/:class | PUT  | クラスをアップデート
classes/:times/:grade/:class | DELETE | クラスを削除
classes/:times/:grade/:class/images | POST | 画像の追加
classes/:times/:grade/:class/images/:url | POST | 画像の削除
classes/:times/:grade/:class/reviews | POST | 講評の追加
classes/:times/:grade/:class/reviews | PUT | 講評のアップデート
classes/:times/:grade/:class/reviews | DELETE | 講評の削除
times/:times     | POST   | 新しく回を作る
times/:times     | PUT    | 回情報をアップデート
times/:times     | DELETE | 回情報を削除
articles         | POST   | 新しく記事を作る
articles/:id     | PUT    | 記事をアップデート
articles/:id     | DELETE | 記事を削除
data             | POST   | 新しく資料をアップロード
data/:id         | PUT    | 資料をアップデート
data/:id         | DELETE | 資料を削除
users            | GET    | ユーザー情報のリスト
users            | POST   | 新規ユーザー登録
users/:name      | GET    | ユーザー情報
users/:name      | PUT    | ユーザー情報のアップデート
users/:name      | DELETE | ユーザーの削除
