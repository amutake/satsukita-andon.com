行灯職人への道 - satsukita-andon.com
====================================

http://satsukita-andon.com

行灯職人への道 (satsukita-andon.com) is a website to record and support *andon-gyoretsu* (行灯行列), which is one of the most exciting events of Sapporo-Kita High School (札幌北高校).

![logo](https://raw.githubusercontent.com/amutake/satsukita-andon.com/master/public/img/logo.png)

Required
--------

- play-2.1.5
- java-1.7
- ImageMagick (`mogrify` command)
- Twitter app (`cp twitter4j.properties.example twitter4j.properties` and edit it)
  - consumer key
  - consumer secret
  - access token
  - access token secret
- h2.jar (http://www.h2database.com/html/download.html)
  - checked with h2-1.3.*
  - put this binary to /db directory with the name `h2.jar`

Usage of `andon` command
------------------------

`andon` is a convenient command for (start|stop|restart|backup) application.

Usage: andon (start|stop|restart|backup)

- `start`
  - start application
  - to detach, type Ctrl-D
- `stop`
  - stop application
- `restart`
  - restart application with a little down time
- `backup`
  - backup /files and DB contents
  - require h2.jar
  - backup-ed files are in /backup directory
  - automatically restart

If you want to change port-number, edit `andon` file and change `port` variable to another number.

Branches
--------

- master
  - http://satsukita-andon.com
- test
  - http://test.satsukita-andon.com

Development
-----------

use `play run`

If you want to insert initial data, add the following.

(app/Global.scala)

```scala
...

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    ...
    InitialData.insert
    ...
  }

  ...

}

...

object InitialData {
  ...
  def insert = DB.db.withSession { implicit session: Session =>
    Accounts.create("開発者", "developer", "password", OrdInt(60), Admin)
  }
}
```

Contributing
------------

1. Fork it (https://github.com/amutake/satsukita-andon.com/fork)
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -am 'Add some feature')
4. Push to the branch (git push origin my-new-feature)
5. Create new Pull Request
