(ns leiningen.polylith.cmd.help.info)

(defn help [sha1 sha2]
  (println "  Show the content of a Polylith workspace and its changes if Git hashes are given")
  (println "  (each row is followed by an * if something is changed)")
  (println)
  (println "  lein polylith info [x] [s1 s2]")
  (println "    x = a[ll]       -> show all interfaces, components, bases and systems")
  (println "        c[hanged]   -> show changed interfaces, components, bases and systems")
  (println "        u[nchanged] -> show unchanged interfaces, components, bases and systems")
  (println "        (omitted)   -> show all components, bases, systems (and interfaces if changed)")
  (println "    s1 = last (successful) Git sha1")
  (println "    s2 = current Git sha1")
  (println)
  (println "  example:")
  (println "    lein polylith info")
  (println "    lein polylith info a")
  (println "    lein polylith info all")
  (println "    lein polylith info" sha1 sha2)
  (println "    lein polylith info a" sha1 sha2)
  (println "    lein polylith info c" sha1 sha2))

