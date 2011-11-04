require 'rubygems'
require "bundler/setup"
require 'buildr-dependency-extensions'

# Version number for this release
VERSION_NUMBER = "1.1.#{ENV['BUILD_NUMBER'] || 'SNAPSHOT'}"
# Group identifier for your projects
GROUP = "com.springsense"
COPYRIGHT = "(C) Copyright 2011 SpringSense Trust. All rights reserved. Licensed under  the Apache License, Version 2.0. See file LICENSE."

# Specify Maven 2.0 remote repositories here, like this:
repositories.remote << "http://www.ibiblio.org/maven2/"
repositories.release_to = 'sftp://artifacts:repository@192.168.0.96/home/artifacts/repository'

desc "The Java_api_bindings project"
define "java_api_bindings" do
  extend PomGenerator

  project.version = VERSION_NUMBER
  project.group = GROUP
  manifest["Implementation-Vendor"] = COPYRIGHT
  
  GSON = artifact('com.google.code.gson:gson:jar:1.6')
  OPENCSV = artifact('net.sf.opencsv:opencsv:jar:2.3')  
  GUAVA = artifact('com.google.guava:guava:jar:r09')  

  JUNIT4 = artifact("junit:junit:jar:4.8.2")
  HAMCREST = artifact("org.hamcrest:hamcrest-core:jar:1.2.1")
  MOCKITO = artifact("org.mockito:mockito-all:jar:1.8.5")
       
  compile.with GSON, GUAVA, OPENCSV
  compile.using :target => "1.5"
  test.compile.with JUNIT4, HAMCREST, MOCKITO
  
  package(:jar)
end
