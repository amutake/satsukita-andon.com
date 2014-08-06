行灯職人への道 - satsukita-andon.com
====================================

![logo](https://raw.githubusercontent.com/amutake/satsukita-andon.com/master/docs/logo_black.png)

http://satsukita-andon.com

行灯職人への道 (satsukita-andon.com) is a website to record and support *andon-gyoretsu* (行灯行列), which is one of the most exciting events of Sapporo-Kita High School (札幌北高校).


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
  - checked with h2-1.3.*.jar
  - put this binary to /db directory with the name `h2.jar`

Usage of `andon` command
------------------------

`andon` is a convenient command for (start|stop|restart|backup|restore) this application.

Usage: andon (start|stop|restart|backup|restore)

- `start`
  - starts the application
  - to detach, type Ctrl-D
- `stop`
  - stops the application
- `restart`
  - restarts the application with a little down time (about 10 seconds)
- `backup`
  - takes backup of the contents of `files` directory and the DB contents
  - automatically restarts the application with a little down time (about 30 seconds)
  - `h2.jar` is needed
  - backup files will be in `backup` directory
    - e.g., `2014-08-03.tar.gz` (`files` directory) and `2014-08-03.sql` (DB records)
    - you should move (or send) these files into backup storage
- `restore`
  - restores backup files
  - down time is about 1 minute.
  - current contents will be deleted! be careful!
  - to restore to 2014-08-05, run `./andon restore 2014-08-05`
    - `backup/2014-08-05.tar.gz`, `backup/2014-08-05.sql` and `db/h2.jar` are needed

If you want to change port-number, edit `andon` file and change `port` variable to another number.

Branches
--------

- master
  - http://satsukita-andon.com
- test
  - http://test.satsukita-andon.com

Development
-----------

Use `play run` instead of `./andon start`.

If you want to insert initial data, add the following code.

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
