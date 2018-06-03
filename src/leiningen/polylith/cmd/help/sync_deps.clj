(ns leiningen.polylith.cmd.help.sync-deps)

(defn help []
  (println "  It first makes sure that the library versions in project.clj")
  (println "  files of components and bases are in sync with the library")
  (println "  versions in environment/development/project.clj.")
  (println)
  (println "  Secondly it makes sure that each system has a library list")
  (println "  that reflects the sum of all libraries of its components")
  (println "  and bases.")
  (println)
  (println "  lein polylith sync-deps")
  (println)
  (println "  examples:")
  (println "    lein polylith sync-deps"))