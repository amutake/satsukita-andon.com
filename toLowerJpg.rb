#!/usr/bin/env ruby

def isJPG(path)
  File.extname(path) == ".JPG"
end

def toLowerJpg(path)
  dir = File.dirname(path)
  lower = File.basename(path, ".JPG") + ".jpg"
  File.rename(path, dir + "/" + lower)
end

def recSelect(dir)
  Dir.entries(dir).select { |p|
    File.basename(p, "") != "." && File.basename(p, "") != ".."
  }.map { |p|
    if File.directory?(dir + p)
      recSelect(dir + p + "/")
    else
      dir + p
    end
  }.flatten
end

if ARGV[0] then
  puts "recursive search.."
  jpgs = recSelect(ARGV[0] + "/").select { |p| isJPG(p) }
  puts jpgs
  if jpgs.size == 0
    puts "there is no *.JPG"
  else
    print "JPG -> jpg. OK? [y/n] "
    if STDIN.gets == "y\n"
      jpgs.map { |p| toLowerJpg(p) }
      puts "done"
    else
      puts "abort"
    end
  end
else
  puts "this is recursive .JPG -> .jpg"
  puts "toLowerJpg <dir>"
end
